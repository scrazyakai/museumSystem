package com.design.museum.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.design.museum.dto.*;
import com.design.museum.entity.VisitBooking;
import com.design.museum.vo.BookingBatchResultVO;
import com.design.museum.vo.BookingVO;

import java.time.LocalDate;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author
 * @since 2026-01-29
 */
public interface IVisitBookingService extends IService<VisitBooking> {

    /**
     * 创建预约
     *
     * @param request 创建预约请求
     * @param userId  用户ID
     * @return 预约信息
     */
    BookingVO createBooking(BookingCreateRequest request, Long userId);

    /**
     * 批量创建预约（团体预约）
     *
     * @param request 批量创建预约请求
     * @return 批量预约结果
     */
    BookingBatchResultVO createBatchBookings(BookingBatchCreateRequest request);

    /**
     * 改签预约
     *
     * @param request 改签请求
     * @param userId  用户ID
     * @return 预约信息
     */
    BookingVO rescheduleBooking(BookingRescheduleRequest request, Long userId);

    /**
     * 取消预约
     *
     * @param request 取消请求
     * @param userId  用户ID
     * @return 预约信息
     */
    BookingVO cancelBooking(BookingCancelRequest request, Long userId);

    /**
     * 我的预约列表（分页）
     *
     * @param page     页码
     * @param size     每页大小
     * @param status   状态（可选）
     * @param userId   用户ID
     * @return 分页结果
     */
    IPage<BookingVO> myBookings(Integer page, Integer size, Integer status, Long userId);

    /**
     * 预约详情
     *
     * @param id     预约ID
     * @param userId 用户ID
     * @return 预约信息
     */
    BookingVO getDetail(Long id, Long userId);

    /**
     * 核验预约（管理员）
     *
     * @param request 核验请求
     * @return 核验结果
     */
    BookingVO verifyBooking(BookingVerifyRequest request);

    /**
     * 管理员分页查询预约列表
     *
     * @param page      页码
     * @param size      每页数量
     * @param visitDate 预约日期（可选）
     * @param status    预约状态（可选）
     * @param ticketCode 票号（可选）
     * @return 分页结果
     */
    IPage<BookingVO> queryBookings(Integer page, Integer size, LocalDate visitDate, Integer status, String ticketCode);

    /**
     * 批量更新过期预约状态
     *
     * @return 更新数量
     */
    Integer updateExpiredBookings();
}
