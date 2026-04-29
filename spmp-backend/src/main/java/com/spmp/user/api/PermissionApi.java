package com.spmp.user.api;

import com.spmp.user.api.dto.DataPermissionDTO;

import java.util.List;

/**
 * 权限查询 API 接口（供其他模块调用）。
 *
 * @author 技术团队
 * @since 1.0.0
 */
public interface PermissionApi {

    DataPermissionDTO getDataPermission(Long userId);

    boolean checkPermission(Long userId, String permCode);

    List<String> getUserRoles(Long userId);
}
