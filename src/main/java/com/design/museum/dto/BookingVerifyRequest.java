package com.design.museum.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 核验预约请求
 */
@Data
public class BookingVerifyRequest {

    /**
     * 票号
     */
    @NotBlank(message = "票号不能为空")
    private String ticketCode;
}
