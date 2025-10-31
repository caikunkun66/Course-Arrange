package com.lyk.coursearrange.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyk.coursearrange.dao.StudentCourseDao;
import com.lyk.coursearrange.entity.StudentCourse;
import com.lyk.coursearrange.entity.Student;
import com.lyk.coursearrange.entity.Teacher;
import com.lyk.coursearrange.service.StudentCourseService;
import com.lyk.coursearrange.service.StudentService;
import com.lyk.coursearrange.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    @Autowired
    private StudentService studentService;

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

    @Override
    public boolean completeCourse(Integer courseId) {
        StudentCourse course = this.getById(courseId);
        if (course == null) {
            return false;
        }
        // 仅允许未开始(0) -> 已完成(2)
        if (course.getStatus() == null || course.getStatus() != 0) {
            return false;
        }

        course.setStatus(2);
        boolean updated = this.updateById(course);
        if (!updated) {
            return false;
        }

        if (course.getStudentId() != null) {
            Student student = studentService.getById(course.getStudentId());
            if (student != null) {
                // 使用课程的实际课时数（如果是null或0，则默认为1）
                Integer courseHours = course.getDuration() == null || course.getDuration() <= 0 ? 1 : course.getDuration();
                Integer completed = student.getCompletedHours() == null ? 0 : student.getCompletedHours();
                student.setCompletedHours(completed + courseHours);
                // 记录课程完成操作到学生日志
                StringBuilder logBuilder = new StringBuilder();
                String existingLog = student.getCompletionLog();
                if (existingLog != null && !existingLog.trim().isEmpty()) {
                    logBuilder.append(existingLog.trim()).append("\n");
                }
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String now = LocalDateTime.now().format(dtf);
                String teacherName = course.getTeacherName() == null ? "-" : course.getTeacherName();
                String courseName = course.getCourseName() == null ? "-" : course.getCourseName();
                String start = course.getStartTime() == null ? "-" : course.getStartTime();
                String end = course.getEndTime() == null ? "-" : course.getEndTime();
                // 确保课时显示为整数格式（不带.0）
                String durationText = course.getDuration() == null ? "1" : String.valueOf(course.getDuration());
                String line = String.format("[%s] 完成课程：%s，日期：%s，时间：%s-%s，教师：%s，课时：%s，课程ID：%d",
                        now,
                        courseName,
                        course.getCourseDate(),
                        start,
                        end,
                        teacherName,
                        durationText,
                        course.getId());
                logBuilder.append(line);
                student.setCompletionLog(logBuilder.toString());
                studentService.updateById(student);
            }
        }
        return true;
    }

    @Override
    public boolean cancelCourse(Integer courseId) {
        StudentCourse course = this.getById(courseId);
        if (course == null) {
            return false;
        }
        // 仅允许未开始(0) -> 已取消(3)
        if (course.getStatus() == null || course.getStatus() != 0) {
            return false;
        }
        course.setStatus(3);
        return this.updateById(course);
    }
}

