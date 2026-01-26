package com.design.museum.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.design.museum.dto.ExhibitItemAddRequest;
import com.design.museum.dto.ExhibitItemQueryRequest;
import com.design.museum.dto.ExhibitItemUpdateRequest;
import com.design.museum.entity.ExhibitItem;
import com.design.museum.enums.ExhibitItemStatusEnum;
import com.design.museum.mapper.ExhibitItemMapper;
import com.design.museum.service.IExhibitItemService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 展品服务实现类
 */
@Service
public class ExhibitItemServiceImpl extends ServiceImpl<ExhibitItemMapper, ExhibitItem> implements IExhibitItemService {

    @Override
    public Long addExhibitItem(ExhibitItemAddRequest request, Long creatorId) {
        ExhibitItem exhibitItem = new ExhibitItem();
        BeanUtil.copyProperties(request, exhibitItem);
        exhibitItem.setCreatorId(creatorId);
        exhibitItem.setCreatedAt(LocalDateTime.now());
        exhibitItem.setUpdatedAt(LocalDateTime.now());

        this.save(exhibitItem);
        return exhibitItem.getId();
    }

    @Override
    public boolean updateExhibitItem(ExhibitItemUpdateRequest request) {
        ExhibitItem exhibitItem = new ExhibitItem();
        BeanUtil.copyProperties(request, exhibitItem);
        exhibitItem.setUpdatedAt(LocalDateTime.now());

        return this.updateById(exhibitItem);
    }

    @Override
    public boolean downExhibitItem(Long id) {
        ExhibitItem exhibitItem = this.getById(id);
        if (exhibitItem == null) {
            return false;
        }
        exhibitItem.setStatus(ExhibitItemStatusEnum.DOWN.getValue());
        exhibitItem.setUpdatedAt(LocalDateTime.now());
        return this.updateById(exhibitItem);
    }

    @Override
    public boolean restoreExhibitItem(Long id) {
        // 恢复：将deleted改回0
        UpdateWrapper<ExhibitItem> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id);
        updateWrapper.set("status", ExhibitItemStatusEnum.DISPLAY.getValue());

        return this.update(updateWrapper);
    }

    @Override
    public Page<ExhibitItem> adminListExhibitItems(ExhibitItemQueryRequest request) {
        long current = request.getCurrent();
        long size = request.getSize();
        String keyword = request.getKeyword();
        String mediaKind = request.getMediaKind();

        Page<ExhibitItem> page = this.page(
                new Page<>(current, size),
                new QueryWrapper<ExhibitItem>()
                        .like(keyword != null, "title", keyword)
                        .eq(mediaKind != null, "media_kind", mediaKind)
                        .orderByDesc("created_at")
        );

        return page;
    }

    @Override
    public Page<com.design.museum.vo.ExhibitItemVO> userListExhibitItems(ExhibitItemQueryRequest request) {
        long current = request.getCurrent();
        long size = request.getSize();
        String mediaKind = request.getMediaKind();
        LocalDateTime now = LocalDateTime.now();

        Page<ExhibitItem> page = this.page(
                new Page<>(current, size),
                new QueryWrapper<ExhibitItem>()
                        .eq("status", ExhibitItemStatusEnum.DISPLAY.getValue())
                        .eq(mediaKind != null, "media_kind", mediaKind)
                        .and(wrapper -> wrapper
                                .isNull("start_time")
                                .or()
                                .le("start_time", now)
                        )
                        .and(wrapper -> wrapper
                                .isNull("end_time")
                                .or()
                                .ge("end_time", now)
                        )
                        .orderByDesc("created_at")
        );

        // 转换为VO
        Page<com.design.museum.vo.ExhibitItemVO> voPage = new Page<>(current, size, page.getTotal());
        voPage.setRecords(page.getRecords().stream()
                .map(this::entityToVO)
                .collect(java.util.stream.Collectors.toList()));

        return voPage;
    }

    @Override
    public com.design.museum.vo.ExhibitItemVO getExhibitItemVO(Long id) {
        ExhibitItem exhibitItem = this.getById(id);
        if (exhibitItem == null) {
            return null;
        }
        return entityToVO(exhibitItem);
    }

    @Override
    public ExhibitItem getExhibitItemById(Long id) {
        return this.getById(id);
    }

    /**
     * 实体转VO
     */
    private com.design.museum.vo.ExhibitItemVO entityToVO(ExhibitItem exhibitItem) {
        com.design.museum.vo.ExhibitItemVO vo = new com.design.museum.vo.ExhibitItemVO();
        BeanUtil.copyProperties(exhibitItem, vo);
        return vo;
    }
}