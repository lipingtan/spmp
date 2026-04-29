package com.spmp.workorder.config;

import com.spmp.common.exception.BusinessException;
import com.spmp.workorder.constant.WorkOrderErrorCode;
import com.spmp.workorder.constant.WorkOrderStatus;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * 工单状态机（状态流转校验）。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Component
public class WorkOrderStateMachine {

    private static final Map<WorkOrderStatus, Set<WorkOrderStatus>> TRANSITIONS = new EnumMap<>(WorkOrderStatus.class);

    static {
        TRANSITIONS.put(WorkOrderStatus.PENDING_DISPATCH, EnumSet.of(
                WorkOrderStatus.PENDING_ACCEPT,
                WorkOrderStatus.CANCELLED
        ));
        TRANSITIONS.put(WorkOrderStatus.PENDING_ACCEPT, EnumSet.of(
                WorkOrderStatus.IN_PROGRESS,
                WorkOrderStatus.PENDING_DISPATCH,
                WorkOrderStatus.CANCELLED
        ));
        TRANSITIONS.put(WorkOrderStatus.IN_PROGRESS, EnumSet.of(
                WorkOrderStatus.PENDING_VERIFY,
                WorkOrderStatus.PENDING_DISPATCH,
                WorkOrderStatus.FORCE_CLOSED
        ));
        TRANSITIONS.put(WorkOrderStatus.PENDING_VERIFY, EnumSet.of(
                WorkOrderStatus.COMPLETED,
                WorkOrderStatus.IN_PROGRESS,
                WorkOrderStatus.FORCE_CLOSED
        ));
    }

    public void checkTransition(WorkOrderStatus from, WorkOrderStatus to) {
        Set<WorkOrderStatus> allowed = TRANSITIONS.get(from);
        if (allowed == null || !allowed.contains(to)) {
            throw new BusinessException(WorkOrderErrorCode.INVALID_STATUS_TRANSITION.getCode(),
                    "工单状态不允许从 " + from.getDescription() + " 变更为 " + to.getDescription());
        }
    }
}
