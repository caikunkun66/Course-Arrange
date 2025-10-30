package com.lyk.coursearrange.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lyk.coursearrange.entity.StudentCourse;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学生课程安排Mapper
 * @author system
 * @since 2025-10-30
 */
@Mapper
public interface StudentCourseDao extends BaseMapper<StudentCourse> {
}


