package com.spmp.workorder.service.impl;

import com.spmp.workorder.domain.dto.StatisticsQueryDTO;
import com.spmp.workorder.domain.vo.StatisticsVO;
import com.spmp.workorder.domain.vo.TrendDataVO;
import com.spmp.workorder.repository.WorkOrderMapper;
import com.spmp.workorder.service.WorkOrderStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 工单统计服务实现。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkOrderStatisticsServiceImpl implements WorkOrderStatisticsService {

    private final WorkOrderMapper workOrderMapper;

    @Override
    public StatisticsVO getStatistics(StatisticsQueryDTO queryDTO) {
        LocalDateTime startDate = resolveStartDate(queryDTO);
        LocalDateTime endDate = resolveEndDate(queryDTO);

        StatisticsVO vo = new StatisticsVO();

        List<Map<String, Object>> statusCounts = workOrderMapper.countByStatus(
                queryDTO.getCommunityId(), queryDTO.getBuildingId(), startDate, endDate);

        Map<String, Integer> statusMap = new HashMap<>();
        for (Map<String, Object> row : statusCounts) {
            String status = (String) row.get("status");
            Number cnt = (Number) row.get("cnt");
            statusMap.put(status, cnt != null ? cnt.intValue() : 0);
        }

        vo.setPendingCount(statusMap.getOrDefault("PENDING_DISPATCH", 0)
                + statusMap.getOrDefault("PENDING_ACCEPT", 0));
        vo.setInProgressCount(statusMap.getOrDefault("IN_PROGRESS", 0));
        vo.setMonthlyCompletedCount(statusMap.getOrDefault("COMPLETED", 0));

        Integer avgDuration = workOrderMapper.avgRepairDuration(
                queryDTO.getCommunityId(), queryDTO.getBuildingId(), startDate, endDate);
        vo.setAvgRepairDuration(avgDuration != null ? avgDuration : 0);

        Double avgScore = workOrderMapper.avgScore(
                queryDTO.getCommunityId(), queryDTO.getBuildingId(), startDate, endDate);
        vo.setAvgScore(avgScore != null ? Math.round(avgScore * 10.0) / 10.0 : 0.0);

        List<TrendDataVO> dailyTrend = workOrderMapper.countDailyTrend(
                queryDTO.getCommunityId(), queryDTO.getBuildingId(), startDate, endDate);
        vo.setDailyTrend(dailyTrend);

        List<TrendDataVO> monthlyScoreTrend = workOrderMapper.avgScoreMonthlyTrend(
                queryDTO.getCommunityId(), queryDTO.getBuildingId(), startDate, endDate);
        vo.setMonthlyScoreTrend(monthlyScoreTrend);

        return vo;
    }

    private LocalDateTime resolveStartDate(StatisticsQueryDTO queryDTO) {
        if (queryDTO.getStartDate() != null) {
            return queryDTO.getStartDate().atStartOfDay();
        }
        String timeRange = queryDTO.getTimeRange();
        if ("TODAY".equals(timeRange)) {
            return LocalDate.now().atStartOfDay();
        } else if ("WEEK".equals(timeRange)) {
            return LocalDate.now().minusWeeks(1).atStartOfDay();
        } else if ("MONTH".equals(timeRange)) {
            return LocalDate.now().minusMonths(1).atStartOfDay();
        }
        return LocalDate.now().minusMonths(1).atStartOfDay();
    }

    private LocalDateTime resolveEndDate(StatisticsQueryDTO queryDTO) {
        if (queryDTO.getEndDate() != null) {
            return queryDTO.getEndDate().atTime(LocalTime.MAX);
        }
        return LocalDateTime.now();
    }
}
