package com.design.museum.controller;


import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.design.museum.common.BaseResponse;
import com.design.museum.common.ResultUtils;
import com.design.museum.dto.QuotaQueryRequest;
import com.design.museum.dto.QuotaUpdateRequest;
import com.design.museum.service.IVisitDayQuotaService;
import com.design.museum.vo.QuotaVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * <p>
 *  配额管理控制器
 * </p>
 *
 * @author
 * @since 2026-02-03
 *
 **/
@RestController
@RequestMapping("/quota")
@Slf4j
public class VisitQuotaController {

    @Resource
    private IVisitDayQuotaService visitDayQuotaService;

    /**
     * 查看库存信息
     */
    @Operation(summary = "查看库存信息")
    @PostMapping("/info")
    public BaseResponse<QuotaVO> getQuotaInfo(@Validated @RequestBody QuotaQueryRequest request) {
        log.info("接收到查询库存请求: {}", request);
        log.info("visitDate值: {}", request.getVisitDate());
        QuotaVO quotaVO = visitDayQuotaService.getQuotaInfo(request.getVisitDate());
        return ResultUtils.success(quotaVO);
    }

    /**
     * 修改库存容量
     */
    @Operation(summary = "修改库存容量")
    @PostMapping("/update")
    @SaCheckLogin
    @SaCheckRole("ADMIN")
    public BaseResponse<Boolean> updateCapacity(@Validated @RequestBody QuotaUpdateRequest request) {
        boolean result = visitDayQuotaService.updateCapacity(
                request.getVisitDate(),
                request.getCapacity()
        );
        return ResultUtils.success(result);
    }

    /**
     * 手动创建未来N天的配额
     */
    @Operation(summary = "手动创建未来N天的配额")
    @PostMapping("/create")
    @SaCheckLogin
    @SaCheckRole("ADMIN")
    public BaseResponse<String> createFutureQuotas(Integer days) {
        if (days == null || days <= 0 || days > 30) {
            return ResultUtils.error(400, "天数必须在1-30之间");
        }

        int createdCount = visitDayQuotaService.createFutureQuotas(days);
        return ResultUtils.success(String.format("成功创建 %d 天的配额", createdCount));
    }
}
