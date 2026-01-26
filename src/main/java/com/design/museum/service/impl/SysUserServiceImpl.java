package com.design.museum.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.design.museum.enums.UserRoleEnum;
import com.design.museum.enums.UserStatusEnum;
import com.design.museum.common.ErrorCode;
import com.design.museum.dto.LoginRequest;
import com.design.museum.dto.LoginResponse;
import com.design.museum.dto.RegisterRequest;
import com.design.museum.dto.UserQueryRequest;
import com.design.museum.entity.SysUser;
import com.design.museum.exception.BusinessException;
import com.design.museum.mapper.SysUserMapper;
import com.design.museum.service.ISysUserService;
import cn.hutool.crypto.digest.DigestUtil;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 
 * @since 2026-01-19
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements ISysUserService {
    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();

        // 查询用户
        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        SysUser user = this.getOne(queryWrapper);

        // 验证用户是否存在
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_EXIST, "用户不存在");
        }

        // 验证密码（MD5加密）
        String md5Password = DigestUtil.md5Hex(password);
        if (!md5Password.equals(user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.USER_PASSWORD_ERROR, "用户名或密码错误");
        }

        // 验证用户状态
        if (user.getStatus().equals(UserStatusEnum.BANED.getValue())) {
            throw new BusinessException(ErrorCode.USER_DISABLED, "该账户已被封禁，请联系管理员");
        }

        // 使用Sa-Token进行登录
        StpUtil.login(user.getId());
        String token = StpUtil.getTokenValue();

        // 构建返回结果
        LoginResponse response = new LoginResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setAvatarUrl(user.getAvatarUrl());
        response.setRole(user.getRole());
        response.setToken(token);

        return response;
    }

    @Override
    public Long register(RegisterRequest registerRequest) {

        String username = registerRequest.getUsername();
        String password = registerRequest.getPassword();
        String confirmPassword = registerRequest.getConfirmPassword();

        // 验证两次密码是否一致
        if (!password.equals(confirmPassword)) {
            throw new BusinessException(ErrorCode.USER_PASSWORD_NOT_MATCH, "两次密码不一致");
        }

        // 查询用户名是否已存在
        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        SysUser existUser = this.getOne(queryWrapper);
        if (existUser != null) {
            throw new BusinessException(ErrorCode.USER_ALREADY_EXIST, "用户名已存在");
        }

        // 创建新用户
        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPasswordHash(DigestUtil.md5Hex(password));
        user.setPhone(registerRequest.getPhone());
        user.setStatus(UserStatusEnum.NORMAL.getValue());
        user.setRole(UserRoleEnum.USER.getText());

        // 保存用户
        this.save(user);
        return user.getId();
    }

    @Override
    public IPage<SysUser> listUsersByPage(UserQueryRequest userQueryRequest) {
        Long current = userQueryRequest.getCurrent();
        Long size = userQueryRequest.getSize();
        String keyword = userQueryRequest.getKeyword();
        Integer status = userQueryRequest.getStatus();

        // 创建分页对象
        Page<SysUser> page = new Page<>(current, size);

        // 构建查询条件
        QueryWrapper<SysUser> queryWrapper = new QueryWrapper<>();

        // 关键词查询（用户名或昵称）
        if (StringUtils.isNotBlank(keyword)) {
            queryWrapper.and(wrapper -> wrapper
                    .like("username", keyword)
                    .or()
                    .like("nickname", keyword)
            );
        }

        // 状态查询
        if (ObjectUtil.isNotNull(status)) {
            queryWrapper.eq("status", status);
        }

        // 按创建时间降序排序
        queryWrapper.orderByDesc("created_at");

        // 执行分页查询
        return this.page(page, queryWrapper);
    }

    @Override
    public boolean banUser(Long userId) {
        SysUser user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_EXIST, "用户不存在");
        }
        user.setStatus(UserStatusEnum.BANED.getValue());
        return updateById(user);
    }

    @Override
    public boolean unbanUser(Long userId) {
        SysUser user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_EXIST, "用户不存在");
        }
        user.setStatus(UserStatusEnum.NORMAL.getValue());
        boolean result = updateById(user);
        return result;
    }
}
