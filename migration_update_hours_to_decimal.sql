-- ============================================================
-- 修改学生表的总课时和已完成课时字段，支持小数（如1.5）
-- 执行时间: 2025-11-03
-- 说明: 将 total_hours 和 completed_hours 从 INT 改为 DECIMAL(6,1)
-- ============================================================

USE `db_course_arrangement`;

-- 开始事务（可选，但建议使用）
START TRANSACTION;

-- 1. 修改 total_hours 字段为 decimal(6,1)，支持一位小数
-- 范围：0.0 到 99999.9
ALTER TABLE `tb_student` 
MODIFY COLUMN `total_hours` DECIMAL(6, 1) NULL DEFAULT 0.0 COMMENT '总课时（支持小数，如1.5）';

-- 2. 修改 completed_hours 字段为 decimal(6,1)，支持一位小数
-- 范围：0.0 到 99999.9
ALTER TABLE `tb_student` 
MODIFY COLUMN `completed_hours` DECIMAL(6, 1) NULL DEFAULT 0.0 COMMENT '已上课时（支持小数，如1.5）';

-- 提交事务
COMMIT;

-- 3. 验证修改结果
SELECT 
    COLUMN_NAME AS '字段名',
    COLUMN_TYPE AS '数据类型',
    IS_NULLABLE AS '可为空',
    COLUMN_DEFAULT AS '默认值',
    COLUMN_COMMENT AS '备注'
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'db_course_arrangement'
  AND TABLE_NAME = 'tb_student'
  AND COLUMN_NAME IN ('total_hours', 'completed_hours')
ORDER BY ORDINAL_POSITION;

-- 4. 查看示例数据（可选）
SELECT 
    id,
    username AS '学生姓名',
    total_hours AS '总课时',
    completed_hours AS '已完成课时',
    (total_hours - completed_hours) AS '剩余课时'
FROM tb_student
LIMIT 10;

