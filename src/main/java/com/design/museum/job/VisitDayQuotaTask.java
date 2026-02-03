package com.design.museum.job;

import com.design.museum.service.IVisitDayQuotaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 参观配额定时任务
 * 每天0点执行：创建当天配额，删除过去日期的配额
 *
 * @author
 * @since 2026-02-03
 */
@Slf4j
@Component
public class VisitDayQuotaTask {

    @Autowired
    private IVisitDayQuotaService visitDayQuotaService;

    /**
     * 每天0点0分0秒执行
     * 创建未来7天的配额（包含今天）
     * 逻辑删除过去日期的配额
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void manageDailyQuota() {
        log.info("开始执行参观配额定时任务...");

        try {
            // 创建未来7天的配额
            int createdCount = visitDayQuotaService.createFutureQuotas(7);
            log.info("成功创建 {} 天的参观配额（包含今天及未来6天），每天容量：2000", createdCount);

            // 逻辑删除过去日期的配额
            visitDayQuotaService.deletePastQuota();
            log.info("成功逻辑删除过去日期的参观配额");

        } catch (Exception e) {
            log.error("执行参观配额定时任务失败", e);
        }

        log.info("参观配额定时任务执行完成");
    }
}
