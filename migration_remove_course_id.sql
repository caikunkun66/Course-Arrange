-- 删除tb_student_course表的course_id字段
-- 执行时间：2025-10-30
-- 说明：删除course_id列，因为现在直接使用course_name字段存储课程名称

USE db_course_arrangement;

-- 删除course_id列
ALTER TABLE tb_student_course DROP COLUMN course_id;

-- 查看表结构确认修改
DESCRIBE tb_student_course;

-- 说明：执行成功后，tb_student_course表将不再包含course_id字段
-- course_name字段将直接存储教师的teach字段值

