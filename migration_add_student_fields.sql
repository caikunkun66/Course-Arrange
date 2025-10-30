-- ============================================
-- 学生表新增字段 - 数据库迁移脚本
-- 功能：添加总课时、已上课时、课程备注、学生备注字段
-- 日期：2024
-- ============================================

-- 使用数据库
USE db_course_arrangement;

-- 1. 添加总课时字段
ALTER TABLE tb_student 
ADD COLUMN total_hours INT DEFAULT 0 COMMENT '总课时' AFTER teacher_id;

-- 2. 添加已上课时字段
ALTER TABLE tb_student 
ADD COLUMN completed_hours INT DEFAULT 0 COMMENT '已上课时' AFTER total_hours;

-- 3. 添加课程备注字段
ALTER TABLE tb_student 
ADD COLUMN course_remark VARCHAR(500) DEFAULT NULL COMMENT '课程备注' AFTER completed_hours;

-- 4. 添加学生备注字段
ALTER TABLE tb_student 
ADD COLUMN student_remark VARCHAR(500) DEFAULT NULL COMMENT '学生备注' AFTER course_remark;

-- 5. 查看修改后的表结构
DESC tb_student;

-- 6. 验证数据
SELECT 
    id, 
    username, 
    teacher_id,
    total_hours,
    completed_hours,
    course_remark,
    student_remark,
    create_time
FROM tb_student
ORDER BY create_time DESC
LIMIT 10;

-- 7. 添加索引（可选，用于提高查询性能）
ALTER TABLE tb_student 
ADD INDEX idx_total_hours (total_hours),
ADD INDEX idx_completed_hours (completed_hours);

-- 完成！
-- 新增字段说明：
-- - total_hours (INT) - 总课时，默认0
-- - completed_hours (INT) - 已上课时，默认0
-- - course_remark (VARCHAR(500)) - 课程备注，可选
-- - student_remark (VARCHAR(500)) - 学生备注，可选

