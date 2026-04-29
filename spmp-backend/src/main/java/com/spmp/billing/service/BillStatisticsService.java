package com.spmp.billing.service;

import com.spmp.billing.domain.dto.StatisticsQueryDTO;
import com.spmp.billing.domain.vo.StatisticsVO;

/**
 * 收费统计服务接口。
 */
public interface BillStatisticsService {

    StatisticsVO getStatistics(StatisticsQueryDTO queryDTO);
}
