package com.lyk.coursearrange.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lyk.coursearrange.entity.StudentCourse;

import java.util.List;

/**
 * 学生课程安排Service
 */
public interface StudentCourseService extends IService<StudentCourse> {

    List<StudentCourse> getByStudentId(Integer studentId, String startDate, String endDate);

    List<StudentCourse> listInRange(String startDate, String endDate, Integer teacherId, Integer studentId);

    StudentCourse getDetail(Integer courseId);

    boolean addCourse(StudentCourse studentCourse);

    boolean updateCourse(StudentCourse studentCourse);

    boolean deleteCourse(Integer courseId);

    /**
     * 标记某个学生完成本节课，并增加该学生已上课时。
     */
    boolean completeCourse(Integer courseId, Integer studentId);

    /**
     * 把本节课里尚未完成的学生全部标记完成。
     * @return 实际完成的学生人数
     */
    int completeAll(Integer courseId);

    /**
     * 取消本节课里尚未完成的学生。
     */
    boolean cancelCourse(Integer courseId);
}
