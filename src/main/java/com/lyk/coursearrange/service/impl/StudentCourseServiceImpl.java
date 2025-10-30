package com.lyk.coursearrange.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyk.coursearrange.dao.StudentCourseDao;
import com.lyk.coursearrange.entity.StudentCourse;
import com.lyk.coursearrange.entity.Teacher;
import com.lyk.coursearrange.service.StudentCourseService;
import com.lyk.coursearrange.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 学生课程安排Service实现
 * @author system
 * @since 2025-10-30
 */
@Service
public class StudentCourseServiceImpl extends ServiceImpl<StudentCourseDao, StudentCourse> 
        implements StudentCourseService {

    @Autowired
    private TeacherService teacherService;

    @Override
    public List<StudentCourse> getByStudentId(Integer studentId, String startDate, String endDate) {
        QueryWrapper<StudentCourse> wrapper = new QueryWrapper<>();
        wrapper.eq("student_id", studentId);
        
        if (startDate != null && !startDate.isEmpty()) {
            wrapper.ge("course_date", startDate);
        }
        
        if (endDate != null && !endDate.isEmpty()) {
            wrapper.le("course_date", endDate);
        }
        
        wrapper.orderByAsc("course_date", "start_time");
        
        return this.list(wrapper);
    }

    @Override
    public boolean addCourse(StudentCourse studentCourse) {
        // 填充教师姓名
        if (studentCourse.getTeacherId() != null && 
            (studentCourse.getTeacherName() == null || studentCourse.getTeacherName().isEmpty())) {
            Teacher teacher = teacherService.getById(studentCourse.getTeacherId());
            if (teacher != null) {
                studentCourse.setTeacherName(teacher.getRealname());
            }
        }
        
        // 设置初始状态为未开始
        if (studentCourse.getStatus() == null) {
            studentCourse.setStatus(0);
        }
        
        return this.save(studentCourse);
    }

    @Override
    public boolean updateCourse(StudentCourse studentCourse) {
        // 更新教师姓名
        if (studentCourse.getTeacherId() != null) {
            Teacher teacher = teacherService.getById(studentCourse.getTeacherId());
            if (teacher != null) {
                studentCourse.setTeacherName(teacher.getRealname());
            }
        }
        
        return this.updateById(studentCourse);
    }
}

