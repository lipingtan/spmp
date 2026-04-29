package com.spmp.owner.service;

import com.spmp.owner.api.dto.OwnerBriefDTO;

import java.util.List;

/**
 * 业主缓存管理服务接口。
 *
 * @author 技术团队
 * @since 1.0.0
 */
public interface OwnerCacheService {

    // ========== 按 ID 查业主缓存 ==========

    /**
     * 获取按 ID 缓存的业主信息。
     *
     * @param ownerId 业主ID
     * @return 业主简要信息，缓存未命中返回 null
     */
    OwnerBriefDTO getOwnerById(Long ownerId);

    /**
     * 设置按 ID 缓存的业主信息。
     *
     * @param ownerId 业主ID
     * @param dto     业主简要信息
     */
    void setOwnerById(Long ownerId, OwnerBriefDTO dto);

    // ========== 按房屋查业主缓存 ==========

    /**
     * 获取按房屋 ID 缓存的业主信息。
     *
     * @param houseId 房屋ID
     * @return 业主简要信息，缓存未命中返回 null
     */
    OwnerBriefDTO getOwnerByHouseId(Long houseId);

    /**
     * 设置按房屋 ID 缓存的业主信息。
     *
     * @param houseId 房屋ID
     * @param dto     业主简要信息
     */
    void setOwnerByHouseId(Long houseId, OwnerBriefDTO dto);

    // ========== 按手机号查业主缓存 ==========

    /**
     * 获取按加密手机号缓存的业主信息。
     *
     * @param encryptedPhone 加密后的手机号
     * @return 业主简要信息，缓存未命中返回 null
     */
    OwnerBriefDTO getOwnerByPhone(String encryptedPhone);

    /**
     * 设置按加密手机号缓存的业主信息。
     *
     * @param encryptedPhone 加密后的手机号
     * @param dto            业主简要信息
     */
    void setOwnerByPhone(String encryptedPhone, OwnerBriefDTO dto);

    // ========== 按小区查业主列表缓存 ==========

    /**
     * 获取按小区 ID 缓存的业主列表。
     *
     * @param communityId 小区ID
     * @return 业主列表，缓存未命中返回 null
     */
    List<OwnerBriefDTO> getOwnersByCommunityId(Long communityId);

    /**
     * 设置按小区 ID 缓存的业主列表。
     *
     * @param communityId 小区ID
     * @param list        业主列表
     */
    void setOwnersByCommunityId(Long communityId, List<OwnerBriefDTO> list);

    // ========== 按楼栋查业主列表缓存 ==========

    /**
     * 获取按楼栋 ID 缓存的业主列表。
     *
     * @param buildingId 楼栋ID
     * @return 业主列表，缓存未命中返回 null
     */
    List<OwnerBriefDTO> getOwnersByBuildingId(Long buildingId);

    /**
     * 设置按楼栋 ID 缓存的业主列表。
     *
     * @param buildingId 楼栋ID
     * @param list       业主列表
     */
    void setOwnersByBuildingId(Long buildingId, List<OwnerBriefDTO> list);

    // ========== 业主认证状态缓存 ==========

    /**
     * 获取业主认证状态缓存。
     *
     * @param ownerId 业主ID
     * @return 是否已认证，缓存未命中返回 null
     */
    Boolean getOwnerCertified(Long ownerId);

    /**
     * 设置业主认证状态缓存。
     *
     * @param ownerId   业主ID
     * @param certified 是否已认证
     */
    void setOwnerCertified(Long ownerId, boolean certified);

    // ========== 缓存清除 ==========

    /**
     * 清除业主相关缓存。
     *
     * @param ownerId 业主ID
     */
    void clearOwnerCache(Long ownerId);

    /**
     * 清除房屋相关缓存。
     *
     * @param houseId 房屋ID
     */
    void clearHouseCache(Long houseId);

    /**
     * 清除手机号相关缓存。
     *
     * @param encryptedPhone 加密后的手机号
     */
    void clearPhoneCache(String encryptedPhone);

    /**
     * 清除小区相关缓存。
     *
     * @param communityId 小区ID
     */
    void clearCommunityCache(Long communityId);

    /**
     * 清除楼栋相关缓存。
     *
     * @param buildingId 楼栋ID
     */
    void clearBuildingCache(Long buildingId);
}
