package com.spmp.base.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.spmp.base.constant.BaseConstants;
import com.spmp.base.domain.entity.BuildingDO;
import com.spmp.base.domain.entity.CommunityDO;
import com.spmp.base.domain.entity.DistrictDO;
import com.spmp.base.domain.entity.HouseDO;
import com.spmp.base.domain.entity.UnitDO;
import com.spmp.base.repository.BuildingMapper;
import com.spmp.base.repository.CommunityMapper;
import com.spmp.base.repository.DistrictMapper;
import com.spmp.base.repository.HouseMapper;
import com.spmp.base.repository.UnitMapper;
import com.spmp.base.service.BaseCacheService;
import com.spmp.common.util.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 基础数据缓存管理服务实现。
 * <p>
 * 所有缓存操作优先 Redis，Redis 不可用时降级查数据库 + WARN 日志。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Slf4j
@Service
public class BaseCacheServiceImpl implements BaseCacheService {

    private final RedisUtils redisUtils;
    private final DistrictMapper districtMapper;
    private final CommunityMapper communityMapper;
    private final BuildingMapper buildingMapper;
    private final UnitMapper unitMapper;
    private final HouseMapper houseMapper;

    public BaseCacheServiceImpl(RedisUtils redisUtils,
                                DistrictMapper districtMapper,
                                CommunityMapper communityMapper,
                                BuildingMapper buildingMapper,
                                UnitMapper unitMapper,
                                HouseMapper houseMapper) {
        this.redisUtils = redisUtils;
        this.districtMapper = districtMapper;
        this.communityMapper = communityMapper;
        this.buildingMapper = buildingMapper;
        this.unitMapper = unitMapper;
        this.houseMapper = houseMapper;
    }

