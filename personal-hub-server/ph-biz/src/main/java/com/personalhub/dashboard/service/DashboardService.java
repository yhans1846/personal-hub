package com.personalhub.dashboard.service;

import com.personalhub.dashboard.vo.DashboardStatsVO;
import com.personalhub.dashboard.vo.SearchVO;
import com.personalhub.dashboard.vo.StatsVO;
import com.personalhub.dashboard.vo.TrendVO;

/**
 * Dashboard 服务接口
 */
public interface DashboardService {

    /** 获取用户 Dashboard 统计数据 */
    DashboardStatsVO getStats(Long userId);

    /** 获取趋势数据（学习/笔记/Todo/阅读） */
    TrendVO getTrends(Long userId, int days);

    /** 全局搜索（跨所有模块） */
    SearchVO search(Long userId, String keyword);

    /** 获取综合统计数据（统计页面专用） */
    StatsVO getDetailedStats(Long userId, int days);
}
