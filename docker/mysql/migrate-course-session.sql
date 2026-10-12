-- 同一时段合并成一条排课，学生挂到 tb_course_student。

CREATE TABLE IF NOT EXISTS `tb_course_student` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `course_id` int(11) NOT NULL COMMENT '排课ID',
  `student_id` int(11) NOT NULL COMMENT '学生ID',
  `status` tinyint(1) NULL DEFAULT 0 COMMENT '0未开始 2已完成 3已取消',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `uk_course_student`(`course_id`, `student_id`),
  INDEX `idx_student_id`(`student_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

INSERT IGNORE INTO tb_course_student (course_id, student_id, status)
SELECT keeper_id, student_id, MAX(status) AS status
FROM (
  SELECT k.min_id AS keeper_id, s.student_id, IFNULL(s.status, 0) AS status
  FROM tb_student_course s
  JOIN (
    SELECT MIN(id) AS min_id,
           course_date,
           start_time,
           end_time,
           IFNULL(course_name, '') AS course_name,
           IFNULL(teacher_id, 0) AS teacher_id
    FROM tb_student_course
    WHERE deleted = 0 AND student_id IS NOT NULL
    GROUP BY course_date, start_time, end_time, IFNULL(course_name, ''), IFNULL(teacher_id, 0)
  ) k
    ON s.course_date = k.course_date
   AND s.start_time = k.start_time
   AND s.end_time = k.end_time
   AND IFNULL(s.course_name, '') = k.course_name
   AND IFNULL(s.teacher_id, 0) = k.teacher_id
  WHERE s.deleted = 0 AND s.student_id IS NOT NULL
) grouped
GROUP BY keeper_id, student_id;

DELETE s
FROM tb_student_course s
JOIN (
  SELECT MIN(id) AS min_id,
         course_date,
         start_time,
         end_time,
         IFNULL(course_name, '') AS course_name,
         IFNULL(teacher_id, 0) AS teacher_id
  FROM tb_student_course
  WHERE deleted = 0
  GROUP BY course_date, start_time, end_time, IFNULL(course_name, ''), IFNULL(teacher_id, 0)
) k
  ON s.course_date = k.course_date
 AND s.start_time = k.start_time
 AND s.end_time = k.end_time
 AND IFNULL(s.course_name, '') = k.course_name
 AND IFNULL(s.teacher_id, 0) = k.teacher_id
WHERE s.deleted = 0 AND s.id <> k.min_id;

ALTER TABLE tb_student_course
  MODIFY COLUMN student_id int(11) NULL DEFAULT NULL COMMENT '已废弃，学生改挂 tb_course_student',
  MODIFY COLUMN status tinyint(1) NULL DEFAULT 0 COMMENT '已废弃，完成状态在 tb_course_student';
