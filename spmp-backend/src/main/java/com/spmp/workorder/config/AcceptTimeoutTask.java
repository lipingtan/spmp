package com.spmp.workorder.config;

import com.spmp.workorder.constant.WorkOrderAction;
import com.spmp.workorder.constant.WorkOrderConstants;
import com.spmp.workorder.constant.WorkOrderStatus;
import com.spmp.workorder.domain.entity.WorkOrderDO;
import com.spmp.workorder.domain.entity.WorkOrderLogDO;
import com.spmp.workorder.repository.WorkOrderLogMapper;
import com.spmp.workorder.repository.WorkOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 接单超时检测定时任务。
 * <p>
 * 每30分钟检测一次：
 * - 2小时未接单 → 提醒维修人员
 * - 3小时未接单 → 退回待派发状态
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AcceptTimeoutTask {

    private final WorkOrderMapper workOrderMapper;
    private final WorkOrderLogMapper workOrderLogMapper;
    private final WorkOrderEventPublisher eventPublisher;

    @Scheduled(fixedRate = 30 * 60 * 1000)
    @Transactional(rollbackFor = Exception.class)
    public void checkAcceptTimeout() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime remindTime = now.minusHours(WorkOrderConstants.ACCEPT_REMIND_HOURS);
        LocalDateTime returnTime = now.minusHours(WorkOrderConstants.ACCEPT_RETURN_HOURS);

        List<WorkOrderDO> returnOrders = workOrderMapper.selectTimeoutPendingOrders(remindTime, returnTime);

        for (WorkOrderDO order : returnOrders) {
            LocalDateTime updateTime = convertToLocalDateTime(order.getUpdateTime());
            if (updateTime == null) {
                continue;
            }

            if (!updateTime.isAfter(returnTime)) {
                String fromStatus = order.getStatus();
                order.setStatus(WorkOrderStatus.PENDING_DISPATCH.getCode());
                order.setRepairUserId(null);
                workOrderMapper.updateById(order);

                WorkOrderLogDO logDO = new WorkOrderLogDO();
                logDO.setOrderId(order.getId());
                logDO.setAction(WorkOrderAction.TIMEOUT_RETURN.getCode());
                logDO.setFromStatus(fromStatus);
                logDO.setToStatus(WorkOrderStatus.PENDING_DISPATCH.getCode());
                logDO.setOperatorType("SYSTEM");
                logDO.setRemark("接单超时（3小时未接单），退回待派发");
                logDO.setOperateTime(now);
                workOrderLogMapper.insert(logDO);

                eventPublisher.publishTimeoutReturn(order.getId(), order.getCommunityId(), order.getOrderNo());
                log.info("接单超时退回：orderId={}, orderNo={}", order.getId(), order.getOrderNo());
            } else if (!updateTime.isAfter(remindTime)) {
                if (order.getRepairUserId() != null) {
                    eventPublisher.publishTimeoutRemind(order.getId(), order.getRepairUserId(), order.getOrderNo());
                    log.info("接单超时提醒：orderId={}, orderNo={}", order.getId(), order.getOrderNo());
                }
            }
        }
    }

    private LocalDateTime convertToLocalDateTime(java.util.Date date) {
        if (date == null) {
            return null;
        }
        return new java.sql.Timestamp(date.getTime()).toLocalDateTime();
    }
}
