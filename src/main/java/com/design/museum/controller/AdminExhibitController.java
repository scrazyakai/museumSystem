package com.design.museum.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.design.museum.annotation.AdminAuth;
import com.design.museum.common.BaseResponse;
import com.design.museum.common.ResultUtils;
import com.design.museum.dto.ExhibitItemAddRequest;
import com.design.museum.dto.ExhibitItemQueryRequest;
import com.design.museum.dto.ExhibitItemUpdateRequest;
import com.design.museum.entity.ExhibitItem;
import com.design.museum.service.IExhibitItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 管理员展品管理接口
 */
@RestController
@RequestMapping("/admin/items")
@Tag(name = "管理员展品接口", description = "管理员展品管理相关接口")
@Validated
public class AdminExhibitController {

    @Resource
    private IExhibitItemService exhibitItemService;

    /**
     * 添加展品
     *
     * @param request 添加请求
     * @return 展品ID
     */
    @PostMapping
    @AdminAuth
    @Operation(summary = "添加展品", description = "管理员添加新展品")
    public BaseResponse<Long> addExhibitItem(@Validated @RequestBody ExhibitItemAddRequest request) {
        // 从登录信息中获取管理员ID
        Long adminId = StpUtil.getLoginIdAsLong();
        Long itemId = exhibitItemService.addExhibitItem(request, adminId);
        return ResultUtils.success("添加成功", itemId);
    }

    /**
     * 更新展品
     *
     * @param request 更新请求
     * @return 是否成功
     */
    @PutMapping
    @AdminAuth
    @Operation(summary = "更新展品", description = "管理员更新展品信息")
    public BaseResponse<Boolean> updateExhibitItem(@Validated @RequestBody ExhibitItemUpdateRequest request) {
        boolean result = exhibitItemService.updateExhibitItem(request);
        return ResultUtils.success("更新成功", result);
    }

    /**
     * 分页查询展品列表
     *
     * @param request 查询请求
     * @return 展品分页列表
     */
    @GetMapping
    @AdminAuth
    @Operation(summary = "分页查询展品列表", description = "管理员分页查询展品，支持关键词搜索")
    public BaseResponse<Page<ExhibitItem>> listExhibitItems(ExhibitItemQueryRequest request) {
        Page<ExhibitItem> page = exhibitItemService.adminListExhibitItems(request);
        return ResultUtils.success(page);
    }

    /**
     * 下架展品（逻辑删除）
     *
     * @param id 展品ID
     * @return 是否成功
     */
    @PatchMapping("/{id}/down")
    @AdminAuth
    @Operation(summary = "下架展品", description = "管理员下架指定展品（逻辑删除）")
    public BaseResponse<Boolean> downExhibitItem(
            @Parameter(description = "展品ID") @PathVariable("id") Long id) {
        boolean result = exhibitItemService.downExhibitItem(id);
        return ResultUtils.success("下架成功", result);
    }

    /**
     * 恢复展品
     *
     * @param id 展品ID
     * @return 是否成功
     */
    @PatchMapping("/{id}/restore")
    @AdminAuth
    @Operation(summary = "恢复展品", description = "管理员恢复已下架的展品")
    public BaseResponse<Boolean> restoreExhibitItem(
            @Parameter(description = "展品ID") @PathVariable("id") Long id) {
        boolean result = exhibitItemService.restoreExhibitItem(id);
        return ResultUtils.success("恢复成功", result);
    }
}