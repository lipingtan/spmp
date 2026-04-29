package com.spmp.workorder.service;

import com.spmp.workorder.domain.dto.WorkOrderEvaluateDTO;

/**
 * 评价服务接口。
 *
 * @author 技术团队
 * @since 1.0.0
 */
public interface EvaluationService {

    /**
     * 业主评价工单（验收通过后）。
     *
     * @param id           工单ID
     * @param evaluateDTO  评价参数
     * @param ownerId      业主ID
     */
    void evaluateWorkOrder(Long id, WorkOrderEvaluateDTO evaluateDTO, Long ownerId);
}
