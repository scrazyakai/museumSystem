package com.design.museum.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 预约查询请求（管理员）
 */
@Data
public class BookingQueryRequest {

    /**
     * 页码
     */
    private Integer page = 1;

    /**
     * 每页数量
     */
    private Integer size = 10;

    /**
     * 预约日期（可选）
     */
    private LocalDate visitDate;

    /**
     * 预约状态（可选）：1已预约 2已取消 3已改签 4已核验 5已过期
     */
    private Integer status;

    /**
     * 票号（可选，模糊查询）
     */
    private String ticketCode;
}
