package com.design.museum.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author 
 * @since 2026-01-26
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("exhibit_item")
public class ExhibitItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 展览标题/展品名称
     */
    private String title;

    /**
     * 简介/解说
     */
    private String description;

    /**
     * IMAGE/VIDEO(ITEM可用)
     */
    private String mediaKind;

    /**
     * 图片或视频OSS地址
     */
    private String mediaUrl;

    /**
     * 封面图(视频建议有封面)
     */
    private String coverUrl;

    /**
     * 0上架 1下架
     */
    private Integer status;

    /**
     * 展览展示周期开始(可空)
     */
    private LocalDateTime startTime;

    /**
     * 展览展示周期结束(可空)
     */
    private LocalDateTime endTime;

    /**
     * 创建人(管理员ID)
     */
    private Long creatorId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;


}
