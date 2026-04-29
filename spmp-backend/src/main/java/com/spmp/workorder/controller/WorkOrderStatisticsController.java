package com.spmp.workorder.controller;

import com.spmp.common.result.Result;
import com.spmp.workorder.domain.dto.StatisticsQueryDTO;
import com.spmp.workorder.domain.vo.StatisticsVO;
import com.spmp.workorder.service.WorkOrderStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 工单统计 Controller（PC 管理端）。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/workorder/statistics")
@RequiredArgsConstructor
public class WorkOrderStatisticsController {

    private final WorkOrderStatisticsService statisticsService;

    /**
     * 统计看板。
     */
    @GetMapping
    @PreAuthorize("@perm.check('workorder:statistics')")
    public Result<StatisticsVO> getStatistics(StatisticsQueryDTO queryDTO) {
        return Result.success(statisticsService.getStatistics(queryDTO));
    }
}
