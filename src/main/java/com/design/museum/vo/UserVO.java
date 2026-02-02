package com.design.museum.vo;

import lombok.Data;


@Data
public class UserVO {
    /**
     * 用户名
     */
    private String username;
    /**
     * 头像URL
     */
    private String avatarUrl;
    /**
     * 手机号
     */
    private String phone;
    /**
     * 真实姓名
     */
    private String realName;
    /**
     * 身份证号
     */
    private String idNo;
}
