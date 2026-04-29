package com.spmp.dashboard.service;

import com.spmp.dashboard.domain.dto.DashboardQueryDTO;
import com.spmp.dashboard.domain.vo.DashboardKpiVO;
import com.spmp.dashboard.domain.vo.DashboardModuleVO;
import com.spmp.workorder.domain.vo.TrendDataVO;

import java.util.List;

/**
 * Dashboard 服务。
 */
public interface DashboardService {

    DashboardModuleVO<DashboardKpiVO> getKpi(DashboardQueryDTO queryDTO);

    DashboardModuleVO<List<TrendDataVO>> getWorkorderTrend(DashboardQueryDTO queryDTO);

    DashboardModuleVO<List<com.spmp.billing.domain.vo.TrendDataVO>> getBillingTrend(DashboardQueryDTO queryDTO);
}

