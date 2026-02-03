package com.design.museum.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.design.museum.annotation.AdminAuth;
import com.design.museum.common.BaseResponse;
import com.design.museum.common.ResultUtils;
import com.design.museum.service.IExhibitCommentService;
import com.design.museum.vo.CommentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 管理员评论管理接口
 */
@RestController
@RequestMapping("/admin/comments")
@Tag(name = "管理员评论接口", description = "管理员评论管理相关接口")
@Validated
public class AdminCommentController {

    @Resource
    private IExhibitCommentService exhibitCommentService;

    /**
     * 分页查询评论
     *
     * @param itemId  展品ID（可选）
     * @param itemName 展品名称（可选，模糊搜索）
     * @param current 当前页
     * @param size    每页大小
     * @return 评论分页列表
     */
    @GetMapping
    @AdminAuth
    @Operation(summary = "分页查询评论", description = "管理员分页查询评论，支持按展品ID/名称搜索")
    public BaseResponse<Page<CommentVO>> listComments(
            @Parameter(description = "展品ID") @RequestParam(value = "itemId", required = false) Long itemId,
            @Parameter(description = "展品名称") @RequestParam(value = "itemName", required = false) String itemName,
            @Parameter(description = "当前页") @RequestParam(value = "current", defaultValue = "1") long current,
            @Parameter(description = "每页大小") @RequestParam(value = "size", defaultValue = "10") long size,
            @Parameter(description = "评论状态") @RequestParam(value = "status", required = false) Integer status
    ) {
        // 获取当前登录用户ID
        Long userId = StpUtil.getLoginIdAsLong();
        Page<CommentVO> page = exhibitCommentService.adminListComments(itemId, itemName, current, size, status, userId);
        return ResultUtils.success(page);
    }

    /**
     * 删除评论（逻辑删除）
     *
     * @param id 评论ID
     * @return 是否成功
     */
    @PostMapping("/delete/{id}")
    @AdminAuth
    @Operation(summary = "删除评论", description = "管理员删除指定评论（逻辑删除）")
    public BaseResponse<Boolean> deleteComment(
            @Parameter(description = "评论ID") @PathVariable("id") Long id) {
        boolean result = exhibitCommentService.adminDeleteComment(id);
        return ResultUtils.success("删除成功", result);
    }

    /**
     * 隐藏评论
     *
     * @param id 评论ID
     * @return 是否成功
     */
    @PostMapping("/{id}/hide")
    @AdminAuth
    @Operation(summary = "隐藏评论", description = "管理员隐藏指定评论（设置status=0）")
    public BaseResponse<Boolean> hideComment(
            @Parameter(description = "评论ID") @PathVariable("id") Long id) {
        boolean result = exhibitCommentService.adminHideComment(id);
        return ResultUtils.success("隐藏成功", result);
    }

    /**
     * 显示评论
     *
     * @param id 评论ID
     * @return 是否成功
     */
    @PostMapping("/{id}/show")
    @AdminAuth
    @Operation(summary = "显示评论", description = "管理员显示指定评论（设置status=1）")
    public BaseResponse<Boolean> showComment(
            @Parameter(description = "评论ID") @PathVariable("id") Long id) {
        boolean result = exhibitCommentService.adminShowComment(id);
        return ResultUtils.success("显示成功", result);
    }
}