    @Override
    public CommunityDO getCommunityFromCache(Long communityId) {
        String key = BaseConstants.CACHE_COMMUNITY_KEY + communityId;
        CommunityDO cached = redisUtils.get(key, CommunityDO.class);
        if (cached != null) {
            return cached;
        }
        // 缓存未命中或 Redis 不可用，从数据库加载
        CommunityDO community = communityMapper.selectById(communityId);
        if (community == null || community.getDelFlag() != null && community.getDelFlag() == 1) {
            return null;
        }
        redisUtils.set(key, community, BaseConstants.CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
        return community;
    }

    @Override
    public BuildingDO getBuildingFromCache(Long buildingId) {
        String key = BaseConstants.CACHE_BUILDING_KEY + buildingId;
        BuildingDO cached = redisUtils.get(key, BuildingDO.class);
        if (cached != null) {
            return cached;
        }
        BuildingDO building = buildingMapper.selectById(buildingId);
        if (building == null || building.getDelFlag() != null && building.getDelFlag() == 1) {
            return null;
        }
        redisUtils.set(key, building, BaseConstants.CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
        return building;
    }

    @Override
    public UnitDO getUnitFromCache(Long unitId) {
        String key = BaseConstants.CACHE_UNIT_KEY + unitId;
        UnitDO cached = redisUtils.get(key, UnitDO.class);
        if (cached != null) {
            return cached;
        }
        UnitDO unit = unitMapper.selectById(unitId);
        if (unit == null || unit.getDelFlag() != null && unit.getDelFlag() == 1) {
            return null;
        }
        redisUtils.set(key, unit, BaseConstants.CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
        return unit;
    }

    @Override
    public HouseDO getHouseFromCache(Long houseId) {
        String key = BaseConstants.CACHE_HOUSE_KEY + houseId;
        HouseDO cached = redisUtils.get(key, HouseDO.class);
        if (cached != null) {
            return cached;
        }
        HouseDO house = houseMapper.selectById(houseId);
        if (house == null || house.getDelFlag() != null && house.getDelFlag() == 1) {
            return null;
        }
        redisUtils.set(key, house, BaseConstants.CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
        return house;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Long> getDistrictIdsFromCache() {
        String key = BaseConstants.CACHE_DISTRICT_ALL_IDS_KEY;
        List<Long> cached = redisUtils.get(key, List.class);
        if (cached != null && !cached.isEmpty()) {
            return cached;
        }
        // 从数据库加载所有启用片区ID
        LambdaQueryWrapper<DistrictDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DistrictDO::getStatus, BaseConstants.STATUS_ENABLED)
                .select(DistrictDO::getId);
        List<Long> ids = districtMapper.selectList(wrapper).stream()
                .map(DistrictDO::getId)
                .collect(Collectors.toList());
        redisUtils.set(key, ids, BaseConstants.CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
        return ids;
    }

    @Override
    public void evictDistrictCache(Long districtId) {
        List<String> keys = new ArrayList<>();
        keys.add(BaseConstants.CACHE_DISTRICT_KEY + districtId);
        keys.add(BaseConstants.CACHE_DISTRICT_ALL_IDS_KEY);
        // 清除级联查询缓存（片区级别）
        keys.add(BaseConstants.CACHE_CASCADE_KEY + "DISTRICT:" + districtId);
        // 片区变更可能影响 DISTRICT 级别的级联查询（无 parentId 的情况）
        keys.add(BaseConstants.CACHE_CASCADE_KEY + "DISTRICT:root");
        redisUtils.delete(keys);
        log.info("清除片区缓存, districtId={}", districtId);
    }

    @Override
    public void evictCommunityCache(Long communityId, Long districtId) {
        List<String> keys = new ArrayList<>();
        keys.add(BaseConstants.CACHE_COMMUNITY_KEY + communityId);
        // 清除级联查询缓存
        keys.add(BaseConstants.CACHE_CASCADE_KEY + "COMMUNITY:" + districtId);
        keys.add(BaseConstants.CACHE_CASCADE_KEY + "DISTRICT:root");
        keys.add(BaseConstants.CACHE_DISTRICT_ALL_IDS_KEY);
        redisUtils.delete(keys);
        log.info("清除小区缓存, communityId={}, districtId={}", communityId, districtId);
    }

    @Override
    public void evictBuildingCache(Long buildingId, Long communityId) {
        List<String> keys = new ArrayList<>();
        keys.add(BaseConstants.CACHE_BUILDING_KEY + buildingId);
        keys.add(BaseConstants.CACHE_CASCADE_KEY + "BUILDING:" + communityId);
        keys.add(BaseConstants.CACHE_TREE_KEY + communityId);
        redisUtils.delete(keys);
        log.info("清除楼栋缓存, buildingId={}, communityId={}", buildingId, communityId);
    }

    @Override
    public void evictUnitCache(Long unitId, Long buildingId, Long communityId) {
        List<String> keys = new ArrayList<>();
        keys.add(BaseConstants.CACHE_UNIT_KEY + unitId);
        keys.add(BaseConstants.CACHE_CASCADE_KEY + "UNIT:" + buildingId);
        keys.add(BaseConstants.CACHE_TREE_KEY + communityId);
        redisUtils.delete(keys);
        log.info("清除单元缓存, unitId={}, buildingId={}, communityId={}", unitId, buildingId, communityId);
    }

    @Override
    public void evictHouseCache(Long houseId, Long unitId, Long communityId) {
        List<String> keys = new ArrayList<>();
        keys.add(BaseConstants.CACHE_HOUSE_KEY + houseId);
        keys.add(BaseConstants.CACHE_CASCADE_KEY + "HOUSE:" + unitId);
        keys.add(BaseConstants.CACHE_TREE_KEY + communityId);
        redisUtils.delete(keys);
        log.info("清除房屋缓存, houseId={}, unitId={}, communityId={}", houseId, unitId, communityId);
    }

    @Override
    public void warmupCache() {
        log.info("开始基础数据缓存预热...");
        try {
            // 预热所有启用片区ID列表
            getDistrictIdsFromCache();

            // 预热所有启用片区基本信息
            LambdaQueryWrapper<DistrictDO> districtWrapper = new LambdaQueryWrapper<>();
            districtWrapper.eq(DistrictDO::getStatus, BaseConstants.STATUS_ENABLED);
            List<DistrictDO> districts = districtMapper.selectList(districtWrapper);
            for (DistrictDO district : districts) {
                String key = BaseConstants.CACHE_DISTRICT_KEY + district.getId();
                redisUtils.set(key, district, BaseConstants.CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
            }
            log.info("片区缓存预热完成, count={}", districts.size());

            // 预热所有启用小区基本信息
            LambdaQueryWrapper<CommunityDO> communityWrapper = new LambdaQueryWrapper<>();
            communityWrapper.eq(CommunityDO::getStatus, BaseConstants.STATUS_ENABLED);
            List<CommunityDO> communities = communityMapper.selectList(communityWrapper);
            for (CommunityDO community : communities) {
                String key = BaseConstants.CACHE_COMMUNITY_KEY + community.getId();
                redisUtils.set(key, community, BaseConstants.CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
            }
            log.info("小区缓存预热完成, count={}", communities.size());

            // 预热所有启用楼栋基本信息
            LambdaQueryWrapper<BuildingDO> buildingWrapper = new LambdaQueryWrapper<>();
            buildingWrapper.eq(BuildingDO::getStatus, BaseConstants.STATUS_ENABLED);
            List<BuildingDO> buildings = buildingMapper.selectList(buildingWrapper);
            for (BuildingDO building : buildings) {
                String key = BaseConstants.CACHE_BUILDING_KEY + building.getId();
                redisUtils.set(key, building, BaseConstants.CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
            }
            log.info("楼栋缓存预热完成, count={}", buildings.size());

            log.info("基础数据缓存预热全部完成");
        } catch (Exception e) {
            log.warn("基础数据缓存预热失败，将在首次查询时加载: {}", e.getMessage());
        }
    }
}
