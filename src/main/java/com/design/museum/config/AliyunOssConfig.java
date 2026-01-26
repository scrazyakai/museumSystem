package com.design.museum.config;

import cn.hutool.core.lang.UUID;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * 阿里云OSS配置类
 */
@Data
@Component
public class AliyunOssConfig {

    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    @Value("${aliyun.oss.accessKeyId}")
    private String accessKeyId;

    @Value("${aliyun.oss.accessKeySecret}")
    private String accessKeySecret;

    @Value("${aliyun.oss.bucketName}")
    private String bucketName;

    /**
     * 上传文件到OSS
     *
     * @param file 文件
     * @param dir  目录（images/ 或 videos/）
     * @return 文件访问URL
     */
    public String upload(MultipartFile file, String dir) throws IOException {
        // 构建 OSSClient
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            // 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String objectName = dir + UUID.randomUUID() + fileExtension;

            // 设置文件元数据
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            // 上传文件
            ossClient.putObject(bucketName, objectName, file.getInputStream(), metadata);

            // 返回公网访问 URL
            return "https://" + bucketName + "." + endpoint + "/" + objectName;
        } finally {
            ossClient.shutdown();
        }
    }

    /**
     * 根据文件后缀或ContentType判断是图片还是视频
     *
     * @param file 文件
     * @return IMAGE 或 VIDEO
     */
    public String getMediaKind(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null) {
            throw new RuntimeException("无法识别文件类型");
        }

        if (contentType.startsWith("image/")) {
            return "IMAGE";
        } else if (contentType.startsWith("video/")) {
            return "VIDEO";
        } else {
            throw new RuntimeException("不支持的文件类型，仅支持图片和视频");
        }
    }

    /**
     * 根据媒体类型获取上传目录
     *
     * @param mediaKind IMAGE 或 VIDEO
     * @return images/ 或 videos/
     */
    public String getDirByMediaKind(String mediaKind) {
        if ("IMAGE".equals(mediaKind)) {
            return "images/";
        } else if ("VIDEO".equals(mediaKind)) {
            return "videos/";
        } else {
            throw new RuntimeException("不支持的媒体类型: " + mediaKind);
        }
    }
}