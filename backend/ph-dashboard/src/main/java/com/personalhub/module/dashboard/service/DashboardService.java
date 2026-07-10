package com.personalhub.module.dashboard.service;

import com.personalhub.module.dashboard.vo.DashboardStatsVO;
import com.personalhub.module.dashboard.vo.SearchVO;
import com.personalhub.module.dashboard.vo.TrendVO;

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
}
