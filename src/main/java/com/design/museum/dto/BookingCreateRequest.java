package com.design.museum.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * 创建预约请求
 */
@Data
public class BookingCreateRequest {

    /**
     * 参观日期
     */
    @NotNull(message = "参观日期不能为空")
    private LocalDate visitDate;
}
