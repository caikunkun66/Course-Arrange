package com.lyk.coursearrange.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyk.coursearrange.dao.CourseStudentDao;
import com.lyk.coursearrange.dao.StudentCourseDao;
import com.lyk.coursearrange.entity.CourseStudent;
import com.lyk.coursearrange.entity.Student;
import com.lyk.coursearrange.entity.StudentCourse;
import com.lyk.coursearrange.entity.Teacher;
import com.lyk.coursearrange.service.StudentCourseService;
import com.lyk.coursearrange.service.StudentService;
import com.lyk.coursearrange.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class StudentCourseServiceImpl extends ServiceImpl<StudentCourseDao, StudentCourse>
        implements StudentCourseService {

    @Autowired
    private TeacherService teacherService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private CourseStudentDao courseStudentDao;

    @Override
    public List<StudentCourse> getByStudentId(Integer studentId, String startDate, String endDate) {
        return listInRange(startDate, endDate, null, studentId);
    }

    @Override
    public List<StudentCourse> listInRange(String startDate, String endDate, Integer teacherId, Integer studentId) {
        QueryWrapper<StudentCourse> wrapper = new QueryWrapper<>();
        if (startDate != null && !startDate.isEmpty()) {
            wrapper.ge("course_date", startDate);
        }
        if (endDate != null && !endDate.isEmpty()) {
            wrapper.le("course_date", endDate);
        }
        if (teacherId != null) {
            wrapper.eq("teacher_id", teacherId);
        }
        if (studentId != null) {
            List<Integer> courseIds = courseIdsOfStudent(studentId);
            if (courseIds.isEmpty()) {
                return new ArrayList<>();
            }
            wrapper.in("id", courseIds);
        }
        wrapper.orderByAsc("course_date", "start_time");
        List<StudentCourse> courses = this.list(wrapper);
        fillStudents(courses);
        return courses;
    }

    @Override
    public StudentCourse getDetail(Integer courseId) {
        StudentCourse course = this.getById(courseId);
        if (course == null) {
            return null;
        }
        fillStudents(java.util.Collections.singletonList(course));
        return course;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addCourse(StudentCourse studentCourse) {
        List<Integer> studentIds = distinctIds(studentCourse.getStudentIds());
        assertSameClassDuration(studentIds);
        fillTeacherName(studentCourse);
        studentCourse.setStudentId(null);
        boolean saved = this.save(studentCourse);
        if (!saved) {
            return false;
        }
        for (Integer studentId : studentIds) {
            insertLink(studentCourse.getId(), studentId, 0);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateCourse(StudentCourse studentCourse) {
        List<Integer> studentIds = distinctIds(studentCourse.getStudentIds());
        assertSameClassDuration(studentIds);
        StudentCourse existing = this.getById(studentCourse.getId());
        if (existing == null) {
            return false;
        }
        fillTeacherName(studentCourse);
        this.updateById(studentCourse);

        double hours = studentCourse.getDuration() == null ? 0 : studentCourse.getDuration();
        List<CourseStudent> links = linksOfCourse(studentCourse.getId());
        Set<Integer> wanted = new LinkedHashSet<>(studentIds);
        for (CourseStudent link : links) {
            if (wanted.contains(link.getStudentId())) {
                wanted.remove(link.getStudentId());
                continue;
            }
            if (link.getStatus() != null && link.getStatus() == 2) {
                subtractCompletedHours(link.getStudentId(), hours);
            }
            courseStudentDao.deleteById(link.getId());
        }
        for (Integer studentId : wanted) {
            insertLink(studentCourse.getId(), studentId, 0);
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteCourse(Integer courseId) {
        QueryWrapper<CourseStudent> wrapper = new QueryWrapper<>();
        wrapper.eq("course_id", courseId);
        courseStudentDao.delete(wrapper);
        return this.removeById(courseId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean completeCourse(Integer courseId, Integer studentId) {
        return markStudentComplete(courseId, studentId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int completeAll(Integer courseId) {
        int count = 0;
        for (CourseStudent link : linksOfCourse(courseId)) {
            if (link.getStatus() == null || link.getStatus() == 0) {
                if (markStudentComplete(courseId, link.getStudentId())) {
                    count++;
                }
            }
        }
        return count;
    }

    private boolean markStudentComplete(Integer courseId, Integer studentId) {
        StudentCourse course = this.getById(courseId);
        if (course == null || studentId == null) {
            return false;
        }
        CourseStudent link = findLink(courseId, studentId);
        if (link == null || (link.getStatus() != null && link.getStatus() != 0)) {
            return false;
        }
        link.setStatus(2);
        courseStudentDao.updateById(link);

        Student student = studentService.getById(studentId);
        if (student == null) {
            return true;
        }
        double courseHours = course.getDuration() == null || course.getDuration() <= 0 ? 1.0 : course.getDuration();
        double completed = student.getCompletedHours() == null ? 0.0 : student.getCompletedHours();
        student.setCompletedHours(completed + courseHours);
        student.setCompletionLog(appendLog(student.getCompletionLog(), course, courseHours));
        studentService.updateById(student);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelCourse(Integer courseId) {
        List<CourseStudent> links = linksOfCourse(courseId);
        if (links.isEmpty()) {
            return false;
        }
        for (CourseStudent link : links) {
            if (link.getStatus() == null || link.getStatus() == 0) {
                link.setStatus(3);
                courseStudentDao.updateById(link);
            }
        }
        return true;
    }

    private void fillStudents(List<StudentCourse> courses) {
        if (courses == null || courses.isEmpty()) {
            return;
        }
        List<Integer> courseIds = courses.stream().map(StudentCourse::getId).collect(Collectors.toList());
        QueryWrapper<CourseStudent> wrapper = new QueryWrapper<>();
        wrapper.in("course_id", courseIds);
        List<CourseStudent> links = courseStudentDao.selectList(wrapper);
        Set<Integer> studentIds = links.stream().map(CourseStudent::getStudentId).collect(Collectors.toSet());
        Map<Integer, Student> students = studentIds.isEmpty()
                ? Collections.emptyMap()
                : studentService.listByIds(studentIds).stream().collect(Collectors.toMap(Student::getId, s -> s, (a, b) -> a));
        Map<Integer, List<CourseStudent>> grouped = links.stream().collect(Collectors.groupingBy(CourseStudent::getCourseId));
        for (StudentCourse course : courses) {
            List<CourseStudent> courseLinks = grouped.getOrDefault(course.getId(), new ArrayList<>());
            for (CourseStudent link : courseLinks) {
                Student student = students.get(link.getStudentId());
                link.setUsername(student == null ? null : student.getUsername());
                if (link.getStatus() == null) {
                    link.setStatus(0);
                }
            }
            course.setStudents(courseLinks);
            course.setStudentIds(courseLinks.stream().map(CourseStudent::getStudentId).collect(Collectors.toList()));
        }
    }

    private List<Integer> courseIdsOfStudent(Integer studentId) {
        QueryWrapper<CourseStudent> wrapper = new QueryWrapper<>();
        wrapper.eq("student_id", studentId);
        return courseStudentDao.selectList(wrapper).stream()
                .map(CourseStudent::getCourseId)
                .distinct()
                .collect(Collectors.toList());
    }

    private List<CourseStudent> linksOfCourse(Integer courseId) {
        QueryWrapper<CourseStudent> wrapper = new QueryWrapper<>();
        wrapper.eq("course_id", courseId);
        return courseStudentDao.selectList(wrapper);
    }

    private CourseStudent findLink(Integer courseId, Integer studentId) {
        QueryWrapper<CourseStudent> wrapper = new QueryWrapper<>();
        wrapper.eq("course_id", courseId).eq("student_id", studentId);
        return courseStudentDao.selectOne(wrapper);
    }

    private void insertLink(Integer courseId, Integer studentId, Integer status) {
        CourseStudent link = new CourseStudent();
        link.setCourseId(courseId);
        link.setStudentId(studentId);
        link.setStatus(status);
        courseStudentDao.insert(link);
    }

    private void fillTeacherName(StudentCourse studentCourse) {
        if (studentCourse.getTeacherId() == null) {
            return;
        }
        Teacher teacher = teacherService.getById(studentCourse.getTeacherId());
        if (teacher != null) {
            studentCourse.setTeacherName(teacher.getUsername());
        }
    }

    private List<Integer> distinctIds(List<Integer> studentIds) {
        if (studentIds == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(new LinkedHashSet<>(studentIds));
    }

    private void assertSameClassDuration(List<Integer> studentIds) {
        if (studentIds.isEmpty()) {
            throw new IllegalArgumentException("请选择学生");
        }
        Integer base = null;
        Collection<Student> students = studentService.listByIds(studentIds);
        Map<Integer, Student> byId = students.stream().collect(Collectors.toMap(Student::getId, s -> s, (a, b) -> a));
        for (Integer studentId : studentIds) {
            Student student = byId.get(studentId);
            if (student == null) {
                throw new IllegalArgumentException("学生不存在");
            }
            int minutes = student.getClassDuration() == null || student.getClassDuration() <= 0
                    ? 45 : student.getClassDuration();
            if (base == null) {
                base = minutes;
            } else if (!base.equals(minutes)) {
                throw new IllegalArgumentException("学生单课时长不一致，无法排在同一时段");
            }
        }
    }

    private void subtractCompletedHours(Integer studentId, double hours) {
        if (hours <= 0) {
            return;
        }
        Student student = studentService.getById(studentId);
        if (student == null) {
            return;
        }
        double completed = student.getCompletedHours() == null ? 0.0 : student.getCompletedHours();
        double next = completed - hours;
        student.setCompletedHours(next < 0 ? 0 : next);
        studentService.updateById(student);
    }

    private String appendLog(String existingLog, StudentCourse course, double courseHours) {
        StringBuilder logBuilder = new StringBuilder();
        if (existingLog != null && !existingLog.trim().isEmpty()) {
            logBuilder.append(existingLog.trim()).append("\n");
        }
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String teacherName = course.getTeacherName() == null ? "-" : course.getTeacherName();
        String courseName = course.getCourseName() == null ? "-" : course.getCourseName();
        String start = course.getStartTime() == null ? "-" : course.getStartTime();
        String end = course.getEndTime() == null ? "-" : course.getEndTime();
        String durationText = courseHours % 1 == 0 ? String.valueOf((int) courseHours) : String.format("%.1f", courseHours);
        logBuilder.append(String.format("[%s] 完成课程：%s，日期：%s，时间：%s-%s，教师：%s，课时：%s，课程ID：%d",
                now, courseName, course.getCourseDate(), start, end, teacherName, durationText, course.getId()));
        return logBuilder.toString();
    }
}
