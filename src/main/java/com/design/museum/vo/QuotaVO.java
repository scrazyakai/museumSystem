package com.design.museum.vo;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 配额视图对象
 */
@Data
public class QuotaVO {

    /**
     * 配额ID
     */
    private Long id;

    /**
     * 参观日期
     */
    private LocalDate visitDate;

    /**
     * 总容量
     */
    private Integer capacity;

    /**
     * 已预约数量
     */
    private Long reservedCount;

    /**
     * 剩余数量
     */
    private Long remainingCount;

    /**
     * 状态：1可预约 0停用
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
