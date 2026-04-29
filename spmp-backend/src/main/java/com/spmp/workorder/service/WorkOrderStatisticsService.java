package com.spmp.workorder.service;

import com.spmp.workorder.domain.dto.StatisticsQueryDTO;
import com.spmp.workorder.domain.vo.StatisticsVO;

/**
 * 工单统计服务接口。
 *
 * @author 技术团队
 * @since 1.0.0
 */
public interface WorkOrderStatisticsService {

    /**
     * 获取统计看板数据。
     *
     * @param queryDTO 统计查询参数
     * @return 统计数据
     */
    StatisticsVO getStatistics(StatisticsQueryDTO queryDTO);
}
