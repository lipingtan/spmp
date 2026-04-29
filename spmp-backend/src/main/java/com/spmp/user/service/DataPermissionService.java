package com.spmp.user.service;

import com.spmp.common.security.DataPermissionContext;
import com.spmp.user.domain.dto.DataPermissionConfigDTO;
import com.spmp.user.domain.vo.DataPermissionOptionVO;

/**
 * 数据权限服务接口。
 *
 * @author 技术团队
 * @since 1.0.0
 */
public interface DataPermissionService {

    DataPermissionOptionVO getOptions();

    DataPermissionContext loadUserDataPermission(Long userId);

    void configRoleDataPermission(Long roleId, DataPermissionConfigDTO configDTO);
}
