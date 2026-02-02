package com.design.museum.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.design.museum.aop.Notice;
import com.design.museum.common.ErrorCode;
import com.design.museum.dto.BookingCancelRequest;
import com.design.museum.dto.BookingCreateRequest;
import com.design.museum.dto.BookingRescheduleRequest;
import com.design.museum.dto.BookingVerifyRequest;
import com.design.museum.entity.SysUser;
import com.design.museum.entity.VisitBooking;
import com.design.museum.exception.BusinessException;
import com.design.museum.mapper.VisitBookingMapper;
import com.design.museum.service.ISysUserService;
import com.design.museum.service.IVisitBookingService;
import com.design.museum.vo.BookingVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 预约服务实现类
 */
@Service
@Slf4j
public class VisitBookingServiceImpl extends ServiceImpl<VisitBookingMapper, VisitBooking> implements IVisitBookingService {

    @Resource
    private ISysUserService sysUserService;

    @Override
    @Notice(title = "预约成功", message = "预约成功：#{#result.visitDate.toString()}，票号：#{#result.ticketCode}")
    @Transactional(rollbackFor = Exception.class)
    public BookingVO createBooking(BookingCreateRequest request, Long userId) {
        // 1. 校验实名信息
        checkRealName(userId);

        LocalDate visitDate = request.getVisitDate();
        LocalDate today = LocalDate.now();

        // 2. 校验预约日期
        if (visitDate.isBefore(today)) {
            throw new BusinessException(ErrorCode.BOOKING_DATE_INVALID, "预约日期不能早于今天");
        }

        // 3. 检查该日期是否已有有效预约
        QueryWrapper<VisitBooking> existQuery = new QueryWrapper<>();
        existQuery.eq("user_id", userId)
                .eq("visit_date", visitDate)
                .eq("deleted", 0)
                .in("status", 1, 3, 4); // 已预约、已改签、已核验
        Long count = this.baseMapper.selectCount(existQuery);
        if (count > 0) {
            throw new BusinessException(ErrorCode.BOOKING_ALREADY_EXISTS, "该日期已有预约，无法重复预约");
        }

        // 4. 生成票号（使用 UUID 去掉横线）
        String ticketCode = java.util.UUID.randomUUID().toString().replace("-", "");

        // 5. 创建预约记录
        VisitBooking booking = new VisitBooking();
        booking.setUserId(userId);
        booking.setVisitDate(visitDate);
        booking.setTicketCode(ticketCode);
        booking.setStatus(1); // 1已预约
        booking.setDeleted(0);
        booking.setCreatedAt(LocalDateTime.now());
        booking.setUpdatedAt(LocalDateTime.now());

        this.baseMapper.insert(booking);

        // 6. 返回结果
        return toVO(booking);
    }

    @Override
    @Notice(title = "改签成功", message = "改签成功，新参观日期：#{#result.visitDate.toString()}，票号：#{#result.ticketCode}")
    @Transactional(rollbackFor = Exception.class)
    public BookingVO rescheduleBooking(BookingRescheduleRequest request, Long userId) {
        // 1. 校验实名信息
        checkRealName(userId);

        Long bookingId = request.getBookingId();
        LocalDate newVisitDate = request.getNewVisitDate();
        LocalDate today = LocalDate.now();

        // 2. 校验新日期
        if (newVisitDate.isBefore(today)) {
            throw new BusinessException(ErrorCode.BOOKING_DATE_INVALID, "新预约日期不能早于今天");
        }

        // 3. 查询原预约
        VisitBooking booking = this.baseMapper.selectById(bookingId);
        if (booking == null || booking.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.BOOKING_NOT_FOUND);
        }

        // 4. 校验是否属于当前用户
        if (!booking.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权操作此预约");
        }

        // 5. 校验预约状态（仅允许 status=1 的预约改签）
        if (booking.getStatus() != 1) {
            throw new BusinessException(ErrorCode.BOOKING_NOT_ALLOW_RESCHEDULE, "该预约不允许改签");
        }

