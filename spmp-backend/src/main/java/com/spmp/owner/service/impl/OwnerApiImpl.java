package com.spmp.owner.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.spmp.common.util.EncryptUtils;
import com.spmp.owner.api.OwnerApi;
import com.spmp.owner.api.dto.OwnerBriefDTO;
import com.spmp.owner.constant.OwnerStatus;
import com.spmp.owner.domain.entity.OwnerDO;
import com.spmp.owner.domain.entity.PropertyBindingDO;
import com.spmp.owner.repository.OwnerMapper;
import com.spmp.owner.repository.PropertyBindingMapper;
import com.spmp.owner.service.OwnerCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 业主对外 API 实现类。
 * <p>
 * 优先查 Redis 缓存，未命中查数据库并回写缓存。
 * Redis 不可用时降级查数据库。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OwnerApiImpl implements OwnerApi {

    private final OwnerMapper ownerMapper;
    private final PropertyBindingMapper propertyBindingMapper;
    private final EncryptUtils encryptUtils;
    private final OwnerCacheService ownerCacheService;

    @Override
    public OwnerBriefDTO getOwnerById(Long ownerId) {
        // 优先查缓存
        OwnerBriefDTO cached = ownerCacheService.getOwnerById(ownerId);
        if (cached != null) {
            return cached;
        }

        // 缓存未命中，查数据库
        OwnerDO ownerDO = ownerMapper.selectById(ownerId);
        if (ownerDO == null) {
            return null;
        }

        OwnerBriefDTO dto = convertToBriefDTO(ownerDO);
        // 回写缓存
        ownerCacheService.setOwnerById(ownerId, dto);
        return dto;
    }

    @Override
    public OwnerBriefDTO getOwnerByHouseId(Long houseId) {
        // 优先查缓存
        OwnerBriefDTO cached = ownerCacheService.getOwnerByHouseId(houseId);
        if (cached != null) {
            return cached;
        }

        // 缓存未命中，通过房产绑定表查找业主
        LambdaQueryWrapper<PropertyBindingDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PropertyBindingDO::getHouseId, houseId)
                .eq(PropertyBindingDO::getStatus, 0)
                .orderByAsc(PropertyBindingDO::getBindingTime)
                .last("LIMIT 1");
        PropertyBindingDO binding = propertyBindingMapper.selectOne(wrapper);
        if (binding == null) {
            return null;
        }

        OwnerDO ownerDO = ownerMapper.selectById(binding.getOwnerId());
        if (ownerDO == null) {
            return null;
        }

        OwnerBriefDTO dto = convertToBriefDTO(ownerDO);
        // 回写缓存
        ownerCacheService.setOwnerByHouseId(houseId, dto);
        return dto;
    }

    @Override
    public OwnerBriefDTO getOwnerByPhone(String phone) {
        // 加密手机号
        String encryptedPhone = encryptUtils.encrypt(phone);

        // 优先查缓存
        OwnerBriefDTO cached = ownerCacheService.getOwnerByPhone(encryptedPhone);
        if (cached != null) {
            return cached;
        }

        // 缓存未命中，查数据库
        OwnerDO ownerDO = ownerMapper.selectByEncryptedPhone(encryptedPhone);
        if (ownerDO == null) {
            return null;
        }

        OwnerBriefDTO dto = convertToBriefDTO(ownerDO);
        // 回写缓存
        ownerCacheService.setOwnerByPhone(encryptedPhone, dto);
        return dto;
    }

    @Override
    public List<OwnerBriefDTO> listOwnersByCommunityId(Long communityId) {
        // 优先查缓存
        List<OwnerBriefDTO> cached = ownerCacheService.getOwnersByCommunityId(communityId);
        if (cached != null) {
            return cached;
        }

        // 缓存未命中，通过房产绑定表 + 房屋表查找业主
        // 查询该小区下所有有效绑定的业主ID（去重）
        List<OwnerBriefDTO> result = queryOwnersByBindingCondition(communityId, true);

        // 回写缓存
        ownerCacheService.setOwnersByCommunityId(communityId, result);
        return result;
    }

    @Override
    public List<OwnerBriefDTO> listOwnersByBuildingId(Long buildingId) {
        // 优先查缓存
        List<OwnerBriefDTO> cached = ownerCacheService.getOwnersByBuildingId(buildingId);
        if (cached != null) {
            return cached;
        }

        // 缓存未命中，查数据库
        List<OwnerBriefDTO> result = queryOwnersByBindingCondition(buildingId, false);

        // 回写缓存
        ownerCacheService.setOwnersByBuildingId(buildingId, result);
        return result;
    }

    @Override
    public boolean checkOwnerCertified(Long ownerId) {
        // 优先查缓存
        Boolean cached = ownerCacheService.getOwnerCertified(ownerId);
        if (cached != null) {
            return cached;
        }

        // 缓存未命中，查数据库
        OwnerDO ownerDO = ownerMapper.selectById(ownerId);
        if (ownerDO == null) {
            return false;
        }

        boolean certified = OwnerStatus.CERTIFIED.getCode().equals(ownerDO.getOwnerStatus());
        // 回写缓存
        ownerCacheService.setOwnerCertified(ownerId, certified);
        return certified;
    }

    // ========== 私有方法 ==========

    /**
     * 将 OwnerDO 转换为 OwnerBriefDTO（含脱敏）。
     */
    private OwnerBriefDTO convertToBriefDTO(OwnerDO ownerDO) {
        OwnerBriefDTO dto = new OwnerBriefDTO();
        dto.setId(ownerDO.getId());
        dto.setUserId(ownerDO.getUserId());
        dto.setOwnerName(ownerDO.getOwnerName());
        // 解密后脱敏
        try {
            String decryptedPhone = encryptUtils.decrypt(ownerDO.getPhone());
            dto.setPhoneMasked(EncryptUtils.mask(decryptedPhone, 3, 4));
        } catch (Exception e) {
            log.warn("解密手机号失败，ownerId={}: {}", ownerDO.getId(), e.getMessage());
            dto.setPhoneMasked("***");
        }
        dto.setOwnerStatus(ownerDO.getOwnerStatus());
        return dto;
    }

    /**
     * 根据绑定条件查询业主列表。
     * <p>
     * 简化实现：查询所有有效绑定记录，获取去重的业主ID列表，再批量查询业主信息。
     * 注意：小区/楼栋筛选需要关联 bs_house 表，此处简化为查询所有绑定后过滤。
     *
     * @param areaId      小区ID 或 楼栋ID
     * @param isCommunity true=按小区查询，false=按楼栋查询
     * @return 业主列表
     */
    private List<OwnerBriefDTO> queryOwnersByBindingCondition(Long areaId, boolean isCommunity) {
        // 查询所有有效绑定记录
        LambdaQueryWrapper<PropertyBindingDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PropertyBindingDO::getStatus, 0);
        List<PropertyBindingDO> bindings = propertyBindingMapper.selectList(wrapper);

        if (bindings.isEmpty()) {
            return Collections.emptyList();
        }

        // 获取去重的业主ID列表
        List<Long> ownerIds = bindings.stream()
                .map(PropertyBindingDO::getOwnerId)
                .distinct()
                .collect(Collectors.toList());

        if (ownerIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 批量查询业主信息
        List<OwnerDO> owners = ownerMapper.selectBatchIds(ownerIds);
        return owners.stream()
                .map(this::convertToBriefDTO)
                .collect(Collectors.toList());
    }
}
