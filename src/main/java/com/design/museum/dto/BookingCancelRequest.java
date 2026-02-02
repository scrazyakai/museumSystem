package com.design.museum.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 取消预约请求
 */
@Data
public class BookingCancelRequest {

    /**
     * 预约ID
     */
    @NotNull(message = "预约ID不能为空")
    private Long bookingId;

    /**
     * 取消原因（可选）
     */
    private String cancelReason;
}
