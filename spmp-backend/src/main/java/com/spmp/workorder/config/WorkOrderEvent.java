package com.spmp.workorder.config;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.Map;

/**
 * 工单事件。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Getter
public class WorkOrderEvent extends ApplicationEvent {

    private final Long orderId;
    private final String action;
    private final Long targetUserId;
    private final Map<String, Object> data;

    public WorkOrderEvent(Object source, Long orderId, String action, Long targetUserId, Map<String, Object> data) {
        super(source);
        this.orderId = orderId;
        this.action = action;
        this.targetUserId = targetUserId;
        this.data = data;
    }
}
