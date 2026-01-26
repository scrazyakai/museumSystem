package com.design.museum.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.design.museum.annotation.AdminAuth;
import com.design.museum.common.BaseResponse;
import com.design.museum.common.ResultUtils;
import com.design.museum.config.AliyunOssConfig;
import com.design.museum.dto.OssUploadResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * 管理员OSS上传接口
 */
@RestController
@RequestMapping("/admin/oss")
@Tag(name = "管理员OSS接口", description = "管理员文件上传相关接口")
@Validated
public class AdminOssController {

    @Resource
    private AliyunOssConfig aliyunOssConfig;

    /**
     * 上传文件到OSS
     *
     * @param file 文件
     * @return 上传结果
     */
    @PostMapping("/upload")
    @AdminAuth
    @Operation(summary = "上传文件", description = "管理员上传图片或视频到OSS")
    public BaseResponse<OssUploadResponse> upload(@RequestParam("file") MultipartFile file) {
        try {
            // 检查文件是否为空
            if (file.isEmpty()) {
                return ResultUtils.error(400, "文件不能为空");
            }

            // 判断媒体类型
            String mediaKind = aliyunOssConfig.getMediaKind(file);

            // 获取上传目录
            String dir = aliyunOssConfig.getDirByMediaKind(mediaKind);

            // 上传文件
            String url = aliyunOssConfig.upload(file, dir);

            // 构建响应
            OssUploadResponse response = new OssUploadResponse();
            response.setUrl(url);
            response.setMediaKind(mediaKind);
            response.setOriginalName(file.getOriginalFilename());
            response.setSize(file.getSize());

            return ResultUtils.success("上传成功", response);
        } catch (IOException e) {
            return ResultUtils.error(500, "文件上传失败: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResultUtils.error(400, e.getMessage());
        }
    }
}