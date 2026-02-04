package com.design.museum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.design.museum.entity.ExhibitComment;
import com.design.museum.entity.SysUser;
import com.design.museum.entity.VisitBooking;
import com.design.museum.entity.VisitDayQuota;
import com.design.museum.mapper.ExhibitCommentMapper;
import com.design.museum.mapper.SysUserMapper;
import com.design.museum.mapper.VisitBookingMapper;
import com.design.museum.mapper.VisitDayQuotaMapper;
import com.design.museum.service.IDashboardService;
import com.design.museum.vo.DashboardVO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 数据统计服务实现类
 */
@Service
public class DashboardServiceImpl implements IDashboardService {

    @Resource
    private VisitBookingMapper visitBookingMapper;

    @Resource
    private VisitDayQuotaMapper visitDayQuotaMapper;

    @Resource
    private ExhibitCommentMapper exhibitCommentMapper;

    @Resource
    private SysUserMapper sysUserMapper;

    @Override
    public DashboardVO getDashboard() {
        DashboardVO dashboard = new DashboardVO();

        // 获取今天的日期
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.plusDays(1).atStartOfDay();

        // 获取本月的开始时间
        LocalDateTime monthStart = today.withDayOfMonth(1).atStartOfDay();

        // 1. 当天已预约数量（状态为1：已预约）
        Long todayBookedCount = visitBookingMapper.selectCount(
                new LambdaQueryWrapper<VisitBooking>()
                        .eq(VisitBooking::getVisitDate, today)
                        .eq(VisitBooking::getStatus, 1)
                        .eq(VisitBooking::getDeleted, 0)
        );
        dashboard.setTodayBookedCount(todayBookedCount);

        // 2. 当天放票总量
        VisitDayQuota todayQuota = visitDayQuotaMapper.selectOne(
                new LambdaQueryWrapper<VisitDayQuota>()
                        .eq(VisitDayQuota::getVisitDate, today)
                        .eq(VisitDayQuota::getDeleted, 0)
        );
        dashboard.setTodayTotalQuota(todayQuota != null ? todayQuota.getCapacity() : 0);

        // 3. 评论总量（未删除）
        Long totalCommentCount = exhibitCommentMapper.selectCount(
                new LambdaQueryWrapper<ExhibitComment>()
                        .eq(ExhibitComment::getDeleted, 0)
        );
        dashboard.setTotalCommentCount(totalCommentCount);

        // 4. 待审核评论数（状态为0：隐藏）
        Long pendingCommentCount = exhibitCommentMapper.selectCount(
                new LambdaQueryWrapper<ExhibitComment>()
                        .eq(ExhibitComment::getStatus, 0)
                        .eq(ExhibitComment::getDeleted, 0)
        );
        dashboard.setPendingCommentCount(pendingCommentCount);

        // 5. 用户总数
        Long totalUserCount = sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getDeleted, 0)
        );
        dashboard.setTotalUserCount(totalUserCount);

        // 6. 本月预约数
        Long monthlyBookingCount = visitBookingMapper.selectCount(
                new LambdaQueryWrapper<VisitBooking>()
                        .ge(VisitBooking::getCreatedAt, monthStart)
                        .eq(VisitBooking::getDeleted, 0)
        );
        dashboard.setMonthlyBookingCount(monthlyBookingCount);

        // 7. 今日核验数（状态为4：已核验）
        Long todayVerifiedCount = visitBookingMapper.selectCount(
                new LambdaQueryWrapper<VisitBooking>()
                        .ge(VisitBooking::getVerifyTime, todayStart)
                        .lt(VisitBooking::getVerifyTime, todayEnd)
                        .eq(VisitBooking::getStatus, 4)
                        .eq(VisitBooking::getDeleted, 0)
        );
        dashboard.setTodayVerifiedCount(todayVerifiedCount);

        // 8. 今日取消数（状态为2：已取消）
        Long todayCancelledCount = visitBookingMapper.selectCount(
                new LambdaQueryWrapper<VisitBooking>()
                        .ge(VisitBooking::getCreatedAt, todayStart)
                        .lt(VisitBooking::getCreatedAt, todayEnd)
                        .eq(VisitBooking::getStatus, 2)
                        .eq(VisitBooking::getDeleted, 0)
        );
        dashboard.setTodayCancelledCount(todayCancelledCount);

        return dashboard;
    }
}
