package com.design.museum.service;

import com.design.museum.entity.ExhibitCommentLike;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author
 * @since 2026-02-03
 */
public interface IExhibitCommentLikeService extends IService<ExhibitCommentLike> {

    /**
     * 点赞
     * @param commentId 评论ID
     * @param userId 用户ID
     * @return 是否点赞成功
     */
    boolean likeComment(Long commentId, Long userId);

    /**
     * 取消点赞
     * @param commentId 评论ID
     * @param userId 用户ID
     * @return 是否取消成功
     */
    boolean cancelLike(Long commentId, Long userId);

    /**
     * 获取评论点赞数
     * @param commentId 评论ID
     * @return 点赞数
     */
    Long getLikeCount(Long commentId);

    /**
     * 检查用户是否已点赞
     * @param commentId 评论ID
     * @param userId 用户ID
     * @return 是否已点赞
     */
    boolean hasLiked(Long commentId, Long userId);
}
