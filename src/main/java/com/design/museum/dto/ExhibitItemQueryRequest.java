package com.design.museum.dto;

import com.design.museum.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 展品查询请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ExhibitItemQueryRequest extends PageRequest {

    /**
     * 搜索关键词（标题模糊搜索）
     */
    private String keyword;

    /**
     * 媒体类型：IMAGE/VIDEO
     */
    private String mediaKind;

    private int size;
}