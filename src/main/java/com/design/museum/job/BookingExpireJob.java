package com.design.museum.job;

import com.design.museum.service.IVisitBookingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 预约过期定时任务
 * 每天 12:00 执行，将过期预约更新为已过期状态
 */
@Component
@Slf4j
public class BookingExpireJob {

    @Resource
    private IVisitBookingService visitBookingService;

    /**
     * 每天 12:00:00 执行
     * cron 表达式：秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 0 12 * * ?")
    public void updateExpiredBookings() {
        log.info("定时任务开始：批量更新过期预约状态");
        try {
            Integer count = visitBookingService.updateExpiredBookings();
            log.info("定时任务完成：共更新 {} 条过期预约", count);
        } catch (Exception e) {
            log.error("定时任务执行失败", e);
        }
    }
}
