package com.design.museum.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.design.museum.common.BaseResponse;
import com.design.museum.common.ErrorCode;
import com.design.museum.common.ResultUtils;
import com.design.museum.dto.LoginRequest;
import com.design.museum.dto.LoginResponse;
import com.design.museum.dto.RegisterRequest;
import com.design.museum.entity.SysUser;
import com.design.museum.exception.BusinessException;
import com.design.museum.service.ISysUserService;
import com.design.museum.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.BeanUtils;
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
    @GetMapping("/detail")
    @Operation(summary = "获取当前用户信息", description = "获取用户详细信息")
    public BaseResponse<UserVO> getUserDetail(){
        Long userId = StpUtil.getLoginIdAsLong();
        SysUser user = sysUserService.getById(userId);
        if(user == null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"用户信息不存在");
        }
        UserVO userVO = new UserVO();
        //手机号脱敏
        if(user != null && user.getPhone() != null) {
            String phone = user.getPhone();
            int start = 0;
            int end = 3;
            int firstBegin = phone.length() - 3;
            String maskPhone = phone.substring(start, end)  +"*".repeat(firstBegin - end)
                    +phone.substring(firstBegin);
            user.setPhone(maskPhone);
        }
        //身份证号脱敏
        if(user != null && user.getIdNo() != null) {
            String idNo = user.getIdNo();
            // 第一段保留的开始
            int firstStart = 0;
            // 第一段保留的结束
            int firstEnd = 6;
            // 第二段保留的起始位置
            int secondStart = idNo.length() - 4;
            String maskIdNo = idNo.substring(firstStart, firstEnd) + "*".repeat(secondStart - firstEnd)
                    +idNo.substring(secondStart);
            user.setIdNo(maskIdNo);
        }
        if (user != null && user.getRealName() != null) {

            String realName = user.getRealName();
            int len = realName.length();
            String maskName;
            switch(realName.length()){
                case 2:
                    maskName = realName.substring(0, 1) + "*";
                    break;
                default:
                    maskName = realName.substring(0, 1)
                            + "*".repeat(len - 2)
                            + realName.substring(len - 1);
                    break;
            }
            user.setRealName(maskName);
        }
        BeanUtils.copyProperties(user, userVO);
        return ResultUtils.success(userVO);
    }

}
