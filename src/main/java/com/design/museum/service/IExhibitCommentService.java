package com.design.museum.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.design.museum.dto.CommentAddRequest;
import com.design.museum.entity.ExhibitComment;
import com.design.museum.vo.CommentVO;

/**
 * 展品评论服务类
 */
public interface IExhibitCommentService extends IService<ExhibitComment> {

    /**
     * 添加评论
     *
     * @param itemId  展品ID
     * @param request 添加请求
     * @param userId  用户ID
     * @return 评论ID
     */
    Long addComment(Long itemId, CommentAddRequest request, Long userId);

    /**
     * 删除评论（逻辑删除）
     * 仅允许评论本人删除
     *
     * @param commentId 评论ID
     * @param userId    用户ID
     * @return 是否成功
     */
    boolean deleteComment(Long commentId, Long userId);

    /**
     * 分页查询展品的评论
     *
     * @param itemId     展品ID
     * @param current    当前页
     * @param size       每页大小
     * @return 分页结果
     */
    Page<CommentVO> listComments(Long itemId, long current, long size);

    /**
     * 管理员分页查询评论（支持按展品ID/名称搜索）
     *
     * @param itemId  展品ID（可选）
     * @param itemName 展品名称（可选，模糊搜索）
     * @param current 当前页
     * @param size    每页大小
     * @return 分页结果
     */
    Page<CommentVO> adminListComments(Long itemId, String itemName, long current, long size,Integer status);

    /**
     * 管理员删除评论（逻辑删除）
     *
     * @param commentId 评论ID
     * @return 是否成功
     */
    boolean adminDeleteComment(Long commentId);

    /**
     * 管理员隐藏评论
     *
     * @param commentId 评论ID
     * @return 是否成功
     */
    boolean adminHideComment(Long commentId);

    /**
     * 管理员显示评论
     *
     * @param commentId 评论ID
     * @return 是否成功
     */
    boolean adminShowComment(Long commentId);
}