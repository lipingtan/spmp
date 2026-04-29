package com.spmp.owner.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.spmp.base.api.BaseApi;
import com.spmp.base.api.dto.BuildingBriefDTO;
import com.spmp.base.api.dto.CommunityBriefDTO;
import com.spmp.base.api.dto.HouseBriefDTO;
import com.spmp.base.api.dto.UnitBriefDTO;
import com.spmp.common.exception.BusinessException;
import com.spmp.owner.constant.OwnerErrorCode;
import com.spmp.owner.constant.RelationType;
import com.spmp.owner.domain.dto.PropertyBindingCreateDTO;
import com.spmp.owner.domain.entity.PropertyBindingDO;
import com.spmp.owner.domain.vo.PropertyBindingVO;
import com.spmp.owner.repository.PropertyBindingMapper;
import com.spmp.owner.service.PropertyBindingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 房产绑定服务实现类。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PropertyBindingServiceImpl implements PropertyBindingService {

    private final PropertyBindingMapper propertyBindingMapper;
    private final BaseApi baseApi;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindProperty(PropertyBindingCreateDTO createDTO) {
        // 校验关系类型
        if (!RelationType.isValid(createDTO.getRelationType())) {
            throw new BusinessException(OwnerErrorCode.BINDING_RELATION_TYPE_INVALID.getCode(),
                    OwnerErrorCode.BINDING_RELATION_TYPE_INVALID.getMessage());
        }

        // 校验房屋是否存在
        HouseBriefDTO house = baseApi.getHouseById(createDTO.getHouseId());
        if (house == null) {
            throw new BusinessException(OwnerErrorCode.BINDING_HOUSE_NOT_FOUND.getCode(),
                    OwnerErrorCode.BINDING_HOUSE_NOT_FOUND.getMessage());
        }

        // 创建绑定记录
        PropertyBindingDO bindingDO = new PropertyBindingDO();
        bindingDO.setOwnerId(createDTO.getOwnerId());
        bindingDO.setHouseId(createDTO.getHouseId());
        bindingDO.setRelationType(createDTO.getRelationType());
        bindingDO.setBindingTime(new Date());
        bindingDO.setStatus(0);

        propertyBindingMapper.insert(bindingDO);

        // 更新房屋状态为 OCCUPIED
        baseApi.updateHouseStatus(createDTO.getHouseId(), "OCCUPIED");

        log.info("房产绑定成功，bindingId={}, ownerId={}, houseId={}",
                bindingDO.getId(), createDTO.getOwnerId(), createDTO.getHouseId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unbindProperty(Long id) {
        PropertyBindingDO bindingDO = propertyBindingMapper.selectById(id);
        if (bindingDO == null) {
            throw new BusinessException(OwnerErrorCode.BINDING_NOT_FOUND.getCode(),
                    OwnerErrorCode.BINDING_NOT_FOUND.getMessage());
        }

        if (bindingDO.getStatus() != null && bindingDO.getStatus() == 1) {
            throw new BusinessException(OwnerErrorCode.BINDING_ALREADY_UNBOUND.getCode(),
                    OwnerErrorCode.BINDING_ALREADY_UNBOUND.getMessage());
        }

        // 记录解绑时间，设置状态为已解绑
        bindingDO.setUnbindingTime(new Date());
        bindingDO.setStatus(1);
        propertyBindingMapper.updateById(bindingDO);

        // 检查该房屋是否还有其他有效绑定
        Long houseId = bindingDO.getHouseId();
        LambdaQueryWrapper<PropertyBindingDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PropertyBindingDO::getHouseId, houseId)
                .eq(PropertyBindingDO::getStatus, 0);
        Long activeCount = propertyBindingMapper.selectCount(wrapper);

        if (activeCount == 0) {
            // 恢复房屋状态为 VACANT
            baseApi.updateHouseStatus(houseId, "VACANT");
            log.info("房屋 {} 已无有效绑定，恢复为空置状态", houseId);
        }

        log.info("解除绑定成功，bindingId={}, houseId={}", id, houseId);
    }

    @Override
    public List<PropertyBindingVO> listByOwnerId(Long ownerId) {
        LambdaQueryWrapper<PropertyBindingDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PropertyBindingDO::getOwnerId, ownerId)
                .eq(PropertyBindingDO::getStatus, 0)
                .orderByDesc(PropertyBindingDO::getBindingTime);

        List<PropertyBindingDO> bindings = propertyBindingMapper.selectList(wrapper);
        List<PropertyBindingVO> voList = new ArrayList<>(bindings.size());

        for (PropertyBindingDO binding : bindings) {
            PropertyBindingVO vo = convertToVO(binding);
            // 通过 BaseApi 补充房屋信息
            enrichHouseInfo(vo, binding.getHouseId());
            voList.add(vo);
        }

        return voList;
    }

    // ========== 私有方法 ==========

    /**
     * 将 PropertyBindingDO 转换为 PropertyBindingVO。
     */
    private PropertyBindingVO convertToVO(PropertyBindingDO bindingDO) {
        PropertyBindingVO vo = new PropertyBindingVO();
        vo.setId(bindingDO.getId());
        vo.setOwnerId(bindingDO.getOwnerId());
        vo.setHouseId(bindingDO.getHouseId());
        vo.setRelationType(bindingDO.getRelationType());
        vo.setBindingTime(bindingDO.getBindingTime());
        vo.setUnbindingTime(bindingDO.getUnbindingTime());
        vo.setStatus(bindingDO.getStatus());
        return vo;
    }

    /**
     * 通过 BaseApi 补充房屋信息（小区名/楼栋名/单元名/房屋编号 + 对应ID）。
     */
    private void enrichHouseInfo(PropertyBindingVO vo, Long houseId) {
        try {
            HouseBriefDTO house = baseApi.getHouseById(houseId);
            if (house != null) {
                vo.setHouseCode(house.getHouseCode());

                UnitBriefDTO unit = baseApi.getUnitById(house.getUnitId());
                if (unit != null) {
                    vo.setUnitId(unit.getId());
                    vo.setUnitName(unit.getUnitName());

                    BuildingBriefDTO building = baseApi.getBuildingById(unit.getBuildingId());
                    if (building != null) {
                        vo.setBuildingId(building.getId());
                        vo.setBuildingName(building.getBuildingName());

                        CommunityBriefDTO community = baseApi.getCommunityById(building.getCommunityId());
                        if (community != null) {
                            vo.setCommunityId(community.getId());
                            vo.setCommunityName(community.getCommunityName());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("补充房屋信息失败，houseId={}: {}", houseId, e.getMessage());
        }
    }
}
