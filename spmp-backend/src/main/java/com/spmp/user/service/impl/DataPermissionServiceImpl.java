package com.spmp.user.service.impl;

import com.spmp.base.api.BaseApi;
import com.spmp.base.api.dto.BuildingBriefDTO;
import com.spmp.base.api.dto.CommunityBriefDTO;
import com.spmp.base.repository.DistrictMapper;
import com.spmp.base.domain.entity.DistrictDO;
import com.spmp.common.security.DataPermissionContext;
import com.spmp.user.api.PermissionApi;
import com.spmp.user.api.dto.DataPermissionDTO;
import com.spmp.user.domain.dto.DataPermissionConfigDTO;
import com.spmp.user.domain.entity.RoleDO;
import com.spmp.user.domain.vo.DataPermissionOptionVO;
import com.spmp.user.repository.RoleMapper;
import com.spmp.user.repository.UserRoleMapper;
import com.spmp.user.service.DataPermissionService;
import com.spmp.user.service.PermissionCacheService;
import com.spmp.user.service.RoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据权限服务实现（同时实现 PermissionApi）。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Slf4j
@Service
public class DataPermissionServiceImpl implements DataPermissionService, PermissionApi {

    private final PermissionCacheService permissionCacheService;
    private final RoleService roleService;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;
    private final BaseApi baseApi;
    private final DistrictMapper districtMapper;

    public DataPermissionServiceImpl(PermissionCacheService permissionCacheService,
                                     RoleService roleService, RoleMapper roleMapper,
                                     UserRoleMapper userRoleMapper,
                                     BaseApi baseApi,
                                     DistrictMapper districtMapper) {
        this.permissionCacheService = permissionCacheService;
        this.roleService = roleService;
        this.roleMapper = roleMapper;
        this.userRoleMapper = userRoleMapper;
        this.baseApi = baseApi;
        this.districtMapper = districtMapper;
    }

    @Override
    public DataPermissionOptionVO getOptions() {
        DataPermissionOptionVO vo = new DataPermissionOptionVO();

        // 从 BaseApi 获取所有启用片区ID，再查询片区名称
        List<Long> districtIds = baseApi.listDistrictIds();
        List<DataPermissionOptionVO.OptionItem> areas = new ArrayList<>();
        List<DataPermissionOptionVO.OptionItem> communities = new ArrayList<>();
        List<DataPermissionOptionVO.OptionItem> buildings = new ArrayList<>();

        for (Long districtId : districtIds) {
            // 查询片区名称
            DistrictDO district = districtMapper.selectById(districtId);
            if (district != null) {
                areas.add(new DataPermissionOptionVO.OptionItem(district.getId(), district.getDistrictName()));
            }

            // 查询片区下的小区列表
            List<CommunityBriefDTO> communityList = baseApi.listCommunitiesByDistrictId(districtId);
            for (CommunityBriefDTO community : communityList) {
                communities.add(new DataPermissionOptionVO.OptionItem(community.getId(), community.getCommunityName()));

                // 查询小区下的楼栋列表
                List<BuildingBriefDTO> buildingList = baseApi.listBuildingsByCommunityId(community.getId());
                for (BuildingBriefDTO building : buildingList) {
                    String buildingDisplayName = community.getCommunityName() + building.getBuildingName();
                    buildings.add(new DataPermissionOptionVO.OptionItem(building.getId(), buildingDisplayName));
                }
            }
        }

        vo.setAreas(areas);
        vo.setCommunities(communities);
        vo.setBuildings(buildings);
        return vo;
    }

    @Override
    public DataPermissionContext loadUserDataPermission(Long userId) {
        return permissionCacheService.getDataPermission(userId);
    }

    @Override
    public void configRoleDataPermission(Long roleId, DataPermissionConfigDTO configDTO) {
        roleService.configDataPermission(roleId, configDTO);
    }

    // ========== PermissionApi 实现 ==========

    @Override
    public DataPermissionDTO getDataPermission(Long userId) {
        DataPermissionContext context = permissionCacheService.getDataPermission(userId);
        DataPermissionDTO dto = new DataPermissionDTO();
        if (context != null) {
            dto.setLevel(context.getLevel() != null ? context.getLevel().name() : null);
            dto.setScopeMap(context.getScopeMap());
            dto.setUserId(context.getUserId());
        }
        return dto;
    }

    @Override
    public boolean checkPermission(Long userId, String permCode) {
        Set<String> permissions = permissionCacheService.getUserPermissions(userId);
        return permissions != null && permissions.contains(permCode);
    }

    @Override
    public List<String> getUserRoles(Long userId) {
        List<Long> roleIds = userRoleMapper.selectRoleIdsByUserId(userId);
        return roleIds.stream()
                .map(roleMapper::selectById)
                .filter(Objects::nonNull)
                .map(RoleDO::getRoleCode)
                .collect(Collectors.toList());
    }
}