        // 6. 校验是否过期
        if (booking.getVisitDate().isBefore(today)) {
            throw new BusinessException(ErrorCode.BOOKING_EXPIRED, "预约已过期，无法改签");
        }

        // 7. 检查是否已改签过一次（每日一次限制）
        QueryWrapper<VisitBooking> rescheduleQuery = new QueryWrapper<>();
        rescheduleQuery.eq("user_id", userId)
                .eq("deleted", 0)
                .eq("status", 3) // 3已改签
                .apply("DATE(updated_at) = CURDATE()");
        Long rescheduledCount = this.baseMapper.selectCount(rescheduleQuery);
        if (rescheduledCount > 0) {
            throw new BusinessException(ErrorCode.BOOKING_RESCHEDULE_LIMIT);
        }

        // 8. 检查新日期是否已有预约
        QueryWrapper<VisitBooking> existQuery = new QueryWrapper<>();
        existQuery.eq("user_id", userId)
                .eq("visit_date", newVisitDate)
                .eq("deleted", 0)
                .in("status", 1, 3, 4)
                .ne("id", bookingId);
        Long existCount = this.baseMapper.selectCount(existQuery);
        if (existCount > 0) {
            throw new BusinessException(ErrorCode.BOOKING_ALREADY_EXISTS, "该日期已有预约");
        }

        // 9. 更新预约
        booking.setVisitDate(newVisitDate);
        booking.setStatus(3); // 3已改签
        booking.setUpdatedAt(LocalDateTime.now());
        this.baseMapper.updateById(booking);

