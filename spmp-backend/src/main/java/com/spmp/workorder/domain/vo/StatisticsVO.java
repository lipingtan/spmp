package com.spmp.workorder.domain.vo;

import lombok.Data;

import java.util.List;

/**
 * 统计看板 VO。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class StatisticsVO {

    private Integer pendingCount;

    private Integer inProgressCount;

    private Integer monthlyCompletedCount;

    private Integer avgRepairDuration;

    private Double avgScore;

    private List<TrendDataVO> dailyTrend;

    private List<TrendDataVO> monthlyScoreTrend;
}
