package com.lyk.coursearrange.entity.request;

import lombok.Data;

/**
 * @author: 15760
 * @Date: 2020/3/14
 * @Descripe: 封装用户(管理员，教师)登录请求体
 */
@Data
public class UserLoginRequest {

    /**
     * 管理员登录仍用此字段（对应 admin_no）
     */
    private String username;

    /**
     * 教师登录账号
     */
    private String account;

    private String password;

    /**
     * 1管理员，2教师
     */
    private Integer type;

}
