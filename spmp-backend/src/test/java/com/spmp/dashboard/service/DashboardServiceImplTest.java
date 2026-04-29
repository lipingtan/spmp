package com.spmp.dashboard.service;

import com.spmp.billing.domain.vo.StatisticsVO;
import com.spmp.dashboard.domain.dto.DashboardQueryDTO;
import com.spmp.dashboard.domain.vo.DashboardModuleStatus;
import com.spmp.dashboard.domain.vo.DashboardModuleVO;
import com.spmp.dashboard.service.impl.DashboardServiceImpl;
import com.spmp.notice.repository.AnnouncementMapper;
import com.spmp.user.service.PermissionCacheService;
import com.spmp.workorder.domain.vo.TrendDataVO;
import com.spmp.workorder.service.WorkOrderStatisticsService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Dashboard 服务测试")
class DashboardServiceImplTest {

    @Mock
    private WorkOrderStatisticsService workOrderStatisticsService;
    @Mock
    private com.spmp.billing.service.BillStatisticsService billStatisticsService;
    @Mock
    private AnnouncementMapper announcementMapper;
    @Mock
    private PermissionCacheService permissionCacheService;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("无看板权限时 KPI 返回 NO_PERMISSION")
    void shouldReturnNoPermissionWhenNoDashboardPermission() {
        mockLoginUser(1001L);
        when(permissionCacheService.getUserPermissions(1001L)).thenReturn(Collections.emptySet());

        DashboardModuleVO<com.spmp.dashboard.domain.vo.DashboardKpiVO> result =
                dashboardService.getKpi(new DashboardQueryDTO());

        assertEquals(DashboardModuleStatus.NO_PERMISSION, result.getStatus());
    }

    @Test
    @DisplayName("有权限时 KPI 返回 SUCCESS 并聚合数据")
    void shouldReturnSuccessKpiWhenPermissionsPresent() {
        mockLoginUser(1002L);
        Set<String> perms = new HashSet<>();
        perms.add("workorder:statistics");
        perms.add("billing:statistics");
        perms.add("notice:list");
        when(permissionCacheService.getUserPermissions(1002L)).thenReturn(perms);

        com.spmp.workorder.domain.vo.StatisticsVO workorderStats = new com.spmp.workorder.domain.vo.StatisticsVO();
        workorderStats.setPendingCount(3);
        workorderStats.setInProgressCount(4);
        workorderStats.setMonthlyCompletedCount(5);
        workorderStats.setAvgRepairDuration(66);
        when(workOrderStatisticsService.getStatistics(any())).thenReturn(workorderStats);

        StatisticsVO billingStats = new StatisticsVO();
        billingStats.setTotalReceivable(new BigDecimal("1000.00"));
        billingStats.setTotalReceived(new BigDecimal("800.00"));
        billingStats.setCollectionRate(new BigDecimal("80.00"));
        billingStats.setOverdueAmount(new BigDecimal("200.00"));
        when(billStatisticsService.getStatistics(any())).thenReturn(billingStats);
        when(announcementMapper.countPublishedSince(any())).thenReturn(7L);

        DashboardModuleVO<com.spmp.dashboard.domain.vo.DashboardKpiVO> result =
                dashboardService.getKpi(new DashboardQueryDTO());

        assertEquals(DashboardModuleStatus.SUCCESS, result.getStatus());
        assertNotNull(result.getData());
        assertEquals(Integer.valueOf(3), result.getData().getPendingCount());
        assertEquals(new BigDecimal("1000.00"), result.getData().getTotalReceivable());
        assertEquals(Long.valueOf(7L), result.getData().getRecentNoticeCount());
    }

    @Test
    @DisplayName("无工单统计权限时工单趋势返回 NO_PERMISSION")
    void shouldReturnNoPermissionForWorkorderTrendWhenNoPermission() {
        mockLoginUser(1003L);
        when(permissionCacheService.getUserPermissions(1003L))
                .thenReturn(Collections.singleton("billing:statistics"));

        DashboardModuleVO<java.util.List<TrendDataVO>> result =
                dashboardService.getWorkorderTrend(new DashboardQueryDTO());

        assertEquals(DashboardModuleStatus.NO_PERMISSION, result.getStatus());
        assertEquals(0, result.getData().size());
    }

    @Test
    @DisplayName("有缴费权限但趋势为空时返回 EMPTY")
    void shouldReturnEmptyForBillingTrendWhenNoData() {
        mockLoginUser(1004L);
        when(permissionCacheService.getUserPermissions(1004L))
                .thenReturn(Collections.singleton("billing:statistics"));

        StatisticsVO billingStats = new StatisticsVO();
        billingStats.setTrends(Collections.emptyList());
        when(billStatisticsService.getStatistics(any())).thenReturn(billingStats);

        DashboardModuleVO<java.util.List<com.spmp.billing.domain.vo.TrendDataVO>> result =
                dashboardService.getBillingTrend(new DashboardQueryDTO());

        assertEquals(DashboardModuleStatus.EMPTY, result.getStatus());
        assertEquals(0, result.getData().size());
    }

    private void mockLoginUser(Long userId) {
        Claims claims = Jwts.claims();
        claims.put("userId", userId);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken("tester", null, Collections.emptyList());
        authentication.setDetails(claims);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}

