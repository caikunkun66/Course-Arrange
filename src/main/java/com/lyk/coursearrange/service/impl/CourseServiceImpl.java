package com.lyk.coursearrange.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyk.coursearrange.dao.CourseDao;
import com.lyk.coursearrange.entity.Course;
import com.lyk.coursearrange.service.CourseService;
import org.springframework.stereotype.Service;

@Service
public class CourseServiceImpl extends ServiceImpl<CourseDao, Course> implements CourseService {

    @Override
    public Course saveByName(String name, String imageUrl) {
        String trimmed = name == null ? "" : name.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("课程名称不能为空");
        }
        if (trimmed.length() > 50) {
            throw new IllegalArgumentException("课程名称不能超过50个字");
        }

        Course existing = getOne(new QueryWrapper<Course>().eq("name", trimmed));
        String picture = imageUrl == null ? null : imageUrl.trim();
        if (existing != null) {
            if (picture != null && !picture.isEmpty() && !picture.equals(existing.getImageUrl())) {
                existing.setImageUrl(picture);
                updateById(existing);
            }
            return existing;
        }

        Course course = new Course();
        course.setName(trimmed);
        course.setDeleted(0);
        if (picture != null && !picture.isEmpty()) {
            course.setImageUrl(picture);
        }
        save(course);
        return course;
    }

    @Override
    public Course updateCourse(Integer id, String name, String imageUrl) {
        Course course = getById(id);
        if (course == null) {
            throw new IllegalArgumentException("课程不存在");
        }
        String trimmed = name == null ? "" : name.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("课程名称不能为空");
        }
        if (trimmed.length() > 50) {
            throw new IllegalArgumentException("课程名称不能超过50个字");
        }
        Course sameName = getOne(new QueryWrapper<Course>().eq("name", trimmed));
        if (sameName != null && !sameName.getId().equals(id)) {
            throw new IllegalArgumentException("课程名称已存在");
        }
        course.setName(trimmed);
        course.setImageUrl(imageUrl == null ? null : imageUrl.trim());
        updateById(course);
        return course;
    }
}
