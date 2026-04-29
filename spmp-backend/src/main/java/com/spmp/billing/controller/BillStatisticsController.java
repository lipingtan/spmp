package com.spmp.billing.controller;

import com.spmp.billing.domain.dto.StatisticsQueryDTO;
import com.spmp.billing.domain.vo.StatisticsVO;
import com.spmp.billing.service.BillStatisticsService;
import com.spmp.common.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 收费统计 Controller（PC）。
 */
@RestController
@RequestMapping("/api/v1/billing/statistics")
@RequiredArgsConstructor
public class BillStatisticsController {

    private final BillStatisticsService billStatisticsService;

    @GetMapping
    public Result<StatisticsVO> getStatistics(StatisticsQueryDTO queryDTO) {
        return Result.success(billStatisticsService.getStatistics(queryDTO));
    }
}
