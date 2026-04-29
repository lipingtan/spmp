package com.spmp.workorder.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 工单事件发布器。
 * <p>
 * 用于发布工单相关事件，如派发、接单、完成、取消等。
 * 
 * @author 技术团队
 * @since 1.0.0
 */
@Slf4j
@Component
public class WorkOrderEventPublisher {

    /**
     * 发布派发工单事件。
     * 
     * @param orderId     工单ID
     * @param repairUserId 维修人员ID
     * @param orderNo     工单号
     */
    public void publishDispatch(Long orderId, Long repairUserId, String orderNo) {
        log.info("发布工单派发事件 - 工单ID: {}, 维修人员ID: {}, 工单号: {}", orderId, repairUserId, orderNo);
        // 实际项目中可通过消息队列或事件总线发布事件
    }

    /**
     * 发布取消工单事件。
     * 
     * @param orderId     工单ID
     * @param repairUserId 维修人员ID
     * @param orderNo     工单号
     */
    public void publishCancel(Long orderId, Long repairUserId, String orderNo) {
        log.info("发布工单取消事件 - 工单ID: {}, 维修人员ID: {}, 工单号: {}", orderId, repairUserId, orderNo);
        // 实际项目中可通过消息队列或事件总线发布事件
    }

    /**
     * 发布接单事件。
     * 
     * @param orderId     工单ID
     * @param reporterId  报修人ID
     * @param orderNo     工单号
     */
    public void publishAccept(Long orderId, Long reporterId, String orderNo) {
        log.info("发布工单接单事件 - 工单ID: {}, 报修人ID: {}, 工单号: {}", orderId, reporterId, orderNo);
        // 实际项目中可通过消息队列或事件总线发布事件
    }

    /**
     * 发布完成维修事件。
     * 
     * @param orderId     工单ID
     * @param reporterId  报修人ID
     * @param orderNo     工单号
     */
    public void publishComplete(Long orderId, Long reporterId, String orderNo) {
        log.info("发布工单完成事件 - 工单ID: {}, 报修人ID: {}, 工单号: {}", orderId, reporterId, orderNo);
        // 实际项目中可通过消息队列或事件总线发布事件
    }

    /**
     * 发布验收不通过事件。
     * 
     * @param orderId     工单ID
     * @param repairUserId 维修人员ID
     * @param orderNo     工单号
     * @param rejectCount 不通过次数
     */
    public void publishVerifyReject(Long orderId, Long repairUserId, String orderNo, int rejectCount) {
        log.info("发布工单验收不通过事件 - 工单ID: {}, 维修人员ID: {}, 工单号: {}, 不通过次数: {}", 
                orderId, repairUserId, orderNo, rejectCount);
        // 实际项目中可通过消息队列或事件总线发布事件
    }

    /**
     * 发布验收不通过升级事件。
     * 
     * @param orderId      工单ID
     * @param communityId  社区ID
     * @param orderNo      工单号
     */
    public void publishRejectEscalate(Long orderId, Long communityId, String orderNo) {
        log.info("发布工单验收不通过升级事件 - 工单ID: {}, 社区ID: {}, 工单号: {}", 
                orderId, communityId, orderNo);
        // 实际项目中可通过消息队列或事件总线发布事件
    }

    /**
     * 发布超时退回事件。
     * 
     * @param orderId      工单ID
     * @param communityId  社区ID
     * @param orderNo      工单号
     */
    public void publishTimeoutReturn(Long orderId, Long communityId, String orderNo) {
        log.info("发布工单超时退回事件 - 工单ID: {}, 社区ID: {}, 工单号: {}", 
                orderId, communityId, orderNo);
        // 实际项目中可通过消息队列或事件总线发布事件
    }

    /**
     * 发布超时提醒事件。
     * 
     * @param orderId      工单ID
     * @param communityId  社区ID
     * @param orderNo      工单号
     */
    public void publishTimeoutRemind(Long orderId, Long communityId, String orderNo) {
        log.info("发布工单超时提醒事件 - 工单ID: {}, 社区ID: {}, 工单号: {}", 
                orderId, communityId, orderNo);
        // 实际项目中可通过消息队列或事件总线发布事件
    }

    /**
     * 发布催单事件。
     * 
     * @param orderId      工单ID
     * @param communityId  社区ID
     * @param orderNo      工单号
     */
    public void publishUrge(Long orderId, Long communityId, String orderNo) {
        log.info("发布工单催单事件 - 工单ID: {}, 社区ID: {}, 工单号: {}", 
                orderId, communityId, orderNo);
        // 实际项目中可通过消息队列或事件总线发布事件
    }
}
