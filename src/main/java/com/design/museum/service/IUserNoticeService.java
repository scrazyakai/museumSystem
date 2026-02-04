package com.design.museum.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.design.museum.entity.UserNotice;
import com.baomidou.mybatisplus.extension.service.IService;
import com.design.museum.vo.UserNoticeVO;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author
 * @since 2026-01-29
 */
public interface IUserNoticeService extends IService<UserNotice> {

    /**
     * 分页查询用户消息
     *
     * @param page 页码
     * @param size 每页大小
     * @param userId 用户ID
     * @param category 分类（可选）
     * @param readFlag 读取状态（可选）
     * @return 消息分页数据
     */
    IPage<UserNoticeVO> getUserNotices(int page, int size, Long userId, Integer category, Integer readFlag);

    /**
     * 获取消息详情
     *
     * @param id 消息ID
     * @param userId 用户ID
     * @return 消息详情
     */
    UserNoticeVO getNoticeDetail(Long id, Long userId);

    /**
     * 标记所有消息为已读
     *
     * @param userId 用户ID
     * @return 更新的消息数量
     */
    boolean markAllAsRead(Long userId);
}
