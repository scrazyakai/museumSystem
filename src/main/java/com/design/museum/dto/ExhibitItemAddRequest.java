package com.design.museum.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 添加展品请求
 */
@Data
public class ExhibitItemAddRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 展品标题
     */
    @NotBlank(message = "展品标题不能为空")
    private String title;

    /**
     * 展品描述
     */
    private String description;

    /**
     * 媒体类型：IMAGE/VIDEO
     */
    @NotBlank(message = "媒体类型不能为空")
    private String mediaKind;

    /**
     * 媒体URL（OSS地址）
     */
    @NotBlank(message = "媒体URL不能为空")
    private String mediaUrl;

    /**
     * 封面URL（视频可选）
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
}