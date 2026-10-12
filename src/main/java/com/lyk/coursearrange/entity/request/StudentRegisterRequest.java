package com.lyk.coursearrange.entity.request;

import lombok.Data;

/**
 * @author: 15760
 * @Date: 2020/3/16
 * @Descripe: 封装学生注册的信息（简化版）
 */
@Data
public class StudentRegisterRequest {

    /**
     * 登录账号
     */
    private String account;

    /**
     * 昵称
     */
    private String username;

    /**
     * 密码
     */
    private String password;

}
