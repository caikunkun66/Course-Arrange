package com.lyk.coursearrange.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 一节课与上课学生的关联。完成状态按学生记录。
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("tb_course_student")
public class CourseStudent extends Model<CourseStudent> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("course_id")
    private Integer courseId;

    @TableField("student_id")
    private Integer studentId;

    /**
     * 0未开始 2已完成 3已取消
     */
    private Integer status;

    @TableField(exist = false)
    private String username;

    @Override
    protected Serializable pkVal() {
        return this.id;
    }
}
