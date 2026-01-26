package com.design.museum.interceptor;

import cn.dev33.satoken.stp.StpUtil;
import com.design.museum.annotation.AdminAuth;
import com.design.museum.common.ErrorCode;
import com.design.museum.entity.SysUser;
import com.design.museum.exception.BusinessException;
import com.design.museum.service.ISysUserService;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 管理员权限拦截器
 */
@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    @Resource
    private ISysUserService sysUserService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 检查是否是方法处理器
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        
        // 检查方法或类上是否有AdminAuth注解
        AdminAuth methodAnnotation = handlerMethod.getMethodAnnotation(AdminAuth.class);
        AdminAuth classAnnotation = handlerMethod.getBeanType().getAnnotation(AdminAuth.class);
        
        if (methodAnnotation == null && classAnnotation == null) {
            return true;
        }

        // 检查是否登录
        if (!StpUtil.isLogin()) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR, "未登录");
        }

        // 获取当前用户ID
        Long userId = StpUtil.getLoginIdAsLong();
        
        // 查询用户信息
        SysUser user = sysUserService.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_EXIST, "用户不存在");
        }

        // 检查是否是管理员
        if (!"ADMIN".equals(user.getRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限访问");
        }

        return true;
    }
}
