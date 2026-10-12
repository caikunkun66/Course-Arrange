-- 把 tb_teacher.teach 迁到课程目录和教师课程关联表。可重复执行。

CREATE TABLE IF NOT EXISTS `tb_course` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '课程id',
  `name` varchar(50) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '课程名称',
  `image_url` varchar(500) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '课程图片',
  `deleted` tinyint(1) NULL DEFAULT 0 COMMENT '逻辑删除',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_course_name`(`name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

CREATE TABLE IF NOT EXISTS `tb_teacher_course` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `teacher_id` int(11) NOT NULL COMMENT '教师id',
  `course_id` int(11) NOT NULL COMMENT '课程id',
  `create_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `idx_teacher_course`(`teacher_id`, `course_id`) USING BTREE,
  INDEX `idx_course_id`(`course_id`) USING BTREE,
  CONSTRAINT `fk_tc_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `tb_teacher` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_tc_course` FOREIGN KEY (`course_id`) REFERENCES `tb_course` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = DYNAMIC;

SET @has_teach = (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'tb_teacher' AND COLUMN_NAME = 'teach'
);

SET @migrate_courses = IF(@has_teach > 0,
  'INSERT INTO tb_course (name, deleted, create_time, update_time)
   SELECT DISTINCT TRIM(t.teach), 0, NOW(), NOW()
   FROM tb_teacher t
   LEFT JOIN tb_course c ON c.name = TRIM(t.teach) AND c.deleted = 0
   WHERE t.deleted = 0 AND t.teach IS NOT NULL AND TRIM(t.teach) <> '''' AND c.id IS NULL',
  'SELECT 1');
PREPARE stmt FROM @migrate_courses;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @migrate_links = IF(@has_teach > 0,
  'INSERT INTO tb_teacher_course (teacher_id, course_id, create_time, update_time)
   SELECT t.id, c.id, NOW(), NOW()
   FROM tb_teacher t
   JOIN tb_course c ON c.name = TRIM(t.teach) AND c.deleted = 0
   LEFT JOIN tb_teacher_course tc ON tc.teacher_id = t.id AND tc.course_id = c.id
   WHERE t.deleted = 0 AND t.teach IS NOT NULL AND TRIM(t.teach) <> '''' AND tc.id IS NULL',
  'SELECT 1');
PREPARE stmt FROM @migrate_links;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @drop_teach = IF(@has_teach > 0, 'ALTER TABLE tb_teacher DROP COLUMN teach', 'SELECT 1');
PREPARE stmt FROM @drop_teach;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 排课记录里已经在用的课程名称，补进课程目录并关联到授课教师
INSERT INTO tb_course (name, deleted, create_time, update_time)
SELECT DISTINCT TRIM(sc.course_name), 0, NOW(), NOW()
FROM tb_student_course sc
LEFT JOIN tb_course c ON c.name = TRIM(sc.course_name) AND c.deleted = 0
WHERE sc.deleted = 0 AND sc.course_name IS NOT NULL AND TRIM(sc.course_name) <> '' AND c.id IS NULL;

INSERT INTO tb_teacher_course (teacher_id, course_id, create_time, update_time)
SELECT DISTINCT sc.teacher_id, c.id, NOW(), NOW()
FROM tb_student_course sc
JOIN tb_course c ON c.name = TRIM(sc.course_name) AND c.deleted = 0
LEFT JOIN tb_teacher_course tc ON tc.teacher_id = sc.teacher_id AND tc.course_id = c.id
WHERE sc.deleted = 0 AND sc.teacher_id IS NOT NULL AND sc.course_name IS NOT NULL AND TRIM(sc.course_name) <> '' AND tc.id IS NULL;
