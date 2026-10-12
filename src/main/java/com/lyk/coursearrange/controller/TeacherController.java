package com.lyk.coursearrange.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lyk.coursearrange.common.PassToken;
import com.lyk.coursearrange.common.ServerResponse;
import com.lyk.coursearrange.entity.Student;
import com.lyk.coursearrange.entity.Teacher;
import com.lyk.coursearrange.entity.request.PasswordVO;
import com.lyk.coursearrange.entity.request.TeacherAddRequest;
import com.lyk.coursearrange.entity.request.UserLoginRequest;
import com.lyk.coursearrange.entity.request.StudentRegisterRequest;
import com.lyk.coursearrange.service.StudentService;
import com.lyk.coursearrange.service.TeacherCourseService;
import com.lyk.coursearrange.service.TeacherService;
import com.lyk.coursearrange.service.impl.TokenService;
import com.lyk.coursearrange.util.AliyunUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lequal
 * @since 2020-03-13
 */
@RestController
@RequestMapping("/teacher")
public class TeacherController {

    @Autowired
    private TeacherService teacherService;
    @Autowired
    private TeacherCourseService teacherCourseService;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private StudentService studentService;





    /**
     * 教师登录
     * @param userLoginRequest
     * @return
     */
    @PassToken
    @PostMapping("/login")
    public ServerResponse teacherLogin(@RequestBody UserLoginRequest userLoginRequest) {
        Map<String, Object> map = new HashMap<>();
        String account = StringUtils.isEmpty(userLoginRequest.getAccount())
                ? userLoginRequest.getUsername() : userLoginRequest.getAccount();
        QueryWrapper<Teacher> wrapper = new QueryWrapper<>();
        wrapper.eq("account", account);
        Teacher teacher2 = teacherService.getOne(wrapper);
        if (teacher2 == null) {
            return ServerResponse.ofError("账号不存在");
        } else if (teacher2.getStatus() != 0) {
            return ServerResponse.ofError("账号状态异常，请联系管理员");
        }
        Teacher teacher = teacherService.teacherLogin(account, userLoginRequest.getPassword());

        if (teacher != null) {
            teacherCourseService.fillTeachers(Collections.singletonList(teacher));
            // 允许登录
            String token = tokenService.getToken(teacher);
            map.put("teacher", teacher);
            map.put("token", token);
            return ServerResponse.ofSuccess(map);
        }
        // 否则一律视为密码错误
        return ServerResponse.ofError("密码错误");
    }

    /**
     * 教师注册（简化版）
     * @param req
     * @return
     */
    @PassToken
    @PostMapping("/register")
    public ServerResponse teacherRegister(@RequestBody StudentRegisterRequest req) {
        System.out.println(req);
        
        if (StringUtils.isEmpty(req.getAccount())) {
            return ServerResponse.ofError("账号不能为空");
        }
        QueryWrapper<Teacher> wrapper = new QueryWrapper<Teacher>().eq("account", req.getAccount());
        Teacher existTeacher = teacherService.getOne(wrapper);
        if (existTeacher != null) {
            return ServerResponse.ofError("账号已存在，请更换账号");
        }
        
        Teacher teacher = new Teacher();
        teacher.setAccount(req.getAccount());
        teacher.setUsername(StringUtils.isEmpty(req.getUsername()) ? req.getAccount() : req.getUsername());
        teacher.setPassword(req.getPassword());
        teacher.setUserType(2); // 教师类型为2
        teacher.setStatus(0); // 默认状态为正常
        
        boolean b = teacherService.save(teacher);
        if (b) {
            return ServerResponse.ofSuccess("注册成功", teacher);
        }
        return ServerResponse.ofError("注册失败!");
    }

    /**
     * 根据id查询教师，用于更新操作
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public ServerResponse queryTeacherById(@PathVariable("id") Integer id) {
        Teacher teacher = teacherService.getById(id);
        if (teacher != null) {
            teacherCourseService.fillTeachers(Collections.singletonList(teacher));
        }
        return ServerResponse.ofSuccess(teacher);
    }

    /**
     * 更新教师
     * @param teacher
     * @return
     */
    @PostMapping("/modify")
    public ServerResponse modifyTeacher(@RequestBody Teacher teacher) {
        if (!StringUtils.isEmpty(teacher.getAccount()) && teacher.getId() != null) {
            QueryWrapper<Teacher> accountWrapper = new QueryWrapper<Teacher>()
                    .eq("account", teacher.getAccount())
                    .ne("id", teacher.getId());
            if (teacherService.count(accountWrapper) > 0) {
                return ServerResponse.ofError("账号已存在");
            }
        }

        boolean b = teacherService.updateById(teacher);
        if (teacher.getCourseIds() != null) {
            teacherCourseService.replaceTeacherCourses(teacher.getId(), teacher.getCourseIds());
        }

        if (b || teacher.getCourseIds() != null) {
            return ServerResponse.ofSuccess("更新成功");
        }
        return ServerResponse.ofError("更新失败");
    }

