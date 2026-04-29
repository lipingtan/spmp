package com.spmp.workorder.service.impl;

import com.spmp.common.util.RedisUtils;
import com.spmp.workorder.constant.WorkOrderConstants;
import com.spmp.workorder.service.WorkOrderCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 工单缓存管理服务实现。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkOrderCacheServiceImpl implements WorkOrderCacheService {

    private final RedisUtils redisUtils;

    @Override
    public void evictDetailCache(Long orderId) {
        try {
            String key = WorkOrderConstants.CACHE_DETAIL_KEY_PREFIX + orderId;
            redisUtils.delete(key);
            evictAllListCache();
        } catch (Exception e) {
            log.warn("清除工单详情缓存失败，orderId={}", orderId, e);
        }
    }

    @Override
    public void evictAllListCache() {
        try {
            redisUtils.delete(WorkOrderConstants.CACHE_LIST_KEY_PREFIX + "*");
        } catch (Exception e) {
            log.warn("清除工单列表缓存失败", e);
        }
    }
}
