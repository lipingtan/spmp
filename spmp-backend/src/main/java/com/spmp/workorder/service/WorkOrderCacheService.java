package com.spmp.workorder.service;

/**
 * 工单缓存管理服务接口。
 *
 * @author 技术团队
 * @since 1.0.0
 */
public interface WorkOrderCacheService {

    /**
     * 清除工单详情缓存。
     *
     * @param orderId 工单ID
     */
    void evictDetailCache(Long orderId);

    /**
     * 清除所有工单列表缓存。
     */
    void evictAllListCache();
}
