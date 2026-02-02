package com.design.museum.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.design.museum.common.BaseResponse;
import com.design.museum.common.ResultUtils;
import com.design.museum.dto.BookingCancelRequest;
import com.design.museum.dto.BookingCreateRequest;
import com.design.museum.dto.BookingRescheduleRequest;
import com.design.museum.service.IVisitBookingService;
import com.design.museum.vo.BookingVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 预约控制器（用户端）
 */
@Tag(name = "预约管理（用户端）")
@RestController
@RequestMapping("/bookings")
@Slf4j
public class BookingController {

    @Resource
    private IVisitBookingService visitBookingService;

    /**
     * 创建预约
     */
    @Operation(summary = "创建预约")
    @SaCheckLogin
    @PostMapping("/create")
    public BaseResponse<BookingVO> createBooking(@Validated @RequestBody BookingCreateRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        BookingVO bookingVO = visitBookingService.createBooking(request, userId);
        return ResultUtils.success(bookingVO);
    }

    /**
     * 改签预约
     */
    @Operation(summary = "改签预约")
    @SaCheckLogin
    @PostMapping("/reschedule")
    public BaseResponse<BookingVO> rescheduleBooking(@Validated @RequestBody BookingRescheduleRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        BookingVO bookingVO = visitBookingService.rescheduleBooking(request, userId);
        return ResultUtils.success(bookingVO);
    }

    /**
     * 取消预约
     */
    @Operation(summary = "取消预约")
    @SaCheckLogin
    @PostMapping("/cancel")
    public BaseResponse<BookingVO> cancelBooking(@Validated @RequestBody BookingCancelRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        BookingVO bookingVO = visitBookingService.cancelBooking(request, userId);
        return ResultUtils.success(bookingVO);
    }

    /**
     * 我的预约列表（分页）
     */
    @Operation(summary = "我的预约列表")
    @SaCheckLogin
    @GetMapping("/my")
    public BaseResponse<IPage<BookingVO>> myBookings(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status) {
        Long userId = StpUtil.getLoginIdAsLong();
        IPage<BookingVO> result = visitBookingService.myBookings(page, size, status, userId);
        return ResultUtils.success(result);
    }

    /**
     * 预约详情
     */
    @Operation(summary = "预约详情")
    @SaCheckLogin
    @GetMapping("/detail")
    public BaseResponse<BookingVO> getDetail(@RequestParam Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        BookingVO bookingVO = visitBookingService.getDetail(id, userId);
        return ResultUtils.success(bookingVO);
    }
}
