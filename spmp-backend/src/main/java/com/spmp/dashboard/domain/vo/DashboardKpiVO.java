package com.spmp.dashboard.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Dashboard KPI 数据。
 */
@Data
public class DashboardKpiVO {

    private Integer pendingCount;

    private Integer inProgressCount;

    private Integer monthlyCompletedCount;

    private Integer avgRepairDuration;

    private BigDecimal totalReceivable;

    private BigDecimal totalReceived;

    private BigDecimal collectionRate;

    private BigDecimal overdueAmount;

    private Long recentNoticeCount;
}

