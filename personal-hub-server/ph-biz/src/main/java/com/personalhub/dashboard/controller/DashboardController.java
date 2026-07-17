package com.personalhub.dashboard.controller;

import com.personalhub.common.result.Result;
import com.personalhub.dashboard.service.DashboardService;
import com.personalhub.dashboard.vo.DashboardStatsVO;
import com.personalhub.dashboard.vo.SearchVO;
import com.personalhub.dashboard.vo.StatsVO;
import com.personalhub.dashboard.vo.TrendVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Dashboard 控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    @Operation(summary = "获取 Dashboard 统计数据")
    public Result<DashboardStatsVO> stats(Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        return Result.success(dashboardService.getStats(userId));
    }

    @GetMapping("/trends")
    @Operation(summary = "获取趋势数据（学习/笔记/Todo/阅读）")
    public Result<TrendVO> trends(@RequestParam(defaultValue = "30") int days,
                                  Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        return Result.success(dashboardService.getTrends(userId, days));
    }

    @GetMapping("/search")
    @Operation(summary = "全局搜索（跨所有模块）")
    public Result<SearchVO> search(@RequestParam String keyword,
                                   Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        return Result.success(dashboardService.search(userId, keyword));
    }

    @GetMapping("/detail")
    @Operation(summary = "获取综合统计数据（统计页面专用，含全部8个模块）")
    public Result<StatsVO> detail(@RequestParam(defaultValue = "30") int days,
                                  Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        return Result.success(dashboardService.getDetailedStats(userId, days));
    }
}
