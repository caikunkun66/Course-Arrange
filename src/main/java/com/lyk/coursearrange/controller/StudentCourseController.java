package com.lyk.coursearrange.controller;

import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.entity.StudentCourse;
import com.lyk.coursearrange.service.StudentCourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学生课程安排Controller（培训机构模式）
 * @author system
 * @since 2025-10-30
 */
@RestController
@RequestMapping("/student-course")
public class StudentCourseController {

    @Autowired
    private StudentCourseService studentCourseService;

    /**
     * 根据学生ID查询课程安排
     * @param studentId 学生ID
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 课程列表
     */
    @GetMapping("/{studentId}")
    public ServerResponse getCoursesByStudentId(
            @PathVariable("studentId") Integer studentId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            List<StudentCourse> courses = studentCourseService.getByStudentId(studentId, startDate, endDate);
            return ServerResponse.ofSuccess(courses);
        } catch (Exception e) {
            return ServerResponse.ofError("查询课程安排失败：" + e.getMessage());
        }
    }

    /**
     * 添加课程安排
     * @param studentCourse 课程信息
     * @return 操作结果
     */
    @PostMapping("/add")
    public ServerResponse addCourse(@RequestBody StudentCourse studentCourse) {
        try {
            // 验证必填字段
            if (studentCourse.getStudentId() == null) {
                return ServerResponse.ofError("学生ID不能为空");
            }
            // courseId可以为空，但courseName必须有值
            if (studentCourse.getCourseName() == null || studentCourse.getCourseName().isEmpty()) {
                return ServerResponse.ofError("课程名称不能为空");
            }
            if (studentCourse.getCourseDate() == null || studentCourse.getCourseDate().isEmpty()) {
                return ServerResponse.ofError("上课日期不能为空");
            }
            if (studentCourse.getStartTime() == null || studentCourse.getStartTime().isEmpty()) {
                return ServerResponse.ofError("开始时间不能为空");
            }
            if (studentCourse.getEndTime() == null || studentCourse.getEndTime().isEmpty()) {
                return ServerResponse.ofError("结束时间不能为空");
            }
            
            // 验证时间逻辑
            if (studentCourse.getStartTime().compareTo(studentCourse.getEndTime()) >= 0) {
                return ServerResponse.ofError("结束时间必须晚于开始时间");
            }
            
            boolean success = studentCourseService.addCourse(studentCourse);
            if (success) {
                return ServerResponse.ofSuccess("添加成功");
            } else {
                return ServerResponse.ofError("添加失败");
            }
        } catch (Exception e) {
            return ServerResponse.ofError("添加课程安排失败：" + e.getMessage());
        }
    }

    /**
     * 更新课程安排
     * @param studentCourse 课程信息
     * @return 操作结果
     */
    @PostMapping("/update")
    public ServerResponse updateCourse(@RequestBody StudentCourse studentCourse) {
        try {
            if (studentCourse.getId() == null) {
                return ServerResponse.ofError("课程ID不能为空");
            }
            
            // 验证时间逻辑
            if (studentCourse.getStartTime() != null && studentCourse.getEndTime() != null) {
                if (studentCourse.getStartTime().compareTo(studentCourse.getEndTime()) >= 0) {
                    return ServerResponse.ofError("结束时间必须晚于开始时间");
                }
            }
            
            boolean success = studentCourseService.updateCourse(studentCourse);
            if (success) {
                return ServerResponse.ofSuccess("更新成功");
            } else {
                return ServerResponse.ofError("更新失败");
            }
        } catch (Exception e) {
            return ServerResponse.ofError("更新课程安排失败：" + e.getMessage());
        }
    }

    /**
     * 删除课程安排
     * @param id 课程ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public ServerResponse deleteCourse(@PathVariable("id") Integer id) {
        try {
            boolean success = studentCourseService.removeById(id);
            if (success) {
                return ServerResponse.ofSuccess("删除成功");
            } else {
                return ServerResponse.ofError("删除失败");
            }
        } catch (Exception e) {
            return ServerResponse.ofError("删除课程安排失败：" + e.getMessage());
        }
    }

    /**
     * 批量删除课程安排
     * @param ids 课程ID列表
     * @return 操作结果
     */
    @PostMapping("/batch-delete")
    public ServerResponse batchDelete(@RequestBody List<Integer> ids) {
        try {
            boolean success = studentCourseService.removeByIds(ids);
            if (success) {
                return ServerResponse.ofSuccess("批量删除成功");
            } else {
                return ServerResponse.ofError("批量删除失败");
            }
        } catch (Exception e) {
            return ServerResponse.ofError("批量删除失败：" + e.getMessage());
        }
    }

    /**
     * 根据ID获取课程详情
     * @param id 课程ID
     * @return 课程详情
     */
    @GetMapping("/detail/{id}")
    public ServerResponse getCourseDetail(@PathVariable("id") Integer id) {
        try {
            StudentCourse course = studentCourseService.getById(id);
            if (course != null) {
                return ServerResponse.ofSuccess(course);
            } else {
                return ServerResponse.ofError("课程不存在");
            }
        } catch (Exception e) {
            return ServerResponse.ofError("查询课程详情失败：" + e.getMessage());
        }
    }

    /**
     * 标记课程为完成，并为学生已上课时 +1
     */
    @PostMapping("/{id}/complete")
    public ServerResponse complete(@PathVariable("id") Integer id) {
        try {
            boolean ok = studentCourseService.completeCourse(id);
            return ok ? ServerResponse.ofSuccess("操作成功") : ServerResponse.ofError("操作失败");
        } catch (Exception e) {
            return ServerResponse.ofError("完成课程失败：" + e.getMessage());
        }
    }

    /**
     * 取消课程（仅更新状态为已取消）
     */
    @PostMapping("/{id}/cancel")
    public ServerResponse cancel(@PathVariable("id") Integer id) {
        try {
            boolean ok = studentCourseService.cancelCourse(id);
            return ok ? ServerResponse.ofSuccess("操作成功") : ServerResponse.ofError("操作失败");
        } catch (Exception e) {
            return ServerResponse.ofError("取消课程失败：" + e.getMessage());
        }
    }
}


