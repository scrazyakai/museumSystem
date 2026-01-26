package com.design.museum.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.design.museum.common.BaseResponse;
import com.design.museum.common.ResultUtils;
import com.design.museum.dto.LoginRequest;
import com.design.museum.dto.LoginResponse;
import com.design.museum.dto.RegisterRequest;
import com.design.museum.service.ISysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author 
 * @since 2026-01-19
 */
@RestController
@RequestMapping("/sys-user")
@Tag(name = "用户管理", description = "用户相关接口")
public class SysUserController {

    @Resource
    private ISysUserService sysUserService;

    /**
     * 用户注册
     *
     * @param registerRequest 注册请求
     * @return 用户ID
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "注册新用户")
    public BaseResponse<Long> register(@Validated @RequestBody RegisterRequest registerRequest) {
        Long userId = sysUserService.register(registerRequest);
        return ResultUtils.success("注册成功", userId);
    }

    /**
     * 用户登录
     *
     * @param loginRequest 登录请求
     * @return 登录响应
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "使用用户名和密码登录")
    public BaseResponse<LoginResponse> login(@Validated @RequestBody LoginRequest loginRequest) {
        LoginResponse response = sysUserService.login(loginRequest);
        return ResultUtils.success("登录成功", response);
    }

    /**
     * 用户登出
     *
     * @return 操作结果
     */
    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "用户退出登录")
    public BaseResponse<Boolean> logout() {
        StpUtil.logout();
        return ResultUtils.success("登出成功",true);
    }

    /**
     * 获取当前登录用户信息
     *
     * @return 用户信息
     */
    @GetMapping("/info")
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    public BaseResponse<LoginResponse> getCurrentUserInfo() {
        Long userId = StpUtil.getLoginIdAsLong();
        // 这里可以根据userId查询用户信息并返回
        // 简化处理，返回token信息
        LoginResponse response = new LoginResponse();
        response.setId(userId);
        response.setToken(StpUtil.getTokenValue());
        return ResultUtils.success(response);
    }
}
