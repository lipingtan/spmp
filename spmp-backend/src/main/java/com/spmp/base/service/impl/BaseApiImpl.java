package com.spmp.base.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.spmp.base.api.BaseApi;
import com.spmp.base.api.dto.BuildingBriefDTO;
import com.spmp.base.api.dto.CascadeTreeDTO;
import com.spmp.base.api.dto.CommunityBriefDTO;
import com.spmp.base.api.dto.HouseBriefDTO;
import com.spmp.base.api.dto.UnitBriefDTO;
import com.spmp.base.constant.BaseConstants;
import com.spmp.base.domain.entity.BuildingDO;
import com.spmp.base.domain.entity.CommunityDO;
import com.spmp.base.domain.entity.HouseDO;
import com.spmp.base.domain.entity.UnitDO;
import com.spmp.base.domain.vo.CascadeTreeVO;
import com.spmp.base.repository.BuildingMapper;
import com.spmp.base.repository.CommunityMapper;
import com.spmp.base.repository.HouseMapper;
import com.spmp.base.repository.UnitMapper;
import com.spmp.base.domain.dto.HouseStatusChangeDTO;
import com.spmp.base.service.BaseCacheService;
import com.spmp.base.service.CascadeService;
import com.spmp.base.service.HouseService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 基础数据对外 API 实现。
 * <p>
 * 所有查询方法优先从 BaseCacheService 获取缓存数据，
 * Redis 不可用时降级查数据库。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Slf4j
@Service
public class BaseApiImpl implements BaseApi {

    private final BaseCacheService baseCacheService;
    private final CascadeService cascadeService;
    private final CommunityMapper communityMapper;
    private final BuildingMapper buildingMapper;
    private final UnitMapper unitMapper;
    private final HouseMapper houseMapper;
    private final HouseService houseService;

    public BaseApiImpl(BaseCacheService baseCacheService,
                       CascadeService cascadeService,
                       CommunityMapper communityMapper,
                       BuildingMapper buildingMapper,
                       UnitMapper unitMapper,
                       HouseMapper houseMapper,
                       @Lazy HouseService houseService) {
        this.baseCacheService = baseCacheService;
        this.cascadeService = cascadeService;
        this.communityMapper = communityMapper;
        this.buildingMapper = buildingMapper;
        this.unitMapper = unitMapper;
        this.houseMapper = houseMapper;
        this.houseService = houseService;
    }

    @Override
    public CommunityBriefDTO getCommunityById(Long communityId) {
        if (communityId == null) {
            return null;
        }
        CommunityDO community = baseCacheService.getCommunityFromCache(communityId);
        return convertToCommunityBriefDTO(community);
    }

    @Override
    public BuildingBriefDTO getBuildingById(Long buildingId) {
        if (buildingId == null) {
            return null;
        }
        BuildingDO building = baseCacheService.getBuildingFromCache(buildingId);
        return convertToBuildingBriefDTO(building);
    }

    @Override
    public UnitBriefDTO getUnitById(Long unitId) {
        if (unitId == null) {
            return null;
        }
        UnitDO unit = baseCacheService.getUnitFromCache(unitId);
        return convertToUnitBriefDTO(unit);
    }

    @Override
    public HouseBriefDTO getHouseById(Long houseId) {
        if (houseId == null) {
            return null;
        }
        HouseDO house = baseCacheService.getHouseFromCache(houseId);
        return convertToHouseBriefDTO(house);
    }

    @Override
    public List<Long> listDistrictIds() {
        return baseCacheService.getDistrictIdsFromCache();
    }

