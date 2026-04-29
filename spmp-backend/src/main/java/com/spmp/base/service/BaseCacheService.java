package com.spmp.base.service;

import com.spmp.base.domain.entity.BuildingDO;
import com.spmp.base.domain.entity.CommunityDO;
import com.spmp.base.domain.entity.DistrictDO;
import com.spmp.base.domain.entity.HouseDO;
import com.spmp.base.domain.entity.UnitDO;

import java.util.List;

/**
 * 基础数据缓存管理服务接口。
 * <p>
 * 提供基础数据的 Redis 缓存读取（优先缓存，降级查 DB）和缓存清除功能。
 *
 * @author 技术团队
 * @since 1.0.0
 */
public interface BaseCacheService {

    /**
     * 获取小区缓存（缓存未命中时从数据库加载）。
     *
     * @param communityId 小区ID
     * @return 小区实体，不存在时返回 null
     */
    CommunityDO getCommunityFromCache(Long communityId);

    /**
     * 获取楼栋缓存。
     *
     * @param buildingId 楼栋ID
     * @return 楼栋实体，不存在时返回 null
     */
    BuildingDO getBuildingFromCache(Long buildingId);

    /**
     * 获取单元缓存。
     *
     * @param unitId 单元ID
     * @return 单元实体，不存在时返回 null
     */
    UnitDO getUnitFromCache(Long unitId);

    /**
     * 获取房屋缓存。
     *
     * @param houseId 房屋ID
     * @return 房屋实体，不存在时返回 null
     */
    HouseDO getHouseFromCache(Long houseId);

    /**
     * 获取所有启用片区ID列表。
     *
     * @return 启用片区ID列表
     */
    List<Long> getDistrictIdsFromCache();

    /**
     * 清除片区缓存及关联的级联缓存。
     *
     * @param districtId 片区ID
     */
    void evictDistrictCache(Long districtId);

    /**
     * 清除小区缓存及关联的级联缓存。
     *
     * @param communityId 小区ID
     * @param districtId  所属片区ID
     */
    void evictCommunityCache(Long communityId, Long districtId);

    /**
     * 清除楼栋缓存及关联的级联缓存。
     *
     * @param buildingId  楼栋ID
     * @param communityId 所属小区ID
     */
    void evictBuildingCache(Long buildingId, Long communityId);

    /**
     * 清除单元缓存及关联的级联缓存。
     *
     * @param unitId      单元ID
     * @param buildingId  所属楼栋ID
     * @param communityId 所属小区ID
     */
    void evictUnitCache(Long unitId, Long buildingId, Long communityId);

    /**
     * 清除房屋缓存及关联的级联缓存。
     *
     * @param houseId     房屋ID
     * @param unitId      所属单元ID
     * @param communityId 所属小区ID
     */
    void evictHouseCache(Long houseId, Long unitId, Long communityId);

    /**
     * 缓存预热（系统启动时调用）。
     * <p>
     * 加载所有启用片区ID列表、片区/小区/楼栋基本信息到 Redis。
     */
    void warmupCache();
}
