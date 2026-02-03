package com.design.museum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.design.museum.entity.ExhibitCommentLike;
import com.design.museum.enums.CommentLikeStatusEnum;
import com.design.museum.exception.BusinessException;
import com.design.museum.mapper.ExhibitCommentLikeMapper;
import com.design.museum.service.IExhibitCommentLikeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author
 * @since 2026-02-03
 */
@Slf4j
@Service
public class ExhibitCommentLikeServiceImpl extends ServiceImpl<ExhibitCommentLikeMapper, ExhibitCommentLike> implements IExhibitCommentLikeService {

    @Override
    public boolean likeComment(Long commentId, Long userId) {
        // 检查是否已存在点赞记录
        LambdaQueryWrapper<ExhibitCommentLike> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ExhibitCommentLike::getCommentId, commentId)
                .eq(ExhibitCommentLike::getUserId, userId);
        ExhibitCommentLike existLike = this.getOne(queryWrapper);

        if (existLike != null) {
            // 如果已存在且状态是点赞，直接返回失败
            if (existLike.getStatus().equals(CommentLikeStatusEnum.LIKE.getValue())) {
                return false;
            }
            // 如果状态是取消点赞，更新为点赞
            existLike.setStatus(CommentLikeStatusEnum.LIKE.getValue());
            existLike.setUpdatedAt(LocalDateTime.now());
            return this.updateById(existLike);
        }

        // 不存在记录，创建新点赞记录
        ExhibitCommentLike like = new ExhibitCommentLike();
        like.setCommentId(commentId);
        like.setUserId(userId);
        like.setStatus(CommentLikeStatusEnum.LIKE.getValue());
        like.setCreatedAt(LocalDateTime.now());
        like.setUpdatedAt(LocalDateTime.now());

        return this.save(like);
    }

    @Override
    public boolean cancelLike(Long commentId, Long userId) {
        // 查询点赞记录
        LambdaQueryWrapper<ExhibitCommentLike> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ExhibitCommentLike::getCommentId, commentId)
                .eq(ExhibitCommentLike::getUserId, userId)
                .eq(ExhibitCommentLike::getStatus, CommentLikeStatusEnum.LIKE.getValue());

        ExhibitCommentLike like = this.getOne(queryWrapper);
        if (like == null) {
            // 没有点赞记录，返回失败
            return false;
        }

        // 更新状态为取消点赞
        like.setStatus(CommentLikeStatusEnum.CANCEL_LIKE.getValue());
        like.setUpdatedAt(LocalDateTime.now());

        return this.updateById(like);
    }

    @Override
    public Long getLikeCount(Long commentId) {
        // 统计该评论点赞数（status = 1 的记录数）
        LambdaQueryWrapper<ExhibitCommentLike> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ExhibitCommentLike::getCommentId, commentId)
                .eq(ExhibitCommentLike::getStatus, CommentLikeStatusEnum.LIKE.getValue());

        return this.count(queryWrapper);
    }

    @Override
    public boolean hasLiked(Long commentId, Long userId) {
        // 检查用户是否已点赞该评论
        LambdaQueryWrapper<ExhibitCommentLike> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ExhibitCommentLike::getCommentId, commentId)
                .eq(ExhibitCommentLike::getUserId, userId)
                .eq(ExhibitCommentLike::getStatus, CommentLikeStatusEnum.LIKE.getValue());

        return this.count(queryWrapper) > 0;
    }
}
