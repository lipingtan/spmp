package com.spmp.workorder.service;

import com.spmp.workorder.domain.dto.WorkOrderDispatchDTO;

/**
 * 派发服务接口（手动 + 自动派发）。
 *
 * @author 技术团队
 * @since 1.0.0
 */
public interface DispatchService {

    /**
     * 手动派发工单。
     *
     * @param id          工单ID
     * @param dispatchDTO 派发参数
     */
    void manualDispatch(Long id, WorkOrderDispatchDTO dispatchDTO);

    /**
     * 自动派发工单（由定时任务或工单创建后触发）。
     *
     * @param id 工单ID
     * @return 是否成功派发
     */
    boolean autoDispatch(Long id);
}
