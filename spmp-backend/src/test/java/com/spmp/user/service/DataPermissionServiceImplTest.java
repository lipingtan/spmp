package com.spmp.user.service;

import com.spmp.base.api.BaseApi;
import com.spmp.base.api.dto.BuildingBriefDTO;
import com.spmp.base.api.dto.CommunityBriefDTO;
import com.spmp.base.domain.entity.DistrictDO;
import com.spmp.base.repository.DistrictMapper;
import com.spmp.common.security.DataPermissionContext;
import com.spmp.common.security.DataPermissionLevel;
import com.spmp.user.domain.entity.RoleDO;
import com.spmp.user.domain.vo.DataPermissionOptionVO;
import com.spmp.user.repository.RoleMapper;
import com.spmp.user.repository.UserRoleMapper;
import com.spmp.user.service.impl.DataPermissionServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("数据权限服务测试")
class DataPermissionServiceImplTest {

    @Mock
    private PermissionCacheService permissionCacheService;
    @Mock
    private RoleService roleService;
    @Mock
    private RoleMapper roleMapper;
    @Mock
    private UserRoleMapper userRoleMapper;
    @Mock
    private BaseApi baseApi;
    @Mock
    private DistrictMapper districtMapper;

    @InjectMocks
    private DataPermissionServiceImpl dataPermissionService;

    @Test
    @DisplayName("查询数据权限选项应返回片区/小区/楼栋")
    void shouldBuildPermissionOptions() {
        DistrictDO district = new DistrictDO();
        district.setId(1L);
        district.setDistrictName("东区");
        CommunityBriefDTO community = new CommunityBriefDTO();
        community.setId(10L);
        community.setCommunityName("阳光花园");
        BuildingBriefDTO building = new BuildingBriefDTO();
        building.setId(100L);
        building.setBuildingName("1栋");

        when(baseApi.listDistrictIds()).thenReturn(Collections.singletonList(1L));
        when(districtMapper.selectById(1L)).thenReturn(district);
        when(baseApi.listCommunitiesByDistrictId(1L)).thenReturn(Collections.singletonList(community));
        when(baseApi.listBuildingsByCommunityId(10L)).thenReturn(Collections.singletonList(building));

        DataPermissionOptionVO options = dataPermissionService.getOptions();

        assertEquals(1, options.getAreas().size());
        assertEquals("东区", options.getAreas().get(0).getName());
        assertEquals(1, options.getCommunities().size());
        assertEquals("阳光花园", options.getCommunities().get(0).getName());
        assertEquals(1, options.getBuildings().size());
        assertEquals("阳光花园1栋", options.getBuildings().get(0).getName());
    }

    @Test
    @DisplayName("权限校验与角色查询应返回缓存结果")
    void shouldCheckPermissionAndLoadRoles() {
        when(permissionCacheService.getUserPermissions(9L)).thenReturn(new HashSet<>(Arrays.asList("notice:list", "notice:approve")));
        when(userRoleMapper.selectRoleIdsByUserId(9L)).thenReturn(Arrays.asList(1L, 2L));

        RoleDO adminRole = new RoleDO();
        adminRole.setRoleCode("admin");
        RoleDO operatorRole = new RoleDO();
        operatorRole.setRoleCode("operator");
        when(roleMapper.selectById(1L)).thenReturn(adminRole);
        when(roleMapper.selectById(2L)).thenReturn(operatorRole);
        DataPermissionContext context = new DataPermissionContext();
        context.setUserId(9L);
        context.setLevel(DataPermissionLevel.SELF);
        when(permissionCacheService.getDataPermission(9L)).thenReturn(context);

        assertTrue(dataPermissionService.checkPermission(9L, "notice:list"));
        assertFalse(dataPermissionService.checkPermission(9L, "notice:repush"));
        assertEquals(2, dataPermissionService.getUserRoles(9L).size());
        assertEquals("SELF", dataPermissionService.getDataPermission(9L).getLevel());
    }
}