    /**
     * 分页查询教师
     * @param page
     * @param limit
     * @return
     */
    @GetMapping("/query/{page}")
    public ServerResponse queryTeacher(@PathVariable(value = "page") Integer page,
                                       @RequestParam(defaultValue = "10") Integer limit) {
        Page<Teacher> pages = new Page<>(page, limit);
        QueryWrapper<Teacher> wrapper = new QueryWrapper<Teacher>().orderByDesc("create_time");
        IPage<Teacher> iPage = teacherService.page(pages, wrapper);
        teacherCourseService.fillTeachers(iPage.getRecords());
        return ServerResponse.ofSuccess(iPage);
    }

    /**
     * 根据用户名关键字搜索教师
     * @return
     */
    @GetMapping("/search/{page}/{keyword}")
    public ServerResponse searchTeacher(@PathVariable("keyword") String keyword, @PathVariable("page") Integer page,
                                        @RequestParam(defaultValue = "10") Integer limit) {
        QueryWrapper<Teacher> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("update_time");
        if (!StringUtils.isEmpty(keyword)) {
            wrapper.and(w -> w.like("username", keyword).or().like("account", keyword));
        }
        Page<Teacher> pages = new Page<>(page, limit);
        IPage<Teacher> iPage = teacherService.page(pages, wrapper);
        teacherCourseService.fillTeachers(iPage.getRecords());
        if (page != null) {
            return ServerResponse.ofSuccess(iPage);
        }
        return ServerResponse.ofError("查询失败!");
    }

    /**
     * 管理员根据ID删除教师
     * @return
     */
    @DeleteMapping("/delete/{id}")
    public ServerResponse deleteTeacher(@PathVariable Integer id) {
        boolean b = teacherService.removeById(id);
        if(b) {
            return ServerResponse.ofSuccess("删除成功！");
        }
        return ServerResponse.ofError("删除失败！");
    }


    /**
     * 管理员添加教师,默认密码是123456
     * @param t
     * @return
     */
    @PostMapping("/add")
    public ServerResponse addTeacher(@RequestBody TeacherAddRequest t) {
        String account = StringUtils.isEmpty(t.getAccount()) ? t.getUsername() : t.getAccount();
        if (StringUtils.isEmpty(account)) {
            return ServerResponse.ofError("账号不能为空");
        }
        QueryWrapper<Teacher> exist = new QueryWrapper<Teacher>().eq("account", account);
        if (teacherService.getOne(exist) != null) {
            return ServerResponse.ofError("账号已存在");
        }
        Teacher teacher = new Teacher();
        teacher.setAccount(account);
        teacher.setUsername(StringUtils.isEmpty(t.getUsername()) ? account : t.getUsername());
        // 每一个新增的教师密码默认是123456
        teacher.setPassword("123456");
        teacher.setAddress(t.getAddress());
        boolean b = teacherService.save(teacher);
        if (b) {
            if (t.getCourseIds() != null) {
                teacherCourseService.replaceTeacherCourses(teacher.getId(), t.getCourseIds());
            }
            return ServerResponse.ofSuccess("添加教师成功！");
        }
        return ServerResponse.ofError("添加教师失败！");
    }

    /**
     * 根据ID封禁、解封教师账号，状态为0时正常，1时封禁
     * @param id
     * @return
     */
    @GetMapping("/lock/{id}")
    public ServerResponse lockTeacher(@PathVariable("id") Integer id) {

        // 先查出来再修改，
        Teacher teacher = teacherService.getById(id);
        // 修改
        if (teacher.getStatus() == 0) {
            teacher.setStatus(1);
        } else {
            teacher.setStatus(0);
        }
        teacherService.updateById(teacher);
        return ServerResponse.ofSuccess("操作成功！");
    }


    /**
     * 修改密码
     * @param passwordVO
     * @return
     */
    @PostMapping("/password")
    public ServerResponse updatePass(@RequestBody PasswordVO passwordVO) {
        QueryWrapper<Teacher> wrapper = new QueryWrapper();
        wrapper.eq("id", passwordVO.getId());
        wrapper.eq("password", passwordVO.getOldPass());
        Teacher teacher = teacherService.getOne(wrapper);
        if (teacher == null) {
            return ServerResponse.ofError("旧密码错误");
        }
        // 否则进入修改密码流程
        teacher.setPassword(passwordVO.getNewPass());
        boolean b = teacherService.updateById(teacher);
        if (b) {
            return ServerResponse.ofSuccess("密码修改成功");
        }
        return ServerResponse.ofError("密码更新失败");
    }

    /**
     * 查询所有教师
     * @return
     */
    @GetMapping("/all")
    public ServerResponse getAllTeacher() {
        List<Teacher> teachers = teacherService.list();
        teacherCourseService.fillTeachers(teachers);
        return ServerResponse.ofSuccess(teachers);
    }

    /**
     * 教师查看自己名下的学生（培训模式）
     * @param teacherId
     * @param page
     * @param limit
     * @return
     */
    @GetMapping("/students/{teacherId}/{page}")
    public ServerResponse getMyStudents(@PathVariable("teacherId") Integer teacherId,
                                        @PathVariable("page") Integer page,
                                        @RequestParam(defaultValue = "10") Integer limit) {
        Page<Student> pages = new Page<>(page, limit);
        QueryWrapper<Student> wrapper = new QueryWrapper<Student>()
                .eq("teacher_id", teacherId)
                .orderByDesc("create_time");
        IPage<Student> iPage = studentService.page(pages, wrapper);
        return ServerResponse.ofSuccess(iPage);
    }

}

