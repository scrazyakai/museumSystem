package com.design.museum.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.design.museum.dto.LoginRequest;
import com.design.museum.dto.LoginResponse;
import com.design.museum.dto.RegisterRequest;
import com.design.museum.dto.UserQueryRequest;
import com.design.museum.entity.SysUser;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 
 * @since 2026-01-19
 */
public interface ISysUserService extends IService<SysUser> {

    /**
     * 用户登录
     *
     * @param loginRequest 登录请求
     * @return 登录响应
     */
    LoginResponse login(LoginRequest loginRequest);

    /**
     * 用户注册
     *
     * @param registerRequest 注册请求
     * @return 用户ID
     */
    Long register(RegisterRequest registerRequest);

    /**
     * 分页查询用户列表
     *
     * @param userQueryRequest 查询请求
     * @return 用户分页列表
     */
    IPage<SysUser> listUsersByPage(UserQueryRequest userQueryRequest);

    /**
     * 禁用用户
     *
     * @param userId 用户ID
     */
    boolean banUser(Long userId);

    /**
     * 解禁用户
     *
     * @param userId 用户ID
     */
    boolean unbanUser(Long userId);
}
