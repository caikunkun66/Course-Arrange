package com.lyk.coursearrange.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.entity.Course;
import com.lyk.coursearrange.service.CourseService;
import com.lyk.coursearrange.service.TeacherCourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/course")
public class CourseController {

    @Autowired
    private CourseService courseService;
    @Autowired
    private TeacherCourseService teacherCourseService;

    @GetMapping("/all")
    public ServerResponse listCourses() {
        List<Course> courses = courseService.list(new QueryWrapper<Course>().orderByAsc("name"));
        return ServerResponse.ofSuccess(courses);
    }

    @PostMapping
    public ServerResponse saveCourse(@RequestBody Course course) {
        try {
            Course saved = courseService.saveByName(course.getName(), course.getImageUrl());
            return ServerResponse.ofSuccess("保存成功", saved);
        } catch (IllegalArgumentException e) {
            return ServerResponse.ofError(e.getMessage());
        }
    }

    @PostMapping("/update")
    public ServerResponse updateCourse(@RequestBody Course course) {
        try {
            Course saved = courseService.updateCourse(course.getId(), course.getName(), course.getImageUrl());
            return ServerResponse.ofSuccess("更新成功", saved);
        } catch (IllegalArgumentException e) {
            return ServerResponse.ofError(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ServerResponse deleteCourse(@PathVariable("id") Integer id) {
        teacherCourseService.removeByCourseId(id);
        boolean removed = courseService.removeById(id);
        if (removed) {
            return ServerResponse.ofSuccess("删除成功");
        }
        return ServerResponse.ofError("删除失败");
    }
}
