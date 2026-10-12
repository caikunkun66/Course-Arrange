package com.lyk.coursearrange.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyk.coursearrange.dao.TeacherCourseDao;
import com.lyk.coursearrange.entity.Course;
import com.lyk.coursearrange.entity.Teacher;
import com.lyk.coursearrange.entity.TeacherCourse;
import com.lyk.coursearrange.service.CourseService;
import com.lyk.coursearrange.service.TeacherCourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class TeacherCourseServiceImpl extends ServiceImpl<TeacherCourseDao, TeacherCourse> implements TeacherCourseService {

    @Autowired
    private TeacherCourseDao teacherCourseDao;
    @Autowired
    private CourseService courseService;

    @Override
    public void replaceTeacherCourses(Integer teacherId, List<Integer> courseIds) {
        teacherCourseDao.delete(new QueryWrapper<TeacherCourse>().eq("teacher_id", teacherId));
        if (courseIds == null || courseIds.isEmpty()) {
            return;
        }
        Set<Integer> uniqueIds = new LinkedHashSet<Integer>(courseIds);
        for (Integer courseId : uniqueIds) {
            if (courseId == null || courseService.getById(courseId) == null) {
                continue;
            }
            TeacherCourse link = new TeacherCourse();
            link.setTeacherId(teacherId);
            link.setCourseId(courseId);
            teacherCourseDao.insert(link);
        }
    }

    @Override
    public void removeByCourseId(Integer courseId) {
        teacherCourseDao.delete(new QueryWrapper<TeacherCourse>().eq("course_id", courseId));
    }

    @Override
    public void fillTeachers(List<Teacher> teachers) {
        if (teachers == null || teachers.isEmpty()) {
            return;
        }
        List<Integer> teacherIds = new ArrayList<Integer>();
        for (Teacher teacher : teachers) {
            if (teacher != null && teacher.getId() != null) {
                teacherIds.add(teacher.getId());
                teacher.setCourses(new ArrayList<Course>());
            }
        }
        if (teacherIds.isEmpty()) {
            return;
        }

        List<TeacherCourse> links = list(new QueryWrapper<TeacherCourse>().in("teacher_id", teacherIds));
        if (links.isEmpty()) {
            return;
        }

        Set<Integer> courseIds = new LinkedHashSet<Integer>();
        for (TeacherCourse link : links) {
            courseIds.add(link.getCourseId());
        }
        Collection<Course> courses = courseService.listByIds(courseIds);
        Map<Integer, Course> courseMap = new LinkedHashMap<Integer, Course>();
        for (Course course : courses) {
            courseMap.put(course.getId(), course);
        }

        Map<Integer, Teacher> teacherMap = new LinkedHashMap<Integer, Teacher>();
        for (Teacher teacher : teachers) {
            if (teacher != null && teacher.getId() != null) {
                teacherMap.put(teacher.getId(), teacher);
            }
        }
        for (TeacherCourse link : links) {
            Teacher teacher = teacherMap.get(link.getTeacherId());
            Course course = courseMap.get(link.getCourseId());
            if (teacher != null && course != null) {
                teacher.getCourses().add(course);
            }
        }
    }

    public void fillTeacher(Teacher teacher) {
        if (teacher == null) {
            return;
        }
        fillTeachers(Collections.singletonList(teacher));
    }
}
