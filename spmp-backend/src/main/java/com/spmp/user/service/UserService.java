package com.spmp.user.service;

import com.spmp.common.result.PageResult;
import com.spmp.user.domain.dto.UserCreateDTO;
import com.spmp.user.domain.dto.UserPageDTO;
import com.spmp.user.domain.dto.UserQueryDTO;
import com.spmp.user.domain.dto.UserUpdateDTO;

import java.util.List;

/**
 * 用户管理服务接口。
 *
 * @author 技术团队
 * @since 1.0.0
 */
public interface UserService {

    PageResult<UserPageDTO> listUsers(UserQueryDTO queryDTO);

    void createUser(UserCreateDTO createDTO);

    void updateUser(Long id, UserUpdateDTO updateDTO);

    void deleteUser(Long id);

    void batchDeleteUsers(List<Long> ids);

    void updateStatus(Long id, Integer status);

    void batchUpdateStatus(List<Long> ids, Integer status);

    void resetPassword(Long id);
}
