-- ============================================
-- 简化学生表 - 数据库迁移脚本
-- 功能：删除不需要的字段，只保留昵称
-- 日期：2024
-- 警告：此操作会删除字段及其数据，请务必先备份！
-- ============================================

-- 使用数据库
USE db_course_arrangement;

-- 备份提示
-- 在执行此脚本前，请先备份数据库：
-- mysqldump -u root -p db_course_arrangement > backup_before_simplify.sql

-- 1. 删除学生表中不需要的字段
ALTER TABLE tb_student 
DROP COLUMN IF EXISTS student_no,
DROP COLUMN IF EXISTS realname,
DROP COLUMN IF EXISTS grade,
DROP COLUMN IF EXISTS class_no,
DROP COLUMN IF EXISTS age,
DROP COLUMN IF EXISTS address,
DROP COLUMN IF EXISTS telephone,
DROP COLUMN IF EXISTS email;

-- 2. 为 username 字段添加唯一索引（用于登录）
ALTER TABLE tb_student 
ADD UNIQUE INDEX idx_username (username);

-- 3. 修改 username 字段为非空
ALTER TABLE tb_student 
MODIFY COLUMN username VARCHAR(50) NOT NULL COMMENT '昵称，用于登录';

-- 4. 查看修改后的表结构
DESC tb_student;

-- 5. 验证数据
SELECT 
    COUNT(*) as total_students,
    COUNT(DISTINCT username) as unique_usernames,
    COUNT(teacher_id) as students_with_teacher
FROM tb_student;

-- 6. 查看示例数据
SELECT id, username, teacher_id, create_time, update_time
FROM tb_student
ORDER BY create_time DESC
LIMIT 10;

-- 简化完成！
-- 最终字段列表：
-- - id (主键)
-- - username (昵称，唯一索引，非空)
-- - password (密码)
-- - user_type (用户类型标识)
-- - teacher_id (所属教师ID)
-- - avatar (头像)
-- - description (签名/描述)
-- - deleted (逻辑删除标识)
-- - status (账号状态)
-- - create_time (创建时间)
-- - update_time (更新时间)

