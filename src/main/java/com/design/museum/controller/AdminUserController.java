package com.design.museum.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.design.museum.annotation.AdminAuth;
import com.design.museum.common.BaseResponse;
import com.design.museum.common.ResultUtils;
import com.design.museum.dto.UserQueryRequest;
import com.design.museum.entity.SysUser;
import com.design.museum.service.ISysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

/**
 * 管理员控制器
 */
@RestController
@RequestMapping("/admin/users")
@Tag(name = "管理员接口", description = "管理员相关接口")
@Validated
public class AdminUserController {

    @Resource
    private ISysUserService sysUserService;

    /**
     * 分页查询用户列表
     *
     * @param userQueryRequest 查询请求
     * @return 用户分页列表
     */
    @GetMapping
    @AdminAuth
    @Operation(summary = "分页查询用户列表", description = "管理员分页查询用户列表")
    public BaseResponse<IPage<SysUser>> listUsers(UserQueryRequest userQueryRequest) {
        IPage<SysUser> page = sysUserService.listUsersByPage(userQueryRequest);
        return ResultUtils.success(page);
    }

    /**
     * 获取用户详情
     *
     * @param id 用户ID
     * @return 用户详情
     */
    @GetMapping("/{id}")
    @AdminAuth
    @Operation(summary = "获取用户详情", description = "根据用户ID获取用户详细信息")
    public BaseResponse<SysUser> getUserDetail(
            @Parameter(description = "用户ID") @PathVariable("id") Long id) {
        SysUser user = sysUserService.getById(id);
        return ResultUtils.success(user);
    }

    /**
     * 禁用用户
     *
     * @param id 用户ID
     * @return 操作结果
     */
    @PutMapping("/{id}/ban")
    @AdminAuth
    @Operation(summary = "禁用用户", description = "禁用指定用户")
    public BaseResponse<Boolean> banUser(
            @Parameter(description = "用户ID") @PathVariable("id") Long id) {
        boolean result = sysUserService.banUser(id);
        return ResultUtils.success("封禁成功",result);
    }

    /**
     * 解禁用户
     *
     * @param id 用户ID
     * @return 操作结果
     */
    @PutMapping("/{id}/unban")
    @AdminAuth
    @Operation(summary = "解禁用户", description = "解禁指定用户")
    public BaseResponse<Boolean> unbanUser(
            @Parameter(description = "用户ID") @PathVariable("id") Long id) {
        boolean result =  sysUserService.unbanUser(id);
        return ResultUtils.success("解禁成功",result);
    }

    /**
     * 导出用户列表
     *
     * @param response HTTP响应
     * @param keyword 关键词
     * @param status 状态
     * @throws IOException IO异常
     */
    @GetMapping("/export")
    @AdminAuth
    @Operation(summary = "导出用户列表", description = "导出用户列表到Excel")
    public void exportUsers(
            HttpServletResponse response,
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status) throws IOException {

        // 检查是否登录
        if (!StpUtil.isLogin()) {
            response.setStatus(401);
            response.getWriter().write("未登录");
            return;
        }

        // 构建查询请求
        UserQueryRequest userQueryRequest = new UserQueryRequest();
        userQueryRequest.setKeyword(keyword);
        userQueryRequest.setStatus(status);
        userQueryRequest.setCurrent(1L);
        userQueryRequest.setSize(10000L); // 设置较大的分页，导出所有数据

        // 查询用户列表
        IPage<SysUser> page = sysUserService.listUsersByPage(userQueryRequest);
        List<SysUser> userList = page.getRecords();

        // 设置响应头
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        
        // 这里URLEncoder.encode可以防止中文乱码
        String fileName = URLEncoder.encode("用户列表", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        // 使用EasyExcel导出
        EasyExcel.write(response.getOutputStream(), SysUser.class)
                .sheet("用户列表")
                .doWrite(userList);
    }
}
