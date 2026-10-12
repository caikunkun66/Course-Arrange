package com.lyk.coursearrange.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 学生个性化课程安排表（培训机构模式）
 * @author system
 * @since 2025-10-30
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("tb_student_course")
public class StudentCourse extends Model<StudentCourse> {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 学生ID
     */
    @TableField("student_id")
    private Integer studentId;

    /**
     * 课程名称
     */
    @TableField("course_name")
    private String courseName;

    /**
     * 上课日期
     */
    @TableField("course_date")
    private String courseDate;

    /**
     * 开始时间
     */
    @TableField("start_time")
    private String startTime;

    /**
     * 结束时间
     */
    @TableField("end_time")
    private String endTime;

    /**
     * 授课教师ID
     */
    @TableField("teacher_id")
    private Integer teacherId;

    /**
     * 授课教师姓名（冗余字段）
     */
    @TableField("teacher_name")
    private String teacherName;

    

    /**
     * 课时数（支持小数，如1.5）
     */
    @TableField("duration")
    private Double duration;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 已废弃，完成状态在关联表
     */
    @TableField("status")
    private Integer status;

    /**
     * 本节课的学生
     */
    @TableField(exist = false)
    private java.util.List<Integer> studentIds;

    /**
     * 本节课的学生及完成状态
     */
    @TableField(exist = false)
    private java.util.List<CourseStudent> students;

    /**
     * 逻辑删除
     */
    @TableLogic
    @TableField("deleted")
    private Integer deleted;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @Override
    protected Serializable pkVal() {
        return this.id;
    }
}


