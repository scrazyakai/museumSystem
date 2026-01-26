package com.design.museum.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.design.museum.common.BaseResponse;
import com.design.museum.common.ResultUtils;
import com.design.museum.dto.CommentAddRequest;
import com.design.museum.service.IExhibitCommentService;
import com.design.museum.vo.CommentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 展品评论接口
 */
@RestController
@RequestMapping("/items/{itemId}/comments")
@Tag(name = "评论接口", description = "展品评论相关接口")
@Validated
public class CommentController {

    @Resource
    private IExhibitCommentService exhibitCommentService;

    /**
     * 分页查询展品评论
     *
     * @param itemId 展品ID
     * @param current 当前页
     * @param size   每页大小
     * @return 评论分页列表
     */
    @GetMapping("/list")
    @Operation(summary = "分页查询展品评论", description = "分页查询指定展品的评论列表")
    public BaseResponse<Page<CommentVO>> listComments(
            @Parameter(description = "展品ID") @PathVariable("itemId") Long itemId,
            @Parameter(description = "当前页") @RequestParam(value = "current", defaultValue = "1") long current,
            @Parameter(description = "每页大小") @RequestParam(value = "size", defaultValue = "10") long size) {
        Page<CommentVO> page = exhibitCommentService.listComments(itemId, current, size);
        return ResultUtils.success(page);
    }

    /**
     * 添加评论
     *
     * @param itemId 展品ID
     * @param request 添加请求
     * @return 评论ID
     */
    @PostMapping("/add")
    @Operation(summary = "添加评论", description = "登录用户对展品发表评论")
    public BaseResponse<Long> addComment(
            @Parameter(description = "展品ID") @PathVariable("itemId") Long itemId,
            @Validated @RequestBody CommentAddRequest request) {
        // 检查是否登录
        if (!StpUtil.isLogin()) {
            return ResultUtils.error(401, "未登录或登录已过期");
        }

        // 获取当前登录用户ID
        Long userId = StpUtil.getLoginIdAsLong();

        Long commentId = exhibitCommentService.addComment(itemId, request, userId);
        return ResultUtils.success("评论成功", commentId);
    }

    /**
     * 删除评论
     *
     * @param commentId 评论ID
     * @return 是否成功
     */
    @DeleteMapping("/comments/{commentId}")
    @Operation(summary = "删除评论", description = "删除指定评论（仅限评论本人）")
    public BaseResponse<Boolean> deleteComment(
            @Parameter(description = "评论ID") @PathVariable("commentId") Long commentId) {
        // 检查是否登录
        if (!StpUtil.isLogin()) {
            return ResultUtils.error(401, "未登录或登录已过期");
        }

        // 获取当前登录用户ID
        Long userId = StpUtil.getLoginIdAsLong();

        boolean result = exhibitCommentService.deleteComment(commentId, userId);
        return ResultUtils.success("删除成功", result);
    }
}