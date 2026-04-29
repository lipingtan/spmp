package com.spmp.user.service;

import com.spmp.common.result.PageResult;
import com.spmp.user.domain.dto.*;
import com.spmp.user.domain.vo.RoleSimpleVO;

import java.util.List;

/**
 * 角色管理服务接口。
 *
 * @author 技术团队
 * @since 1.0.0
 */
public interface RoleService {

    PageResult<RolePageDTO> listRoles(RoleQueryDTO queryDTO);

    List<RoleSimpleVO> listAllRoles();

    void createRole(RoleCreateDTO createDTO);

    void updateRole(Long id, RoleUpdateDTO updateDTO);

    void deleteRole(Long id);

    void batchDeleteRoles(List<Long> ids);

    void assignMenus(Long roleId, List<Long> menuIds);

    List<Long> getRoleMenuIds(Long roleId);

    void configDataPermission(Long roleId, DataPermissionConfigDTO configDTO);
}
