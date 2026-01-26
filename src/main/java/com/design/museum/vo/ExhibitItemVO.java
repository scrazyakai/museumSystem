package com.design.museum.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 展品响应对象
 */
@Data
public class ExhibitItemVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 展品ID
     */
    private Long id;

    /**
     * 展品标题
     */
    private String title;

    /**
     * 展品描述
     */
    private String description;

    /**
     * 媒体类型：IMAGE/VIDEO
     */
    private String mediaKind;

    /**
     * 媒体URL
     */
    private String mediaUrl;

    /**
     * 封面URL
     */
    private String coverUrl;

    /**
     * 展览开始时间
     */
    private LocalDateTime startTime;

    /**
     * 展览结束时间
     */
    private LocalDateTime endTime;

    /**
     * 创建人ID
     */
    private Long creatorId;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}