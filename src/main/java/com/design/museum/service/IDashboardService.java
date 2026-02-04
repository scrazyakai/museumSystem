package com.design.museum.service;

import com.design.museum.vo.DashboardVO;

/**
 * 数据统计服务接口
 */
public interface IDashboardService {

    /**
     * 获取统计数据
     *
     * @return 统计数据
     */
    DashboardVO getDashboard();
}
