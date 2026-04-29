package com.spmp.workorder.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 工单事件监听器（站内消息 + 短信预留）。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Slf4j
@Component
public class WorkOrderEventListener {

    @Async
    @EventListener
    public void handleWorkOrderEvent(WorkOrderEvent event) {
        log.info("收到工单事件：orderId={}, action={}, targetUserId={}",
                event.getOrderId(), event.getAction(), event.getTargetUserId());

        log.info("[站内消息] userId={}, message={}", event.getTargetUserId(), event.getData().get("message"));

        log.info("[短信通知-预留] userId={}, message={}", event.getTargetUserId(), event.getData().get("message"));
    }
}
