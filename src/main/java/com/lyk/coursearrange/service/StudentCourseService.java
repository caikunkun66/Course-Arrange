package com.lyk.coursearrange.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lyk.coursearrange.entity.StudentCourse;

import java.util.List;

/**
 * 学生课程安排Service
 * @author system
 * @since 2025-10-30
 */
public interface StudentCourseService extends IService<StudentCourse> {
    
    /**
     * 根据学生ID查询课程安排
     * @param studentId 学生ID
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 课程列表
     */
    List<StudentCourse> getByStudentId(Integer studentId, String startDate, String endDate);
    
    /**
     * 添加课程安排
     * @param studentCourse 课程信息
     * @return 是否成功
     */
    boolean addCourse(StudentCourse studentCourse);
    
    /**
     * 更新课程安排
     * @param studentCourse 课程信息
     * @return 是否成功
     */
    boolean updateCourse(StudentCourse studentCourse);
}


