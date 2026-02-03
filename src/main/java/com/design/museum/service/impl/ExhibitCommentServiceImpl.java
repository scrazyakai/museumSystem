package com.design.museum.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.design.museum.dto.CommentAddRequest;
import com.design.museum.entity.ExhibitComment;
import com.design.museum.entity.ExhibitItem;
import com.design.museum.entity.SysUser;
import com.design.museum.enums.CommentStatusEnum;
import com.design.museum.mapper.ExhibitCommentMapper;
import com.design.museum.service.IExhibitCommentLikeService;
import com.design.museum.service.IExhibitCommentService;
import com.design.museum.service.IExhibitItemService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.design.museum.vo.CommentVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 展品评论服务实现类
 */
@Service
public class ExhibitCommentServiceImpl extends ServiceImpl<ExhibitCommentMapper, ExhibitComment> implements IExhibitCommentService {
    @Resource
    private SysUserServiceImpl sysUserService;
    @Resource
    private IExhibitItemService exhibitItemService;
    @Resource
    private IExhibitCommentLikeService exhibitCommentLikeService;
    @Override
    public Long addComment(Long itemId, CommentAddRequest request, Long userId) {
        ExhibitComment comment = new ExhibitComment();
        comment.setItemId(itemId);
        comment.setUserId(userId);
        comment.setContent(request.getContent());
        comment.setCreatedAt(LocalDateTime.now());

        this.save(comment);
        return comment.getId();
    }

    @Override
    public boolean deleteComment(Long commentId, Long userId) {
        // 查询评论
        ExhibitComment comment = this.getById(commentId);
        if (comment == null) {
            throw new RuntimeException("评论不存在");
        }

        // 检查是否是评论本人
        if (!comment.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除他人的评论");
        }

        // 使用MyBatis-Plus的逻辑删除功能
        return this.removeById(commentId);
    }
    @Override
    public Page<CommentVO> listComments(Long itemId, long current, long size, Long userId) {
        // 查询评论（只返回未删除的）
        Page<ExhibitComment> page = this.page(
                new Page<>(current, size),
                new QueryWrapper<ExhibitComment>()
                        .eq("item_id", itemId)
                        .eq("status", CommentStatusEnum.DISPLAY.getValue())
                        .orderByDesc("created_at")
        );
        // 转换为VO
        Page<CommentVO> voPage = new Page<>(current, size, page.getTotal());
        List<CommentVO> voList = new ArrayList<>();

        for (ExhibitComment comment : page.getRecords()) {
            CommentVO vo = new CommentVO();
            BeanUtil.copyProperties(comment, vo);
            SysUser user = sysUserService.getById(comment.getUserId());
            vo.setUsername(user.getUsername());
            vo.setAvatarURL(user.getAvatarUrl());

            // 查询点赞数
            Long likeCount = exhibitCommentLikeService.getLikeCount(comment.getId());
            vo.setLikeCount(likeCount);

            // 检查当前用户是否已点赞
            if (userId != null) {
                boolean hasLiked = exhibitCommentLikeService.hasLiked(comment.getId(), userId);
                vo.setLiked(hasLiked ? 1 : 0);
            } else {
                vo.setLiked(0);
            }

            voList.add(vo);
        }
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public Page<CommentVO> adminListComments(Long itemId, String itemName, long current, long size, Integer status, Long userId) {
        // 构建查询条件
        QueryWrapper<ExhibitComment> queryWrapper = new QueryWrapper<>();
        if(status != null) {
            queryWrapper.eq("status", status);
        }
        // 如果提供了展品ID，直接按展品ID查询
        if (itemId != null) {
            queryWrapper.eq("item_id", itemId);
        }
        // 如果提供了展品名称，先搜索匹配的展品，然后按展品ID查询
        else if (StringUtils.hasText(itemName)) {
            QueryWrapper<ExhibitItem> itemQueryWrapper = new QueryWrapper<>();
            itemQueryWrapper.like("title", itemName);
            List<ExhibitItem> items = exhibitItemService.list(itemQueryWrapper);
            if (items.isEmpty()) {
                // 如果没有匹配的展品，返回空结果
                return new Page<>(current, size, 0);
            }
            List<Long> itemIds = items.stream().map(ExhibitItem::getId).collect(Collectors.toList());
            queryWrapper.in("item_id", itemIds);
        }

        // 按创建时间倒序排列
        queryWrapper.orderByDesc("created_at");

        // 分页查询评论（包含隐藏和展示的）
        Page<ExhibitComment> page = this.page(new Page<>(current, size), queryWrapper);

        // 获取所有用户ID
        List<Long> userIds = page.getRecords().stream()
                .map(ExhibitComment::getUserId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, String> userMap = new HashMap<>();
        if (!userIds.isEmpty()) {
            userMap = sysUserService.listByIds(userIds).stream()
                    .collect(Collectors.toMap(
                            SysUser::getId,
                            u -> Optional.ofNullable(u.getUsername()).orElse(""),
                            (a, b) -> a
                    ));
        }

        // 转换为VO
        Page<CommentVO> voPage = new Page<>(current, size, page.getTotal());
        List<CommentVO> voList = new ArrayList<>();

        for (ExhibitComment comment : page.getRecords()) {
            CommentVO vo = new CommentVO();
            BeanUtil.copyProperties(comment, vo);
            vo.setUsername(userMap.get(comment.getUserId()));

            // 查询点赞数
            Long likeCount = exhibitCommentLikeService.getLikeCount(comment.getId());
            vo.setLikeCount(likeCount);

            // 检查当前用户是否已点赞
            if (userId != null) {
                boolean hasLiked = exhibitCommentLikeService.hasLiked(comment.getId(), userId);
                vo.setLiked(hasLiked ? 1 : 0);
            } else {
                vo.setLiked(0);
            }

            voList.add(vo);
        }
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public boolean adminDeleteComment(Long commentId) {
        // 查询评论是否存在（只查询未删除的）
        ExhibitComment comment = this.getById(commentId);

        if (comment == null) {
            throw new RuntimeException("评论不存在");
        }
        return this.removeById(comment);
    }

    @Override
    public boolean adminHideComment(Long commentId) {
        // 查询评论是否存在
        ExhibitComment comment = this.getById(commentId);
        if (comment == null) {
            throw new RuntimeException("评论不存在");
        }
        // 更新状态为隐藏
        comment.setStatus(CommentStatusEnum.HIDDEN.getValue());
        return this.updateById(comment);
    }

    @Override
    public boolean adminShowComment(Long commentId) {
        // 查询评论是否存在
        ExhibitComment comment = this.getById(commentId);
        if (comment == null) {
            throw new RuntimeException("评论不存在");
        }
        // 更新状态为展示
        comment.setStatus(CommentStatusEnum.DISPLAY.getValue());
        return this.updateById(comment);
    }
}