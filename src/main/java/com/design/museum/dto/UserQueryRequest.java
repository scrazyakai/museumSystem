package com.design.museum.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户查询请求
 */
@Data
public class UserQueryRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页码
     */
    private Long current = 1L;

    /**
     * 每页条数
     */
    private Long size = 10L;

    /**
     * 关键词（用户名、昵称）
     */
    private String keyword;

    /**
     * 用户状态（1-正常，0-禁用）
     */
    private Integer status;
}
