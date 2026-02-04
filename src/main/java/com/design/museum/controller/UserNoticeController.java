package com.design.museum.controller;


import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.design.museum.common.BaseResponse;
import com.design.museum.common.ResultUtils;
import com.design.museum.service.IUserNoticeService;
import com.design.museum.vo.UserNoticeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * <p>
 * 用户消息前端控制器
 * </p>
 *
 * @author
 * @since 2026-01-29
 */
@Tag(name = "用户消息管理")
@RestController
@RequestMapping("/notice")
@Slf4j
public class UserNoticeController {

    @Resource
    private IUserNoticeService userNoticeService;

    /**
     * 分页查询我的消息
     */
    @Operation(summary = "我的消息列表")
    @SaCheckLogin
    @GetMapping("/my")
    public BaseResponse<IPage<UserNoticeVO>> myNotices(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer category,
            @RequestParam(required = false) Integer readFlag) {
        Long userId = StpUtil.getLoginIdAsLong();
        IPage<UserNoticeVO> result = userNoticeService.getUserNotices(page, size, userId, category, readFlag);
        return ResultUtils.success(result);
    }

    /**
     * 消息详情
     */
    @Operation(summary = "消息详情")
    @SaCheckLogin
    @GetMapping("/detail")
    public BaseResponse<UserNoticeVO> getDetail(@RequestParam Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        UserNoticeVO noticeVO = userNoticeService.getNoticeDetail(id, userId);
        return ResultUtils.success(noticeVO);
    }

    /**
     * 全部标记为已读
     */
    @Operation(summary = "全部标记为已读")
    @SaCheckLogin
    @PostMapping("/read-all")
    public BaseResponse<Boolean> markAllAsRead() {
        Long userId = StpUtil.getLoginIdAsLong();
        boolean result = userNoticeService.markAllAsRead(userId);
        return ResultUtils.success(result);
    }
}
