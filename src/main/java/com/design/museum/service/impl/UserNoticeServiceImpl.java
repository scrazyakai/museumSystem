package com.design.museum.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.design.museum.entity.UserNotice;
import com.design.museum.mapper.UserNoticeMapper;
import com.design.museum.service.IUserNoticeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.design.museum.vo.UserNoticeVO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author
 * @since 2026-01-29
 */
@Service
public class UserNoticeServiceImpl extends ServiceImpl<UserNoticeMapper, UserNotice> implements IUserNoticeService {

    @Override
    public IPage<UserNoticeVO> getUserNotices(int page, int size, Long userId, Integer category, Integer readFlag) {
        // 构建分页对象
        Page<UserNotice> pageParam = new Page<>(page, size);

        // 构建查询条件
        LambdaQueryWrapper<UserNotice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserNotice::getUserId, userId)
                .eq(UserNotice::getDeleted, 0)
                .orderByDesc(UserNotice::getCreatedAt);

        // 可选条件：分类
        if (category != null) {
            wrapper.eq(UserNotice::getCategory, category);
        }

        // 可选条件：读取状态
        if (readFlag != null) {
            wrapper.eq(UserNotice::getReadFlag, readFlag);
        }

        // 执行分页查询
        IPage<UserNotice> noticePage = this.page(pageParam, wrapper);

        // 转换为VO
        return noticePage.convert(notice -> {
            UserNoticeVO vo = new UserNoticeVO();
            BeanUtil.copyProperties(notice, vo);
            return vo;
        });
    }

    @Override
    public UserNoticeVO getNoticeDetail(Long id, Long userId) {
        // 查询消息详情
        UserNotice notice = this.lambdaQuery()
                .eq(UserNotice::getId, id)
                .eq(UserNotice::getUserId, userId)
                .eq(UserNotice::getDeleted, 0)
                .one();

        if (notice == null) {
            return null;
        }

        // 转换为VO
        UserNoticeVO vo = new UserNoticeVO();
        BeanUtil.copyProperties(notice, vo);
        return vo;
    }

    @Override
    public boolean markAllAsRead(Long userId) {
        // 更新当前用户所有未读消息为已读
        LambdaUpdateWrapper<UserNotice> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(UserNotice::getUserId, userId)
                .eq(UserNotice::getReadFlag, 0)
                .eq(UserNotice::getDeleted, 0)
                .set(UserNotice::getReadFlag, 1);

        return this.update(wrapper);
    }
}
