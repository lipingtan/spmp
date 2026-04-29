package com.spmp.workorder.service;

/**
 * 催单服务接口。
 *
 * @author 技术团队
 * @since 1.0.0
 */
public interface UrgeService {

    /**
     * 业主催单。
     *
     * @param id      工单ID
     * @param ownerId 业主ID
     */
    void urgeWorkOrder(Long id, Long ownerId);
}
