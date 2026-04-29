package com.spmp.workorder.service.impl;

import com.spmp.common.exception.BusinessException;
import com.spmp.workorder.constant.EvaluateType;
import com.spmp.workorder.constant.WorkOrderErrorCode;
import com.spmp.workorder.constant.WorkOrderStatus;
import com.spmp.workorder.domain.dto.WorkOrderEvaluateDTO;
import com.spmp.workorder.domain.entity.EvaluationDO;
import com.spmp.workorder.domain.entity.WorkOrderDO;
import com.spmp.workorder.repository.EvaluationMapper;
import com.spmp.workorder.repository.WorkOrderMapper;
import com.spmp.workorder.service.EvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 评价服务实现。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EvaluationServiceImpl implements EvaluationService {

    private final WorkOrderMapper workOrderMapper;
    private final EvaluationMapper evaluationMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void evaluateWorkOrder(Long id, WorkOrderEvaluateDTO evaluateDTO, Long ownerId) {
        WorkOrderDO order = workOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(WorkOrderErrorCode.WORK_ORDER_NOT_FOUND.getCode(),
                    WorkOrderErrorCode.WORK_ORDER_NOT_FOUND.getMessage());
        }
        if (!ownerId.equals(order.getReporterId())) {
            throw new BusinessException(WorkOrderErrorCode.WORK_ORDER_NOT_YOURS.getCode(),
                    WorkOrderErrorCode.WORK_ORDER_NOT_YOURS.getMessage());
        }
        if (!WorkOrderStatus.COMPLETED.getCode().equals(order.getStatus())) {
            throw new BusinessException(WorkOrderErrorCode.INVALID_STATUS_TRANSITION.getCode(), "只有已完成的工单可以评价");
        }

        EvaluationDO existing = evaluationMapper.selectByOrderId(id);
        if (existing != null) {
            throw new BusinessException(WorkOrderErrorCode.EVALUATION_ALREADY_EXISTS.getCode(),
                    WorkOrderErrorCode.EVALUATION_ALREADY_EXISTS.getMessage());
        }

        EvaluationDO evaluation = new EvaluationDO();
        evaluation.setOrderId(id);
        evaluation.setScore(evaluateDTO.getScore());
        evaluation.setContent(evaluateDTO.getContent());
        evaluation.setEvaluatorId(ownerId);
        evaluation.setEvaluateType(EvaluateType.OWNER.getCode());
        evaluationMapper.insert(evaluation);

        log.info("工单评价完成：orderId={}, score={}", id, evaluateDTO.getScore());
    }
}
