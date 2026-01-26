package com.design.museum.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.design.museum.dto.CommentAddRequest;
import com.design.museum.entity.ExhibitComment;
import com.design.museum.mapper.ExhibitCommentMapper;
import com.design.museum.service.IExhibitCommentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 展品评论服务实现类
 */
@Service
public class ExhibitCommentServiceImpl extends ServiceImpl<ExhibitCommentMapper, ExhibitComment> implements IExhibitCommentService {

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
    public Page<com.design.museum.vo.CommentVO> listComments(Long itemId, long current, long size) {
        // 查询评论（只返回未删除的）
        Page<ExhibitComment> page = this.page(
                new Page<>(current, size),
                new QueryWrapper<ExhibitComment>()
                        .eq("item_id", itemId)
                        .eq("deleted", 0)
                        .orderByDesc("created_at")
        );

        // 获取所有用户ID
        List<Long> userIds = page.getRecords().stream()
                .map(ExhibitComment::getUserId)
                .distinct()
                .collect(Collectors.toList());

        // TODO: 批量查询用户信息（需要SysUserService）
        // 这里暂时设置username为空，实际使用时需要注入SysUserService来获取用户名
        Map<Long, String> userMap = userIds.stream()
                .collect(Collectors.toMap(id -> id, id -> "用户" + id));

        // 转换为VO
        Page<com.design.museum.vo.CommentVO> voPage = new Page<>(current, size, page.getTotal());
        List<com.design.museum.vo.CommentVO> voList = new ArrayList<>();

        for (ExhibitComment comment : page.getRecords()) {
            com.design.museum.vo.CommentVO vo = new com.design.museum.vo.CommentVO();
            BeanUtil.copyProperties(comment, vo);
            vo.setUsername(userMap.get(comment.getUserId()));
            voList.add(vo);
        }

        voPage.setRecords(voList);
        return voPage;
    }
}