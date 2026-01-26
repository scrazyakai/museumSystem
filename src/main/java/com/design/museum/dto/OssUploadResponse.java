package com.design.museum.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * OSS上传响应
 */
@Data
public class OssUploadResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 文件访问URL
     */
    private String url;

    /**
     * 媒体类型：IMAGE/VIDEO
     */
    private String mediaKind;

    /**
     * 原始文件名
     */
    private String originalName;

    /**
     * 文件大小（字节）
     */
    private Long size;
}