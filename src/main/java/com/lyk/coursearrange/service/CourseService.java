package com.lyk.coursearrange.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lyk.coursearrange.entity.Course;

public interface CourseService extends IService<Course> {

    /**
     * 按名称查找课程，不存在则创建。传入图片地址时更新图片。
     */
    Course saveByName(String name, String imageUrl);

    Course updateCourse(Integer id, String name, String imageUrl);
}
