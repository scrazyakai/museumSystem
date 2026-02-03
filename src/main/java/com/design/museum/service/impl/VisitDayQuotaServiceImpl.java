package com.design.museum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.design.museum.common.ErrorCode;
import com.design.museum.entity.VisitBooking;
import com.design.museum.entity.VisitDayQuota;
import com.design.museum.enums.QuotaStatusEnum;
import com.design.museum.exception.BusinessException;
import com.design.museum.mapper.VisitBookingMapper;
import com.design.museum.mapper.VisitDayQuotaMapper;
import com.design.museum.service.IVisitDayQuotaService;
import com.design.museum.vo.QuotaVO;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author
 * @since 2026-02-03
 */
@Service
public class VisitDayQuotaServiceImpl extends ServiceImpl<VisitDayQuotaMapper, VisitDayQuota> implements IVisitDayQuotaService {
    private static final int capacity = 2000;

    @Resource
    private VisitBookingMapper visitBookingMapper;

    @Override
    public boolean createTodayQuota() {
        return createQuotaForDate(LocalDate.now());
    }

    @Override
    public boolean createQuotaForDate(LocalDate visitDate) {
        // 检查该日期是否已经存在配额记录
        LambdaQueryWrapper<VisitDayQuota> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(VisitDayQuota::getVisitDate, visitDate)
                .eq(VisitDayQuota::getDeleted, 0);
        long count = this.count(queryWrapper);

        // 如果该日期已经存在配额记录，则不重复创建
        if (count > 0) {
            return false;
        }

        // 创建配额记录
        VisitDayQuota quota = new VisitDayQuota();
        quota.setVisitDate(visitDate);
        quota.setCapacity(capacity);
        quota.setStatus(QuotaStatusEnum.NORMAL.getValue()); // 1可预约
        quota.setDeleted(0); // 0正常
        quota.setCreatedAt(LocalDateTime.now());
        quota.setUpdatedAt(LocalDateTime.now());

        return this.save(quota);
    }

    @Override
    public int createFutureQuotas(int days) {
        int createdCount = 0;
        LocalDate today = LocalDate.now();

        for (int i = 0; i < days; i++) {
            LocalDate targetDate = today.plusDays(i);
            boolean created = createQuotaForDate(targetDate);
            if (created) {
                createdCount++;
            }
        }

        return createdCount;
    }

    @Override
    public void deletePastQuota() {
        LocalDate today = LocalDate.now();

        // 逻辑删除过去日期的配额记录
        LambdaUpdateWrapper<VisitDayQuota> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.lt(VisitDayQuota::getVisitDate, today)
                .eq(VisitDayQuota::getDeleted, 0)
                .set(VisitDayQuota::getDeleted, 1)
                .set(VisitDayQuota::getUpdatedAt, LocalDateTime.now());

        this.update(updateWrapper);
    }

    @Override
    public boolean checkQuotaAvailable(LocalDate visitDate) {
        // 查询配额记录（使用悲观锁）
        LambdaQueryWrapper<VisitDayQuota> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(VisitDayQuota::getVisitDate, visitDate)
                .eq(VisitDayQuota::getDeleted, 0)
                .eq(VisitDayQuota::getStatus, QuotaStatusEnum.NORMAL.getValue())
                .last("FOR UPDATE");

        VisitDayQuota quota = this.getOne(queryWrapper);
        if (quota == null) {
            throw new BusinessException(ErrorCode.QUOTA_NOT_EXIST, "该日期配额不存在");
        }

        // 统计该日期的有效预约数量（状态为1、3、4的预约）
        LambdaQueryWrapper<VisitBooking> bookingQuery = new LambdaQueryWrapper<>();
        bookingQuery.eq(VisitBooking::getVisitDate, visitDate)
                .eq(VisitBooking::getDeleted, 0)
                .in(VisitBooking::getStatus, 1, 3, 4); // 1已预约、3已改签、4已核验

        long reservedCount = visitBookingMapper.selectCount(bookingQuery);

        // 检查是否有剩余名额
        return reservedCount < quota.getCapacity();
    }