        return toVO(booking);
    }

    @Override
    @Notice(title = "取消成功", message = "预约已取消，参观日期：#{#result.visitDate.toString()}，票号：#{#result.ticketCode}")
    @Transactional(rollbackFor = Exception.class)
    public BookingVO cancelBooking(BookingCancelRequest request, Long userId) {
        Long bookingId = request.getBookingId();
        String cancelReason = request.getCancelReason();
        LocalDate today = LocalDate.now();

        // 1. 查询预约
        VisitBooking booking = this.baseMapper.selectById(bookingId);
        if (booking == null || booking.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.BOOKING_NOT_FOUND);
        }

        // 2. 校验是否属于当前用户
        if (!booking.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权操作此预约");
        }

        // 3. 校验预约状态（仅允许 status=1 或 status=3 的预约取消）
        if (booking.getStatus() != 1 && booking.getStatus() != 3) {
            throw new BusinessException(ErrorCode.BOOKING_STATUS_ERROR, "该预约状态不允许取消");
        }

        // 4. 过期的预约不允许取消
        if (booking.getVisitDate().isBefore(today)) {
            throw new BusinessException(ErrorCode.BOOKING_EXPIRED, "预约已过期，无需取消");
        }

        // 5. 更新预约状态
        booking.setStatus(2); // 2已取消
        booking.setCancelReason(cancelReason);
        booking.setUpdatedAt(LocalDateTime.now());
        this.baseMapper.updateById(booking);

        return toVO(booking);
    }

    @Override
    public IPage<BookingVO> myBookings(Integer page, Integer size, Integer status, Long userId) {
        // 1. 构建分页对象
        Page<VisitBooking> pageParam = new Page<>(page, size);

        // 2. 构建查询条件
        QueryWrapper<VisitBooking> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .eq("deleted", 0)
                .orderByDesc("created_at");

        // 3. 状态过滤（可选）
        if (status != null) {
            queryWrapper.eq("status", status);
        }

        // 4. 查询分页
        IPage<VisitBooking> resultPage = this.baseMapper.selectPage(pageParam, queryWrapper);

        // 5. 转换为 VO
        return resultPage.convert(this::toVO);
    }

    @Override
    public BookingVO getDetail(Long id, Long userId) {
        // 1. 查询预约
        VisitBooking booking = this.baseMapper.selectById(id);
        if (booking == null || booking.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.BOOKING_NOT_FOUND);
        }

        // 2. 校验是否属于当前用户
        if (!booking.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权查看此预约");
        }

        return toVO(booking);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BookingVO verifyBooking(BookingVerifyRequest request) {
        String ticketCode = request.getTicketCode();
        LocalDate today = LocalDate.now();

        // 1. 查询预约
        QueryWrapper<VisitBooking> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("ticket_code", ticketCode)
                .eq("deleted", 0);
        VisitBooking booking = this.baseMapper.selectOne(queryWrapper);
        if (booking == null) {
            throw new BusinessException(ErrorCode.BOOKING_NOT_FOUND, "票号不存在");
        }

        // 2. 校验预约状态（仅允许 status=1 或 status=3 的预约核验）
        if (booking.getStatus() != 1 && booking.getStatus() != 3) {
            throw new BusinessException(ErrorCode.BOOKING_STATUS_ERROR, "该预约状态不允许核验");
        }

        // 3. 校验是否过期
        if (booking.getVisitDate().isBefore(today)) {
            throw new BusinessException(ErrorCode.BOOKING_EXPIRED, "预约已过期，无法核验");
        }

        // 4. 更新预约状态
        booking.setStatus(4); // 4已核验
        booking.setVerifyTime(LocalDateTime.now());
        booking.setUpdatedAt(LocalDateTime.now());
        this.baseMapper.updateById(booking);

        log.info("预约核验成功：ticketCode={}, visitDate={}, userId={}",
                ticketCode, booking.getVisitDate(), booking.getUserId());

        return toVO(booking);
    }

    @Override
    public IPage<BookingVO> queryBookings(Integer page, Integer size, LocalDate visitDate, Integer status, String ticketCode) {
        // 1. 构建分页对象
        Page<VisitBooking> pageParam = new Page<>(page, size);

        // 2. 构建查询条件
        QueryWrapper<VisitBooking> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", 0)
                .orderByDesc("visit_date")
                .orderByDesc("created_at");

        // 3. 预约日期过滤（如果未指定，默认查询当天）
        if (visitDate != null) {
            queryWrapper.eq("visit_date", visitDate);
        } else {
            // 默认查询今天的预约
            queryWrapper.eq("visit_date", LocalDate.now());
        }

        // 4. 状态过滤（可选）
        if (status != null) {
            queryWrapper.eq("status", status);
        }

        // 5. 票号模糊查询（可选）
        if (ticketCode != null && !ticketCode.trim().isEmpty()) {
            queryWrapper.like("ticket_code", ticketCode.trim());
        }

        // 6. 查询分页
        IPage<VisitBooking> resultPage = this.baseMapper.selectPage(pageParam, queryWrapper);

        // 7. 转换为 VO
        return resultPage.convert(this::toVO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer updateExpiredBookings() {
        // 批量更新过期预约：status in (1,3) 且 visit_date < CURDATE()
        QueryWrapper<VisitBooking> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("deleted", 0)
                .in("status", 1, 3)
                .lt("visit_date", LocalDate.now());

        // 查询所有过期预约
        var expiredBookings = this.baseMapper.selectList(queryWrapper);
        int count = expiredBookings.size();

        if (count > 0) {
            // 批量更新
            expiredBookings.forEach(booking -> {
                booking.setStatus(5); // 5已过期
                booking.setUpdatedAt(LocalDateTime.now());
            });
            this.updateBatchById(expiredBookings);

            log.info("定时任务：批量更新过期预约完成，共 {} 条", count);
        }

        return count;
    }

    /**
     * 校验用户实名信息
     */
    private void checkRealName(Long userId) {
        SysUser user = sysUserService.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_EXIST);
        }

        // 检查 real_name, id_No, phone 是否为空
        if (isEmpty(user.getRealName()) || isEmpty(user.getIdNo()) || isEmpty(user.getPhone())) {
            throw new BusinessException(ErrorCode.USER_NOT_REALNAME);
        }
    }

    /**
     * 字符串空判断（支持 null 和空字符串）
     */
    private boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * 实体转 VO
     */
    private BookingVO toVO(VisitBooking booking) {
        BookingVO vo = new BookingVO();
        BeanUtils.copyProperties(booking, vo);
        return vo;
    }
}
