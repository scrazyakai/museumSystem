package com.design.museum.vo;

import lombok.Data;

import java.util.List;

/**
 * 批量预约结果
 */
@Data
public class BookingBatchResultVO {

    /**
     * 成功数量
     */
    private Integer successCount;

    /**
     * 失败数量
     */
    private Integer failCount;

    /**
     * 成功的预约列表
     */
    private List<BookingVO> successList;

    /**
     * 失败的用户ID列表
     */
    private List<Long> failedUserIds;

    /**
     * 失败原因
     */
    private String errorMessage;
}
