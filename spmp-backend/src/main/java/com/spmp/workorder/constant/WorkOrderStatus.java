package com.spmp.workorder.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 工单状态枚举。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public enum WorkOrderStatus {

    PENDING_DISPATCH("PENDING_DISPATCH", "待派发"),
    PENDING_ACCEPT("PENDING_ACCEPT", "待接单"),
    IN_PROGRESS("IN_PROGRESS", "处理中"),
    PENDING_VERIFY("PENDING_VERIFY", "待验收"),
    COMPLETED("COMPLETED", "已完成"),
    CANCELLED("CANCELLED", "已取消"),
    FORCE_CLOSED("FORCE_CLOSED", "已关闭");

    private final String code;
    private final String description;

    public static WorkOrderStatus fromCode(String code) {
        for (WorkOrderStatus status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
