package com.spmp.base.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.spmp.base.constant.BaseConstants;
import com.spmp.base.domain.entity.BuildingDO;
import com.spmp.base.domain.entity.CommunityDO;
import com.spmp.base.domain.entity.DistrictDO;
import com.spmp.base.domain.entity.HouseDO;
import com.spmp.base.domain.entity.UnitDO;
import com.spmp.base.domain.vo.CascadeTreeVO;
import com.spmp.base.domain.vo.CascadeVO;
import com.spmp.base.repository.BuildingMapper;
import com.spmp.base.repository.CommunityMapper;
import com.spmp.base.repository.DistrictMapper;
import com.spmp.base.repository.HouseMapper;
import com.spmp.base.repository.UnitMapper;
import com.spmp.base.service.CascadeService;
import com.spmp.common.util.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 级联查询服务实现。
 * <p>
 * 查询结果缓存到 Redis（TTL 24h），Redis 不可用时降级查数据库。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Slf4j
@Service
public class CascadeServiceImpl implements CascadeService {

    private final RedisUtils redisUtils;
    private final DistrictMapper districtMapper;
    private final CommunityMapper communityMapper;
    private final BuildingMapper buildingMapper;
    private final UnitMapper unitMapper;
    private final HouseMapper houseMapper;

    public CascadeServiceImpl(RedisUtils redisUtils,
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

    @SuppressWarnings("unchecked")
    @Override
    public List<CascadeVO> getCascadeData(String level, Long parentId) {
        String cacheKey = BaseConstants.CACHE_CASCADE_KEY + level + ":"
                + (parentId != null ? parentId : "root");

        // 优先从缓存获取
        List<CascadeVO> cached = redisUtils.get(cacheKey, List.class);
        if (cached != null && !cached.isEmpty()) {
            return cached;
        }

        // 缓存未命中，从数据库查询
        List<CascadeVO> result;
        switch (level.toUpperCase()) {
            case "DISTRICT":
                result = queryDistricts();
                break;
            case "COMMUNITY":
                result = queryCommunities(parentId);
                break;
            case "BUILDING":
                result = queryBuildings(parentId);
                break;
            case "UNIT":
                result = queryUnits(parentId);
                break;
            case "HOUSE":
                result = queryHouses(parentId);
                break;
            default:
                result = Collections.emptyList();
        }

        // 写入缓存
        if (!result.isEmpty()) {
            redisUtils.set(cacheKey, result, BaseConstants.CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<CascadeTreeVO> getCascadeTree(Long communityId) {
        String cacheKey = BaseConstants.CACHE_TREE_KEY + communityId;

        // 优先从缓存获取
        List<CascadeTreeVO> cached = redisUtils.get(cacheKey, List.class);
        if (cached != null && !cached.isEmpty()) {
            return cached;
        }

        // 缓存未命中，从数据库构建完整树
        List<CascadeTreeVO> tree = buildCascadeTree(communityId);

        // 写入缓存
        if (!tree.isEmpty()) {
            redisUtils.set(cacheKey, tree, BaseConstants.CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
        }
        return tree;
    }

    /**
     * 查询所有启用片区。
     */
    private List<CascadeVO> queryDistricts() {
        LambdaQueryWrapper<DistrictDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DistrictDO::getStatus, BaseConstants.STATUS_ENABLED)
                .orderByAsc(DistrictDO::getDistrictCode);
        List<DistrictDO> districts = districtMapper.selectList(wrapper);
        return districts.stream().map(d -> {
            CascadeVO vo = new CascadeVO();
            vo.setId(d.getId());
            vo.setName(d.getDistrictName());
            vo.setCode(d.getDistrictCode());
            // 检查是否有下级小区
            int count = communityMapper.countByDistrictId(d.getId());
            vo.setHasChildren(count > 0);
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 查询启用小区。
     * <p>
     * 若传入 districtId 则按片区过滤；不传则返回所有启用小区（H5 端直接选小区场景）。
     *
     * @param districtId 片区ID，可为 null
     */
    private List<CascadeVO> queryCommunities(Long districtId) {
        LambdaQueryWrapper<CommunityDO> wrapper = new LambdaQueryWrapper<>();
        if (districtId != null) {
            wrapper.eq(CommunityDO::getDistrictId, districtId);
        }
        wrapper.eq(CommunityDO::getStatus, BaseConstants.STATUS_ENABLED)
                .orderByAsc(CommunityDO::getCommunityCode);
        List<CommunityDO> communities = communityMapper.selectList(wrapper);
        return communities.stream().map(c -> {
            CascadeVO vo = new CascadeVO();
            vo.setId(c.getId());
            vo.setName(c.getCommunityName());
            vo.setCode(c.getCommunityCode());
            LambdaQueryWrapper<BuildingDO> bw = new LambdaQueryWrapper<>();
            bw.eq(BuildingDO::getCommunityId, c.getId())
                    .eq(BuildingDO::getStatus, BaseConstants.STATUS_ENABLED);
            vo.setHasChildren(buildingMapper.selectCount(bw) > 0);
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 查询指定小区下的启用楼栋。
     */
    private List<CascadeVO> queryBuildings(Long communityId) {
        if (communityId == null) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<BuildingDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BuildingDO::getCommunityId, communityId)
                .eq(BuildingDO::getStatus, BaseConstants.STATUS_ENABLED)
                .orderByAsc(BuildingDO::getBuildingCode);
        List<BuildingDO> buildings = buildingMapper.selectList(wrapper);
        return buildings.stream().map(b -> {
            CascadeVO vo = new CascadeVO();
            vo.setId(b.getId());
            vo.setName(b.getBuildingName());
            vo.setCode(b.getBuildingCode());
            LambdaQueryWrapper<UnitDO> uw = new LambdaQueryWrapper<>();
            uw.eq(UnitDO::getBuildingId, b.getId())
                    .eq(UnitDO::getStatus, BaseConstants.STATUS_ENABLED);
            vo.setHasChildren(unitMapper.selectCount(uw) > 0);
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 查询指定楼栋下的启用单元。
     */
    private List<CascadeVO> queryUnits(Long buildingId) {
        if (buildingId == null) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<UnitDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UnitDO::getBuildingId, buildingId)
                .eq(UnitDO::getStatus, BaseConstants.STATUS_ENABLED)
                .orderByAsc(UnitDO::getUnitCode);
        List<UnitDO> units = unitMapper.selectList(wrapper);
        return units.stream().map(u -> {
            CascadeVO vo = new CascadeVO();
            vo.setId(u.getId());
            vo.setName(u.getUnitName());
            vo.setCode(u.getUnitCode());
            LambdaQueryWrapper<HouseDO> hw = new LambdaQueryWrapper<>();
            hw.eq(HouseDO::getUnitId, u.getId());
            vo.setHasChildren(houseMapper.selectCount(hw) > 0);
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 查询指定单元下的房屋。
     */
    private List<CascadeVO> queryHouses(Long unitId) {
        if (unitId == null) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<HouseDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HouseDO::getUnitId, unitId)
                .orderByAsc(HouseDO::getHouseCode);
        List<HouseDO> houses = houseMapper.selectList(wrapper);
        return houses.stream().map(h -> {
            CascadeVO vo = new CascadeVO();
            vo.setId(h.getId());
            vo.setName(h.getHouseCode());
            vo.setCode(h.getHouseCode());
            vo.setHasChildren(false);
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 构建小区下的完整树形结构（楼栋→单元→房屋）。
     */
    private List<CascadeTreeVO> buildCascadeTree(Long communityId) {
        // 查询楼栋
        LambdaQueryWrapper<BuildingDO> bw = new LambdaQueryWrapper<>();
        bw.eq(BuildingDO::getCommunityId, communityId)
                .eq(BuildingDO::getStatus, BaseConstants.STATUS_ENABLED)
                .orderByAsc(BuildingDO::getBuildingCode);
        List<BuildingDO> buildings = buildingMapper.selectList(bw);

        List<CascadeTreeVO> tree = new ArrayList<>();
        for (BuildingDO building : buildings) {
            CascadeTreeVO buildingNode = new CascadeTreeVO();
            buildingNode.setId(building.getId());
            buildingNode.setName(building.getBuildingName());
            buildingNode.setCode(building.getBuildingCode());
            buildingNode.setType("BUILDING");

            // 查询单元
            LambdaQueryWrapper<UnitDO> uw = new LambdaQueryWrapper<>();
            uw.eq(UnitDO::getBuildingId, building.getId())
                    .eq(UnitDO::getStatus, BaseConstants.STATUS_ENABLED)
                    .orderByAsc(UnitDO::getUnitCode);
            List<UnitDO> units = unitMapper.selectList(uw);

            List<CascadeTreeVO> unitNodes = new ArrayList<>();
            for (UnitDO unit : units) {
                CascadeTreeVO unitNode = new CascadeTreeVO();
                unitNode.setId(unit.getId());
                unitNode.setName(unit.getUnitName());
                unitNode.setCode(unit.getUnitCode());
                unitNode.setType("UNIT");

                // 查询房屋
                LambdaQueryWrapper<HouseDO> hw = new LambdaQueryWrapper<>();
                hw.eq(HouseDO::getUnitId, unit.getId())
                        .orderByAsc(HouseDO::getHouseCode);
                List<HouseDO> houses = houseMapper.selectList(hw);

                List<CascadeTreeVO> houseNodes = houses.stream().map(h -> {
                    CascadeTreeVO houseNode = new CascadeTreeVO();
                    houseNode.setId(h.getId());
                    houseNode.setName(h.getHouseCode());
                    houseNode.setCode(h.getHouseCode());
                    houseNode.setType("HOUSE");
                    houseNode.setChildren(Collections.emptyList());
                    return houseNode;
                }).collect(Collectors.toList());

                unitNode.setChildren(houseNodes);
                unitNodes.add(unitNode);
            }

            buildingNode.setChildren(unitNodes);
            tree.add(buildingNode);
        }
        return tree;
    }
}
