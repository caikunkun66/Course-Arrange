-- ============================================
-- 培训排课系统数据库迁移脚本
-- 功能：添加学生表的教师关联字段
-- 日期：2024
-- ============================================

-- 使用数据库
USE db_course_arrangement;

-- 1. 添加 teacher_id 字段到学生表
ALTER TABLE tb_student 
ADD COLUMN teacher_id INT DEFAULT NULL COMMENT '所属教师ID（培训模式）' AFTER class_no;

-- 2. 添加索引以提高查询性能
ALTER TABLE tb_student 
ADD INDEX idx_teacher_id (teacher_id);

-- 3. 添加外键约束（可选，如果希望保证数据完整性）
-- 注意：如果有历史数据且没有对应的教师，需要先处理数据再添加外键
ALTER TABLE tb_student 
ADD CONSTRAINT fk_student_teacher 
FOREIGN KEY (teacher_id) REFERENCES tb_teacher(id) 
ON DELETE SET NULL 
ON UPDATE CASCADE;

-- 4. 数据迁移（可选）：将学生关联到原班级的班主任
-- 注释说明：如果您的系统已有学生数据，并且希望根据原班级的班主任来分配教师，请执行以下SQL
-- 如果不需要，请忽略此步骤

UPDATE tb_student s
INNER JOIN tb_class_info c ON s.class_no = c.class_no
SET s.teacher_id = c.teacher
WHERE c.teacher IS NOT NULL;

-- 5. 验证迁移结果
SELECT 
    COUNT(*) as total_students,
    COUNT(teacher_id) as students_with_teacher,
    COUNT(*) - COUNT(teacher_id) as students_without_teacher
FROM tb_student;

-- 6. 查看每个教师的学生数量
SELECT 
    t.id,
    t.realname as teacher_name,
    COUNT(s.id) as student_count
FROM tb_teacher t
LEFT JOIN tb_student s ON t.id = s.teacher_id
GROUP BY t.id, t.realname
ORDER BY student_count DESC;

-- 迁移完成！
-- 请检查上面的查询结果，确保数据迁移正确

