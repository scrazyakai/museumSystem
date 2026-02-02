package com.design.museum.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.design.museum.common.BaseResponse;
import com.design.museum.common.ResultUtils;
import com.design.museum.dto.BookingQueryRequest;
import com.design.museum.dto.BookingVerifyRequest;
import com.design.museum.service.IVisitBookingService;
import com.design.museum.vo.BookingVO;
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
 * 预约管理控制器（管理员端）
 */
@Tag(name = "预约管理（管理员端）")
@RestController
@RequestMapping("/admin/bookings")
@Slf4j
public class AdminBookingController {

    @Resource
    private IVisitBookingService visitBookingService;

    /**
     * 核验入馆
     */
    @Operation(summary = "核验入馆")
    @SaCheckLogin
    @SaCheckRole("ADMIN")
    @PostMapping("/verify")
    public BaseResponse<BookingVO> verifyBooking(@Validated @RequestBody BookingVerifyRequest request) {
        BookingVO bookingVO = visitBookingService.verifyBooking(request);
        return ResultUtils.success(bookingVO);
    }

    /**
     * 分页查询预约列表
     */
    @Operation(summary = "分页查询预约列表")
    @SaCheckLogin
    @SaCheckRole("ADMIN")
    @PostMapping("/query")
    public BaseResponse<IPage<BookingVO>> queryBookings(@RequestBody BookingQueryRequest request) {
        IPage<BookingVO> result = visitBookingService.queryBookings(
                request.getPage(),
                request.getSize(),
                request.getVisitDate(),
                request.getStatus(),
                request.getTicketCode()
        );
        return ResultUtils.success(result);
    }
}
