package com.lyk.coursearrange.entity.request;

import lombok.Data;

/**
 * @author: 15760
 * @Date: 2020/3/14
 * @Descripe: 封装学生登录的请求体
 */
@Data
public class StudentLoginRequest {

    /**
     * 登录账号
     */
    private String account;

    /**
     * 兼容旧请求体
     */
    private String username;

    private String password;
}