    @Override
    public boolean decreaseQuota(LocalDate visitDate) {
        // 查询配额记录（使用悲观锁）
        LambdaQueryWrapper<VisitDayQuota> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(VisitDayQuota::getVisitDate, visitDate)
                .eq(VisitDayQuota::getDeleted, 0)
                .eq(VisitDayQuota::getStatus, QuotaStatusEnum.NORMAL.getValue())
                .last("FOR UPDATE");

        VisitDayQuota quota = this.getOne(queryWrapper);
        if (quota == null) {
            throw new BusinessException(ErrorCode.QUOTA_NOT_EXIST, "该日期配额不存在");
        }

        // 统计该日期的有效预约数量
        LambdaQueryWrapper<VisitBooking> bookingQuery = new LambdaQueryWrapper<>();
        bookingQuery.eq(VisitBooking::getVisitDate, visitDate)
                .eq(VisitBooking::getDeleted, 0)
                .in(VisitBooking::getStatus, 1, 3, 4);

        long reservedCount = visitBookingMapper.selectCount(bookingQuery);

        // 检查是否已满
        if (reservedCount >= quota.getCapacity()) {
            throw new BusinessException(ErrorCode.QUOTA_FULL, "该日期预约已满");
        }

        // 配额检查通过，不需要扣减字段（预约记录创建后自然占用名额）
        return true;
    }

    @Override
    public void increaseQuota(LocalDate visitDate) {
        // 取消预约时不需要恢复配额字段（预约记录状态变为2后，统计时不再计入）
        // 这个方法保留为空实现，用于未来可能的扩展
    }

    @Override
    public QuotaVO getQuotaInfo(LocalDate visitDate) {
        // 查询配额记录
        LambdaQueryWrapper<VisitDayQuota> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(VisitDayQuota::getVisitDate, visitDate)
                .eq(VisitDayQuota::getDeleted, 0);

        VisitDayQuota quota = this.getOne(queryWrapper);
        if (quota == null) {
            throw new BusinessException(ErrorCode.QUOTA_NOT_EXIST, "该日期配额不存在");
        }

        // 统计该日期的有效预约数量
        LambdaQueryWrapper<VisitBooking> bookingQuery = new LambdaQueryWrapper<>();
        bookingQuery.eq(VisitBooking::getVisitDate, visitDate)
                .eq(VisitBooking::getDeleted, 0)
                .in(VisitBooking::getStatus, 1, 3, 4); // 1已预约、3已改签、4已核验

        long reservedCount = visitBookingMapper.selectCount(bookingQuery);

        // 创建VO对象
        QuotaVO vo = new QuotaVO();
        vo.setId(quota.getId());
        vo.setVisitDate(quota.getVisitDate());
        vo.setCapacity(quota.getCapacity());
        vo.setReservedCount(reservedCount);
        vo.setRemainingCount(quota.getCapacity() - reservedCount);
        vo.setStatus(quota.getStatus());
        vo.setCreatedAt(quota.getCreatedAt());
        vo.setUpdatedAt(quota.getUpdatedAt());

        return vo;
    }

    @Override
    public boolean updateCapacity(LocalDate visitDate, Integer newCapacity) {
        if (newCapacity == null || newCapacity < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "容量参数无效");
        }

        // 查询配额记录
        LambdaQueryWrapper<VisitDayQuota> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(VisitDayQuota::getVisitDate, visitDate)
                .eq(VisitDayQuota::getDeleted, 0)
                .last("FOR UPDATE"); // 使用悲观锁

        VisitDayQuota quota = this.getOne(queryWrapper);
        if (quota == null) {
            throw new BusinessException(ErrorCode.QUOTA_NOT_EXIST, "该日期配额不存在");
        }

        // 统计当前已预约数量
        LambdaQueryWrapper<VisitBooking> bookingQuery = new LambdaQueryWrapper<>();
        bookingQuery.eq(VisitBooking::getVisitDate, visitDate)
                .eq(VisitBooking::getDeleted, 0)
                .in(VisitBooking::getStatus, 1, 3, 4);

        long reservedCount = visitBookingMapper.selectCount(bookingQuery);

        // 检查新容量是否小于已预约数
        if (newCapacity < reservedCount) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR,
                    String.format("新容量不能小于已预约数量，当前已预约：%d", reservedCount));
        }

        // 更新容量
        quota.setCapacity(newCapacity);
        quota.setUpdatedAt(LocalDateTime.now());

        return this.updateById(quota);
    }
}
