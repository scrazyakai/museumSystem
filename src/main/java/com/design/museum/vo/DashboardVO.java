package com.design.museum.vo;

import lombok.Data;

/**
 * 管理员数据统计视图对象
 */
@Data
public class DashboardVO {

    /**
     * 当天已预约数量
     */
    private Long todayBookedCount;

    /**
     * 当天放票总量
     */
    private Integer todayTotalQuota;

    /**
     * 评论总量
     */
    private Long totalCommentCount;

    /**
     * 待审核评论数
     */
    private Long pendingCommentCount;

    /**
     * 用户总数
     */
    private Long totalUserCount;

    /**
     * 本月预约数
     */
    private Long monthlyBookingCount;

    /**
     * 今日核验数
     */
    private Long todayVerifiedCount;

    /**
     * 今日取消数
     */
    private Long todayCancelledCount;
}
