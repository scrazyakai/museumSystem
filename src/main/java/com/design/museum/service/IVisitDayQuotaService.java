package com.design.museum.service;

import com.design.museum.entity.VisitDayQuota;
import com.design.museum.vo.QuotaVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.time.LocalDate;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author
 * @since 2026-02-03
 */
public interface IVisitDayQuotaService extends IService<VisitDayQuota> {

    /**
     * 创建当天参观配额
     * @return 是否创建成功
     */
    boolean createTodayQuota();

    /**
     * 创建指定日期的配额
     * @param visitDate 参观日期
     * @return 是否创建成功
     */
    boolean createQuotaForDate(LocalDate visitDate);

    /**
     * 创建未来N天的配额
     * @param days 天数
     * @return 创建成功的数量
     */
    int createFutureQuotas(int days);

    /**
     * 逻辑删除过去的参观配额
     */
    void deletePastQuota();

    /**
     * 扣减配额（预约时使用，带行锁）
     * @param visitDate 参观日期
     * @return 是否扣减成功
     */
    boolean decreaseQuota(LocalDate visitDate);

    /**
     * 恢复配额（取消预约时使用）
     * @param visitDate 参观日期
     */
    void increaseQuota(LocalDate visitDate);

    /**
     * 检查配额是否充足
     * @param visitDate 参观日期
     * @return 是否充足
     */
    boolean checkQuotaAvailable(LocalDate visitDate);

    /**
     * 获取指定日期的库存信息（总容量、已预约数、剩余数）
     * @param visitDate 参观日期
     * @return 配额信息
     */
    QuotaVO getQuotaInfo(LocalDate visitDate);

    /**
     * 更新指定日期的库存容量
     * @param visitDate 参观日期
     * @param newCapacity 新的容量
     * @return 是否更新成功
     */
    boolean updateCapacity(LocalDate visitDate, Integer newCapacity);
}
