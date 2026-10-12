package com.lyk.coursearrange.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lyk.coursearrange.entity.Teacher;
import com.lyk.coursearrange.entity.TeacherCourse;

import java.util.List;

public interface TeacherCourseService extends IService<TeacherCourse> {

    void replaceTeacherCourses(Integer teacherId, List<Integer> courseIds);

    void removeByCourseId(Integer courseId);

    void fillTeachers(List<Teacher> teachers);
}
