package com.design.museum.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * 改签预约请求
 */
@Data
public class BookingRescheduleRequest {

    /**
     * 预约ID
     */
    @NotNull(message = "预约ID不能为空")
    private Long bookingId;

    /**
     * 新的参观日期
     */
    @NotNull(message = "新参观日期不能为空")
    private LocalDate newVisitDate;
}
