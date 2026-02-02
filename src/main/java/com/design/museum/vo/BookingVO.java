package com.design.museum.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 预约视图对象
 */
@Data
public class BookingVO {

    /**
     * 预约ID
     */
    private Long id;

    /**
     * 参观日期
     */
    private LocalDate visitDate;

    /**
     * 票号
     */
    private String ticketCode;

    /**
     * 状态：1已预约 2已取消 3已改签 4已核验 5已过期
     */
    private Integer status;

    /**
     * 取消原因
     */
    private String cancelReason;

    /**
     * 核验时间
     */
    private LocalDateTime verifyTime;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
