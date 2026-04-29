package com.spmp.workorder.service.impl;

import com.spmp.common.exception.BusinessException;
import com.spmp.workorder.config.WorkOrderEventPublisher;
import com.spmp.workorder.constant.WorkOrderAction;
import com.spmp.workorder.constant.WorkOrderErrorCode;
import com.spmp.workorder.domain.entity.UrgeRecordDO;
import com.spmp.workorder.domain.entity.WorkOrderDO;
import com.spmp.workorder.domain.entity.WorkOrderLogDO;
import com.spmp.workorder.repository.UrgeRecordMapper;
import com.spmp.workorder.repository.WorkOrderLogMapper;
import com.spmp.workorder.repository.WorkOrderMapper;
import com.spmp.workorder.service.UrgeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 催单服务实现。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Slf4j
@Service("workorderUrgeServiceImpl")
@RequiredArgsConstructor
public class UrgeServiceImpl implements UrgeService {

    private final WorkOrderMapper workOrderMapper;
    private final UrgeRecordMapper urgeRecordMapper;
    private final WorkOrderLogMapper workOrderLogMapper;
    private final WorkOrderEventPublisher eventPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void urgeWorkOrder(Long id, Long ownerId) {
        WorkOrderDO order = workOrderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(WorkOrderErrorCode.WORK_ORDER_NOT_FOUND.getCode(),
                    WorkOrderErrorCode.WORK_ORDER_NOT_FOUND.getMessage());
        }
        if (!ownerId.equals(order.getReporterId())) {
            throw new BusinessException(WorkOrderErrorCode.WORK_ORDER_NOT_YOURS.getCode(),
                    WorkOrderErrorCode.WORK_ORDER_NOT_YOURS.getMessage());
        }

        int newUrgeCount = order.getUrgeCount() + 1;
        order.setUrgeCount(newUrgeCount);
        order.setLastUrgeTime(LocalDateTime.now());
        workOrderMapper.updateById(order);

        UrgeRecordDO record = new UrgeRecordDO();
        record.setOrderId(id);
        record.setUrgeUserId(ownerId);
        record.setUrgeTime(LocalDateTime.now());
        urgeRecordMapper.insert(record);

        WorkOrderLogDO logDO = new WorkOrderLogDO();
        logDO.setOrderId(id);
        logDO.setAction(WorkOrderAction.URGE.getCode());
        logDO.setFromStatus(order.getStatus());
        logDO.setToStatus(order.getStatus());
        logDO.setOperatorId(ownerId);
        logDO.setOperatorType("OWNER");
        logDO.setRemark("业主催单（第" + newUrgeCount + "次）");
        logDO.setOperateTime(LocalDateTime.now());
        workOrderLogMapper.insert(logDO);

        if (order.getRepairUserId() != null) {
            eventPublisher.publishUrge(id, order.getRepairUserId(), order.getOrderNo());
        }

        log.info("催单成功：orderId={}, urgeCount={}", id, newUrgeCount);
    }
}