    @Override
    public List<CommunityBriefDTO> listCommunitiesByDistrictId(Long districtId) {
        if (districtId == null) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<CommunityDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CommunityDO::getDistrictId, districtId)
                .eq(CommunityDO::getStatus, BaseConstants.STATUS_ENABLED)
                .orderByAsc(CommunityDO::getCommunityCode);
        List<CommunityDO> communities = communityMapper.selectList(wrapper);
        return communities.stream()
                .map(this::convertToCommunityBriefDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<BuildingBriefDTO> listBuildingsByCommunityId(Long communityId) {
        if (communityId == null) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<BuildingDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BuildingDO::getCommunityId, communityId)
                .eq(BuildingDO::getStatus, BaseConstants.STATUS_ENABLED)
                .orderByAsc(BuildingDO::getBuildingCode);
        List<BuildingDO> buildings = buildingMapper.selectList(wrapper);
        return buildings.stream()
                .map(this::convertToBuildingBriefDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CascadeTreeDTO> getCascadeTree(Long communityId) {
        if (communityId == null) {
            return Collections.emptyList();
        }
        List<CascadeTreeVO> voList = cascadeService.getCascadeTree(communityId);
        return voList.stream()
                .map(this::convertToCascadeTreeDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void updateHouseStatus(Long houseId, String status) {
        if (houseId == null || status == null) {
            log.warn("updateHouseStatus 参数无效，houseId={}, status={}", houseId, status);
            return;
        }
        HouseStatusChangeDTO statusDTO = new HouseStatusChangeDTO();
        statusDTO.setHouseStatus(status);
        houseService.changeHouseStatus(houseId, statusDTO);
        log.info("更新房屋状态成功，houseId={}, status={}", houseId, status);
    }

    // ========== DO → DTO 转换方法 ==========

    private CommunityBriefDTO convertToCommunityBriefDTO(CommunityDO community) {
        if (community == null) {
            return null;
        }
        CommunityBriefDTO dto = new CommunityBriefDTO();
        dto.setId(community.getId());
        dto.setCommunityName(community.getCommunityName());
        dto.setCommunityCode(community.getCommunityCode());
        dto.setAddress(community.getAddress());
        dto.setDistrictId(community.getDistrictId());
        dto.setStatus(community.getStatus());
        return dto;
    }

    private BuildingBriefDTO convertToBuildingBriefDTO(BuildingDO building) {
        if (building == null) {
            return null;
        }
        BuildingBriefDTO dto = new BuildingBriefDTO();
        dto.setId(building.getId());
        dto.setBuildingName(building.getBuildingName());
        dto.setBuildingCode(building.getBuildingCode());
        dto.setCommunityId(building.getCommunityId());
        dto.setBuildingType(building.getBuildingType());
        dto.setStatus(building.getStatus());
        return dto;
    }

    private UnitBriefDTO convertToUnitBriefDTO(UnitDO unit) {
        if (unit == null) {
            return null;
        }
        UnitBriefDTO dto = new UnitBriefDTO();
        dto.setId(unit.getId());
        dto.setUnitName(unit.getUnitName());
        dto.setUnitCode(unit.getUnitCode());
        dto.setBuildingId(unit.getBuildingId());
        dto.setStatus(unit.getStatus());
        return dto;
    }

    private HouseBriefDTO convertToHouseBriefDTO(HouseDO house) {
        if (house == null) {
            return null;
        }
        HouseBriefDTO dto = new HouseBriefDTO();
        dto.setId(house.getId());
        dto.setHouseCode(house.getHouseCode());
        dto.setUnitId(house.getUnitId());
        dto.setFloor(house.getFloor());
        dto.setBuildingArea(house.getBuildingArea());
        dto.setHouseStatus(house.getHouseStatus());
        dto.setHouseType(house.getHouseType());
        return dto;
    }

    private CascadeTreeDTO convertToCascadeTreeDTO(CascadeTreeVO vo) {
        if (vo == null) {
            return null;
        }
        CascadeTreeDTO dto = new CascadeTreeDTO();
        dto.setId(vo.getId());
        dto.setName(vo.getName());
        dto.setCode(vo.getCode());
        dto.setType(vo.getType());
        if (vo.getChildren() != null) {
            dto.setChildren(vo.getChildren().stream()
                    .map(this::convertToCascadeTreeDTO)
                    .collect(Collectors.toList()));
        } else {
            dto.setChildren(Collections.emptyList());
        }
        return dto;
    }
}
