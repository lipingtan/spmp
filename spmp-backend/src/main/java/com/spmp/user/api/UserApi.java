package com.spmp.user.api;

import com.spmp.user.api.dto.UserBriefDTO;
import com.spmp.user.api.dto.UserCreateDTO;

import java.util.List;

/**
 * 用户查询 API 接口（供其他模块调用）。
 *
 * @author 技术团队
 * @since 1.0.0
 */
public interface UserApi {

    UserBriefDTO getUserById(Long userId);

    List<UserBriefDTO> getUsersByRoleCode(String roleCode);

    List<UserBriefDTO> getUsersByIds(List<Long> userIds);

    /**
     * 创建用户（供 owner 模块调用）。
     * <p>
     * 用于 H5 业主注册和添加家庭成员时创建 sys_user 记录。
     *
     * @param dto 用户创建参数（用户名、密码、角色编码）
     * @return 新创建的用户ID
     */
    Long createUser(UserCreateDTO dto);
}
