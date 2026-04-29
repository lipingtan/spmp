package com.spmp.dashboard.service.impl;

import com.spmp.billing.service.BillStatisticsService;
import com.spmp.dashboard.domain.dto.DashboardQueryDTO;
import com.spmp.dashboard.domain.vo.DashboardKpiVO;
import com.spmp.dashboard.domain.vo.DashboardModuleStatus;
import com.spmp.dashboard.domain.vo.DashboardModuleVO;
import com.spmp.dashboard.service.DashboardService;
import com.spmp.notice.repository.AnnouncementMapper;
import com.spmp.user.service.PermissionCacheService;
import com.spmp.common.util.SecurityUtils;
import com.spmp.workorder.domain.vo.TrendDataVO;
import com.spmp.workorder.service.WorkOrderStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Dashboard 服务实现。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private static final String PERM_WORKORDER_STATISTICS = "workorder:statistics";
    private static final String PERM_BILLING_STATISTICS = "billing:statistics";
    private static final String PERM_NOTICE_LIST = "notice:list";

    private final WorkOrderStatisticsService workOrderStatisticsService;
    private final BillStatisticsService billStatisticsService;
    private final AnnouncementMapper announcementMapper;
    private final PermissionCacheService permissionCacheService;

    @Override
    public DashboardModuleVO<DashboardKpiVO> getKpi(DashboardQueryDTO queryDTO) {
        DashboardModuleVO<DashboardKpiVO> module = new DashboardModuleVO<>();
        Set<String> permissions = currentPermissions();
        boolean canWorkorder = permissions.contains(PERM_WORKORDER_STATISTICS);
        boolean canBilling = permissions.contains(PERM_BILLING_STATISTICS);
        boolean canNotice = permissions.contains(PERM_NOTICE_LIST);
        if (!canWorkorder && !canBilling && !canNotice) {
            module.setStatus(DashboardModuleStatus.NO_PERMISSION);
            module.setMessage("无可用看板权限");
            module.setData(null);
            return module;
        }
        try {
            DashboardKpiVO kpi = new DashboardKpiVO();
            if (canWorkorder) {
                com.spmp.workorder.domain.vo.StatisticsVO workorderStats = workOrderStatisticsService
                        .getStatistics(toWorkorderQuery(queryDTO, false));
                kpi.setPendingCount(safeInteger(workorderStats.getPendingCount()));
                kpi.setInProgressCount(safeInteger(workorderStats.getInProgressCount()));
                kpi.setMonthlyCompletedCount(safeInteger(workorderStats.getMonthlyCompletedCount()));
                kpi.setAvgRepairDuration(safeInteger(workorderStats.getAvgRepairDuration()));
            } else {
                kpi.setPendingCount(0);
                kpi.setInProgressCount(0);
                kpi.setMonthlyCompletedCount(0);
                kpi.setAvgRepairDuration(0);
            }
            if (canBilling) {
                com.spmp.billing.domain.vo.StatisticsVO billingStats = billStatisticsService
                        .getStatistics(toBillingQuery(queryDTO, false));
                kpi.setTotalReceivable(safeDecimal(billingStats.getTotalReceivable()));
                kpi.setTotalReceived(safeDecimal(billingStats.getTotalReceived()));
                kpi.setCollectionRate(safeDecimal(billingStats.getCollectionRate()));
                kpi.setOverdueAmount(safeDecimal(billingStats.getOverdueAmount()));
            } else {
                kpi.setTotalReceivable(BigDecimal.ZERO);
                kpi.setTotalReceived(BigDecimal.ZERO);
                kpi.setCollectionRate(BigDecimal.ZERO);
                kpi.setOverdueAmount(BigDecimal.ZERO);
            }
            if (canNotice) {
                LocalDateTime since = LocalDate.now().minusDays(7).atStartOfDay();
                Long count = announcementMapper.countPublishedSince(since);
                kpi.setRecentNoticeCount(count != null ? count : 0L);
            } else {
                kpi.setRecentNoticeCount(0L);
            }
            module.setStatus(DashboardModuleStatus.SUCCESS);
            module.setMessage("");
            module.setData(kpi);
        } catch (Exception ex) {
            log.error("加载 Dashboard KPI 失败", ex);
            module.setStatus(DashboardModuleStatus.ERROR);
            module.setMessage("KPI 数据加载失败");
            module.setData(null);
        }
        return module;
    }

    @Override
    public DashboardModuleVO<List<TrendDataVO>> getWorkorderTrend(DashboardQueryDTO queryDTO) {
        DashboardModuleVO<List<TrendDataVO>> module = new DashboardModuleVO<>();
        if (!currentPermissions().contains(PERM_WORKORDER_STATISTICS)) {
            module.setStatus(DashboardModuleStatus.NO_PERMISSION);
            module.setMessage("无工单统计权限");
            module.setData(Collections.emptyList());
            return module;
        }
        try {
            com.spmp.workorder.domain.vo.StatisticsVO workorderStats = workOrderStatisticsService
                    .getStatistics(toWorkorderQuery(queryDTO, true));
            List<TrendDataVO> trend = workorderStats.getDailyTrend() != null
                    ? workorderStats.getDailyTrend() : Collections.emptyList();
            module.setStatus(trend.isEmpty() ? DashboardModuleStatus.EMPTY : DashboardModuleStatus.SUCCESS);
            module.setMessage(trend.isEmpty() ? "暂无工单趋势数据" : "");
            module.setData(trend);
        } catch (Exception ex) {
            log.error("加载 Dashboard 工单趋势失败", ex);
            module.setStatus(DashboardModuleStatus.ERROR);
            module.setMessage("工单趋势加载失败");
            module.setData(Collections.emptyList());
        }
        return module;
    }

    @Override
    public DashboardModuleVO<List<com.spmp.billing.domain.vo.TrendDataVO>> getBillingTrend(DashboardQueryDTO queryDTO) {
        DashboardModuleVO<List<com.spmp.billing.domain.vo.TrendDataVO>> module = new DashboardModuleVO<>();
        if (!currentPermissions().contains(PERM_BILLING_STATISTICS)) {
            module.setStatus(DashboardModuleStatus.NO_PERMISSION);
            module.setMessage("无收费统计权限");
            module.setData(Collections.emptyList());
            return module;
        }
        try {
            com.spmp.billing.domain.vo.StatisticsVO billingStats = billStatisticsService
                    .getStatistics(toBillingQuery(queryDTO, true));
            List<com.spmp.billing.domain.vo.TrendDataVO> trend = billingStats.getTrends() != null
                    ? billingStats.getTrends() : Collections.emptyList();
            module.setStatus(trend.isEmpty() ? DashboardModuleStatus.EMPTY : DashboardModuleStatus.SUCCESS);
            module.setMessage(trend.isEmpty() ? "暂无收费趋势数据" : "");
            module.setData(trend);
        } catch (Exception ex) {
            log.error("加载 Dashboard 收费趋势失败", ex);
            module.setStatus(DashboardModuleStatus.ERROR);
            module.setMessage("收费趋势加载失败");
            module.setData(Collections.emptyList());
        }
        return module;
    }

    private Set<String> currentPermissions() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return Collections.emptySet();
        }
        Set<String> permissions = permissionCacheService.getUserPermissions(userId);
        return permissions != null ? permissions : Collections.emptySet();
    }

    private com.spmp.workorder.domain.dto.StatisticsQueryDTO toWorkorderQuery(DashboardQueryDTO queryDTO, boolean trendMode) {
        DateRange range = resolveRange(queryDTO.getTimeRange());
        com.spmp.workorder.domain.dto.StatisticsQueryDTO dto = new com.spmp.workorder.domain.dto.StatisticsQueryDTO();
        dto.setStartDate(range.startDate);
        dto.setEndDate(range.endDate);
        // 趋势场景只关心 start/end 日期窗口，不使用 TODAY/WEEK/MONTH 旧枚举。
        dto.setTimeRange(trendMode ? null : "MONTH");
        return dto;
    }

    private com.spmp.billing.domain.dto.StatisticsQueryDTO toBillingQuery(DashboardQueryDTO queryDTO, boolean trendMode) {
        DateRange range = resolveRange(queryDTO.getTimeRange());
        com.spmp.billing.domain.dto.StatisticsQueryDTO dto = new com.spmp.billing.domain.dto.StatisticsQueryDTO();
        dto.setStartDate(range.startDate);
        dto.setEndDate(range.endDate);
        dto.setTimeRange(trendMode ? null : "MONTH");
        return dto;
    }

    private DateRange resolveRange(String timeRange) {
        LocalDate today = LocalDate.now();
        String range = timeRange != null ? timeRange : "MONTH";
        if ("YEAR".equalsIgnoreCase(range)) {
            return new DateRange(today.withDayOfYear(1), today);
        }
        if ("QUARTER".equalsIgnoreCase(range)) {
            int quarter = (today.getMonthValue() - 1) / 3;
            int startMonth = quarter * 3 + 1;
            return new DateRange(LocalDate.of(today.getYear(), startMonth, 1), today);
        }
        return new DateRange(today.withDayOfMonth(1), today);
    }

    private Integer safeInteger(Integer value) {
        return value != null ? value : 0;
    }

    private BigDecimal safeDecimal(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private static final class DateRange {
        private final LocalDate startDate;
        private final LocalDate endDate;

        private DateRange(LocalDate startDate, LocalDate endDate) {
            this.startDate = startDate;
            this.endDate = endDate;
        }
    }
}

