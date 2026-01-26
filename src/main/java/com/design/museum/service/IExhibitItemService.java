package com.design.museum.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.design.museum.dto.ExhibitItemAddRequest;
import com.design.museum.dto.ExhibitItemQueryRequest;
import com.design.museum.dto.ExhibitItemUpdateRequest;
import com.design.museum.entity.ExhibitItem;
import com.design.museum.vo.ExhibitItemVO;

/**
 * 展品服务类
 */
public interface IExhibitItemService extends IService<ExhibitItem> {

    /**
     * 添加展品
     *
     * @param request     添加请求
     * @param creatorId   创建人ID（管理员ID）
     * @return 展品ID
     */
    Long addExhibitItem(ExhibitItemAddRequest request, Long creatorId);

    /**
     * 更新展品
     *
     * @param request 更新请求
     * @return 是否成功
     */
    boolean updateExhibitItem(ExhibitItemUpdateRequest request);

    /**
     * 下架展品（逻辑删除）
     *
     * @param id 展品ID
     * @return 是否成功
     */
    boolean downExhibitItem(Long id);

    /**
     * 恢复展品
     *
     * @param id 展品ID
     * @return 是否成功
     */
    boolean restoreExhibitItem(Long id);

    /**
     * 分页查询展品（管理员）
     *
     * @param request 查询请求
     * @return 分页结果
     */
    Page<ExhibitItem> adminListExhibitItems(ExhibitItemQueryRequest request);

    /**
     * 分页查询展品（用户端）
     *
     * @param request 查询请求
     * @return 分页结果
     */
    Page<ExhibitItemVO> userListExhibitItems(ExhibitItemQueryRequest request);

    /**
     * 根据ID获取展品详情（用户端）
     *
     * @param id 展品ID
     * @return 展品VO
     */
    ExhibitItemVO getExhibitItemVO(Long id);

    /**
     * 根据ID获取展品实体
     *
     * @param id 展品ID
     * @return 展品实体
     */
    ExhibitItem getExhibitItemById(Long id);
}