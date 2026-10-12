-- 学生、教师登录账号与展示昵称分离。已有登录名原样写入 account。

SET @db = DATABASE();

SET @has = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'tb_student' AND COLUMN_NAME = 'account');
SET @sql = IF(@has = 0, 'ALTER TABLE tb_student ADD COLUMN account varchar(50) NULL COMMENT ''登录账号'' AFTER id', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE tb_student SET account = username WHERE account IS NULL OR account = '';

SET @idx = (SELECT COUNT(*) FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'tb_student' AND INDEX_NAME = 'idx_username');
SET @sql = IF(@idx > 0, 'ALTER TABLE tb_student DROP INDEX idx_username', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

ALTER TABLE tb_student MODIFY COLUMN username varchar(50) NOT NULL COMMENT '昵称';
ALTER TABLE tb_student MODIFY COLUMN account varchar(50) NOT NULL COMMENT '登录账号';

SET @idx = (SELECT COUNT(*) FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'tb_student' AND INDEX_NAME = 'idx_account');
SET @sql = IF(@idx = 0, 'ALTER TABLE tb_student ADD UNIQUE INDEX idx_account(account)', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @has = (SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'tb_teacher' AND COLUMN_NAME = 'account');
SET @sql = IF(@has = 0, 'ALTER TABLE tb_teacher ADD COLUMN account varchar(50) NULL COMMENT ''登录账号'' AFTER id', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE tb_teacher SET account = username WHERE (account IS NULL OR account = '') AND username IS NOT NULL AND username <> '';
UPDATE tb_teacher SET account = CONCAT('teacher', id) WHERE account IS NULL OR account = '';

ALTER TABLE tb_teacher MODIFY COLUMN username varchar(36) NULL COMMENT '昵称';
ALTER TABLE tb_teacher MODIFY COLUMN account varchar(50) NOT NULL COMMENT '登录账号';

SET @idx = (SELECT COUNT(*) FROM information_schema.STATISTICS WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'tb_teacher' AND INDEX_NAME = 'idx_account');
SET @sql = IF(@idx = 0, 'ALTER TABLE tb_teacher ADD UNIQUE INDEX idx_account(account)', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
