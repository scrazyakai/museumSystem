package com.design.museum.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.design.museum.common.BaseResponse;
import com.design.museum.common.ResultUtils;
import com.design.museum.service.IDashboardService;
import com.design.museum.vo.DashboardVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 数据监控控制器（管理员端）
 */
@Tag(name = "数据监控（管理员端）")
@RestController
@RequestMapping("/admin/dashboard")
@Slf4j
public class AdminDashboardController {

    @Resource
    private IDashboardService dashboardService;

    /**
     * 获取统计数据
     */
    @Operation(summary = "获取统计数据")
    @SaCheckLogin
    @SaCheckRole("ADMIN")
    @GetMapping("/stats")
    public BaseResponse<DashboardVO> getDashboardStats() {
        DashboardVO dashboard = dashboardService.getDashboard();
        return ResultUtils.success(dashboard);
    }
}
