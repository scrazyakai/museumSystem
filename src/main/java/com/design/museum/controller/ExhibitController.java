package com.design.museum.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.design.museum.common.BaseResponse;
import com.design.museum.common.ResultUtils;
import com.design.museum.dto.ExhibitItemQueryRequest;
import com.design.museum.service.IExhibitItemService;
import com.design.museum.vo.ExhibitItemVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 用户端展品接口
 */
@RestController
@RequestMapping("/items")
@Tag(name = "展品接口", description = "用户端展品浏览相关接口")
@Validated
public class ExhibitController {

    @Resource
    private IExhibitItemService exhibitItemService;

    /**
     * 分页查询展品列表
     *
     * @param request 查询请求
     * @return 展品分页列表
     */
    @GetMapping
    @Operation(summary = "分页查询展品列表", description = "用户分页查询展品，只返回上架且在展示期内的展品")
    public BaseResponse<Page<ExhibitItemVO>> listExhibitItems(ExhibitItemQueryRequest request) {
        Page<ExhibitItemVO> page = exhibitItemService.userListExhibitItems(request);
        return ResultUtils.success(page);
    }

    /**
     * 获取展品详情
     *
     * @param id 展品ID
     * @return 展品详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取展品详情", description = "根据ID获取展品详细信息")
    public BaseResponse<ExhibitItemVO> getExhibitItem(
            @Parameter(description = "展品ID") @PathVariable("id") Long id) {
        ExhibitItemVO vo = exhibitItemService.getExhibitItemVO(id);
        if (vo == null) {
            return ResultUtils.error(404, "展品不存在");
        }
        return ResultUtils.success(vo);
    }
}