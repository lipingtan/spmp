package com.spmp.workorder.service.impl;

import com.spmp.common.exception.BusinessException;
import com.spmp.workorder.config.DispatchStrategyEngine;
import com.spmp.workorder.config.WorkOrderEventPublisher;
import com.spmp.workorder.config.WorkOrderStateMachine;
import com.spmp.workorder.constant.DispatchType;
import com.spmp.workorder.constant.WorkOrderAction;
import com.spmp.workorder.constant.WorkOrderErrorCode;
import com.spmp.workorder.constant.WorkOrderStatus;
import com.spmp.workorder.domain.dto.WorkOrderDispatchDTO;
import com.spmp.workorder.domain.entity.DispatchRecordDO;
import com.spmp.workorder.domain.entity.WorkOrderDO;
import com.spmp.workorder.domain.entity.WorkOrderLogDO;
import com.spmp.workorder.repository.DispatchRecordMapper;
import com.spmp.workorder.repository.WorkOrderLogMapper;
import com.spmp.workorder.repository.WorkOrderMapper;
import com.spmp.workorder.service.DispatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 派发服务实现。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DispatchServiceImpl implements DispatchService {

    private final WorkOrderMapper workOrderMapper;
    private final DispatchRecordMapper dispatchRecordMapper;
    private final WorkOrderLogMapper workOrderLogMapper;
    private final WorkOrderStateMachine stateMachine;
    private final WorkOrderEventPublisher eventPublisher;
    private final DispatchStrategyEngine strategyEngine;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void manualDispatch(Long id, WorkOrderDispatchDTO dispatchDTO) {
        WorkOrderDO order = getWorkOrderOrThrow(id);
        stateMachine.checkTransition(
                WorkOrderStatus.fromCode(order.getStatus()),
                WorkOrderStatus.PENDING_ACCEPT
        );

        order.setRepairUserId(dispatchDTO.getRepairUserId());
        order.setStatus(WorkOrderStatus.PENDING_ACCEPT.getCode());
        if (dispatchDTO.getExpectedCompleteTime() != null) {
            order.setExpectedCompleteTime(dispatchDTO.getExpectedCompleteTime());
        }
        workOrderMapper.updateById(order);

        DispatchRecordDO record = new DispatchRecordDO();
        record.setOrderId(id);
        record.setRepairUserId(dispatchDTO.getRepairUserId());
        record.setDispatchType(DispatchType.MANUAL.getCode());
        record.setRemark(dispatchDTO.getRemark());
        record.setDispatchTime(LocalDateTime.now());
        dispatchRecordMapper.insert(record);

        saveLog(id, WorkOrderAction.DISPATCH, WorkOrderStatus.PENDING_DISPATCH.getCode(),
                WorkOrderStatus.PENDING_ACCEPT.getCode(), null, "手动派发");

        eventPublisher.publishDispatch(id, dispatchDTO.getRepairUserId(), order.getOrderNo());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean autoDispatch(Long id) {
        WorkOrderDO order = getWorkOrderOrThrow(id);
        if (!WorkOrderStatus.PENDING_DISPATCH.getCode().equals(order.getStatus())) {
            return false;
        }

        Long repairUserId = strategyEngine.autoSelectRepairUser(order);
        if (repairUserId == null) {
            log.warn("自动派发失败，无可用维修人员，工单：{}", order.getOrderNo());
            return false;
        }

        order.setRepairUserId(repairUserId);
        order.setStatus(WorkOrderStatus.PENDING_ACCEPT.getCode());
        workOrderMapper.updateById(order);

        DispatchRecordDO record = new DispatchRecordDO();
        record.setOrderId(id);
        record.setRepairUserId(repairUserId);
        record.setDispatchType(DispatchType.AUTO.getCode());
        record.setDispatchTime(LocalDateTime.now());
        dispatchRecordMapper.insert(record);

        saveLog(id, WorkOrderAction.AUTO_DISPATCH, WorkOrderStatus.PENDING_DISPATCH.getCode(),
                WorkOrderStatus.PENDING_ACCEPT.getCode(), null, "自动派发");

        eventPublisher.publishDispatch(id, repairUserId, order.getOrderNo());
        return true;
    }

    private WorkOrderDO getWorkOrderOrThrow(Long id) {
        WorkOrderDO order = workOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(WorkOrderErrorCode.WORK_ORDER_NOT_FOUND.getCode(),
                    WorkOrderErrorCode.WORK_ORDER_NOT_FOUND.getMessage());
        }
        return order;
    }

    private void saveLog(Long orderId, WorkOrderAction action, String fromStatus,
                          String toStatus, Long operatorId, String remark) {
        WorkOrderLogDO logDO = new WorkOrderLogDO();
        logDO.setOrderId(orderId);
        logDO.setAction(action.getCode());
        logDO.setFromStatus(fromStatus);
        logDO.setToStatus(toStatus);
        logDO.setOperatorId(operatorId);
        logDO.setOperatorType("SYSTEM");
        logDO.setRemark(remark);
        logDO.setOperateTime(LocalDateTime.now());
        workOrderLogMapper.insert(logDO);
    }
}
