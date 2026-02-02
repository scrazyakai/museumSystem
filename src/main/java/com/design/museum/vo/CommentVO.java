package com.design.museum.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评论响应对象
 */
@Data
public class CommentVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 评论ID
     */
    private Long id;

    /**
     * 展品ID
     */
    private Long itemId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 评论内容
     */
    private String content;

    /**
     * 状态：0隐藏，1展示
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    /**
     *  头像URL
     */
    private String avatarURL;
}