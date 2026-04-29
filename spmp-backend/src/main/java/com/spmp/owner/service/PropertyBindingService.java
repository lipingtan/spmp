package com.spmp.owner.service;

import com.spmp.owner.domain.dto.PropertyBindingCreateDTO;
import com.spmp.owner.domain.vo.PropertyBindingVO;

import java.util.List;

/**
 * 房产绑定服务接口。
 *
 * @author 技术团队
 * @since 1.0.0
 */
public interface PropertyBindingService {

    /**
     * 房产绑定（创建绑定记录，调用 BaseApi.updateHouseStatus 更新房屋状态为 OCCUPIED）。
     *
     * @param createDTO 绑定参数
     */
    void bindProperty(PropertyBindingCreateDTO createDTO);

    /**
     * 解除绑定（记录解绑时间，若该房屋无其他绑定则调用 BaseApi 恢复为 VACANT）。
     *
     * @param id 绑定记录ID
     */
    void unbindProperty(Long id);

    /**
     * 查询业主的房产绑定列表。
     *
     * @param ownerId 业主ID
     * @return 房产绑定 VO 列表
     */
    List<PropertyBindingVO> listByOwnerId(Long ownerId);
}
