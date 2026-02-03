package com.design.museum.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.design.museum.common.BaseResponse;
import com.design.museum.common.ResultUtils;
import com.design.museum.service.IExhibitCommentLikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * <p>
 *  评论点赞控制器
 * </p>
 *
 * @author
 * @since 2026-02-03
 */
@Tag(name = "评论点赞管理")
@RestController
@RequestMapping("/comment-like")
@Slf4j
public class ExhibitCommentLikeController {

    @Resource
    private IExhibitCommentLikeService exhibitCommentLikeService;

    /**
     * 点赞
     */
    @Operation(summary = "点赞评论")
    @SaCheckLogin
    @PostMapping("/like")
    public BaseResponse<Boolean> likeComment(@RequestParam Long commentId) {
        Long userId = StpUtil.getLoginIdAsLong();
        boolean result = exhibitCommentLikeService.likeComment(commentId, userId);
        return ResultUtils.success(result);
    }

    /**
     * 取消点赞
     */
    @Operation(summary = "取消点赞")
    @SaCheckLogin
    @PostMapping("/cancel")
    public BaseResponse<Boolean> cancelLike(@RequestParam Long commentId) {
        Long userId = StpUtil.getLoginIdAsLong();
        boolean result = exhibitCommentLikeService.cancelLike(commentId, userId);
        return ResultUtils.success(result);
    }

}
