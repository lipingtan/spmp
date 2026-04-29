package com.spmp.dashboard.controller;

import com.spmp.common.result.Result;
import com.spmp.dashboard.domain.dto.DashboardQueryDTO;
import com.spmp.dashboard.domain.vo.DashboardKpiVO;
import com.spmp.dashboard.domain.vo.DashboardModuleVO;
import com.spmp.dashboard.service.DashboardService;
import com.spmp.workorder.domain.vo.TrendDataVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Dashboard 接口（PC 首页）。
 */
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/kpi")
    public Result<DashboardModuleVO<DashboardKpiVO>> getKpi(DashboardQueryDTO queryDTO) {
        return Result.success(dashboardService.getKpi(queryDTO));
    }

    @GetMapping("/trend/workorder")
    public Result<DashboardModuleVO<List<TrendDataVO>>> getWorkorderTrend(DashboardQueryDTO queryDTO) {
        return Result.success(dashboardService.getWorkorderTrend(queryDTO));
    }

    @GetMapping("/trend/billing")
    public Result<DashboardModuleVO<List<com.spmp.billing.domain.vo.TrendDataVO>>> getBillingTrend(DashboardQueryDTO queryDTO) {
        return Result.success(dashboardService.getBillingTrend(queryDTO));
    }
}

