package com.design.museum.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 登录响应
 */
@Data
public class LoginResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像
     */
    private String avatarUrl;

    /**
     * 角色
     */
    private String role;

    /**
     * Sa-Token令牌
     */
    private String token;
}
