package com.design.museum.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户消息视图对象
 */
@Data
public class UserNoticeVO {

    /**
     * 消息ID
     */
    private Long id;

    /**
     * 接收用户ID
     */
    private Long userId;

    /**
     * 分类：1预约 2公告 3活动 4讲座 5系统
     */
    private Integer category;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 读取状态：0未读 1已读
     */
    private Integer readFlag;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
}
