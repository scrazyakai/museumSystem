package com.design.museum.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.design.museum.dto.CommentAddRequest;
import com.design.museum.entity.ExhibitComment;
import com.design.museum.entity.SysUser;
import com.design.museum.enums.CommentStatusEnum;
import com.design.museum.mapper.ExhibitCommentMapper;
import com.design.museum.service.IExhibitCommentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.design.museum.vo.CommentVO;
import org.springframework.stereotype.Service;

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
    public Page<CommentVO> listComments(Long itemId, long current, long size) {
        // 查询评论（只返回未删除的）
        Page<ExhibitComment> page = this.page(
                new Page<>(current, size),
                new QueryWrapper<ExhibitComment>()
                        .eq("item_id", itemId)
                        .eq("status", CommentStatusEnum.DISPLAY.getValue())
                        .orderByDesc("created_at")
        );

        // 获取所有用户ID
        List<Long> userIds = page.getRecords().stream()
                .map(ExhibitComment::getUserId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, String> userMap = sysUserService.listByIds(userIds).stream()
                .collect(Collectors.toMap(
                        SysUser::getId,
                        u -> Optional.ofNullable(u.getUsername()).orElse(""),
                        (a, b) -> a // 防止极端情况下重复key报错
                ));


        // 转换为VO
        Page<CommentVO> voPage = new Page<>(current, size, page.getTotal());
        List<CommentVO> voList = new ArrayList<>();

        for (ExhibitComment comment : page.getRecords()) {
            CommentVO vo = new CommentVO();
            BeanUtil.copyProperties(comment, vo);
            vo.setUsername(userMap.get(comment.getUserId()));
            voList.add(vo);
        }
        voPage.setRecords(voList);
        return voPage;
    }
}