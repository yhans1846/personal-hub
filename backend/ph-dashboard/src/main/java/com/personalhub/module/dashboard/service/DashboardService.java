package com.personalhub.module.dashboard.service;

import com.personalhub.module.dashboard.vo.DashboardStatsVO;

/**
 * Dashboard 服务接口
 */
public interface DashboardService {

    /** 获取用户 Dashboard 统计数据 */
    DashboardStatsVO getStats(Long userId);
}
