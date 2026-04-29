package com.spmp.user.service.impl;

import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spmp.common.exception.BusinessException;
import com.spmp.common.exception.ErrorCode;
import com.spmp.common.result.PageResult;
import com.spmp.common.util.EncryptUtils;
import com.spmp.common.util.RedisUtils;
import com.spmp.user.api.UserApi;
import com.spmp.user.api.dto.UserBriefDTO;
import com.spmp.user.constant.UserConstants;
import com.spmp.user.constant.UserErrorCode;
import com.spmp.user.domain.dto.*;
import com.spmp.user.domain.entity.RoleDO;
import com.spmp.user.domain.entity.UserDO;
import com.spmp.user.domain.entity.UserRoleDO;
import com.spmp.user.repository.RoleMapper;
import com.spmp.user.repository.UserMapper;
import com.spmp.user.repository.UserRoleMapper;
import com.spmp.user.service.PermissionCacheService;
import com.spmp.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户管理服务实现（同时实现 UserApi）。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService, UserApi {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final EncryptUtils encryptUtils;
    private final RedisUtils redisUtils;
    private final PermissionCacheService permissionCacheService;

    public UserServiceImpl(UserMapper userMapper, UserRoleMapper userRoleMapper,
                           RoleMapper roleMapper, BCryptPasswordEncoder passwordEncoder,
                           EncryptUtils encryptUtils, RedisUtils redisUtils,
                           PermissionCacheService permissionCacheService) {
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
        this.roleMapper = roleMapper;
        this.passwordEncoder = passwordEncoder;
        this.encryptUtils = encryptUtils;
        this.redisUtils = redisUtils;
        this.permissionCacheService = permissionCacheService;
    }

    @Override
    public PageResult<UserPageDTO> listUsers(UserQueryDTO queryDTO) {
        LambdaQueryWrapper<UserDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(queryDTO.getUsername()), UserDO::getUsername, queryDTO.getUsername());
        wrapper.like(StringUtils.hasText(queryDTO.getRealName()), UserDO::getRealName, queryDTO.getRealName());
        wrapper.eq(queryDTO.getStatus() != null, UserDO::getStatus, queryDTO.getStatus());
        wrapper.orderByDesc(UserDO::getCreateTime);

        IPage<UserDO> page = userMapper.selectPage(
                new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize()), wrapper);

        // 转换为 DTO
        List<UserPageDTO> records = page.getRecords().stream().map(user -> {
            UserPageDTO dto = new UserPageDTO();
            dto.setId(user.getId());
            dto.setUsername(user.getUsername());
            dto.setRealName(user.getRealName());
            dto.setPhone(maskPhone(user.getPhone()));
            dto.setStatus(user.getStatus());
            dto.setCreateTime(user.getCreateTime());
            // 查询角色
            List<Long> roleIds = userRoleMapper.selectRoleIdsByUserId(user.getId());
            List<String> roleNames = roleIds.stream()
                    .map(roleId -> {
                        RoleDO role = roleMapper.selectById(roleId);
                        return role != null ? role.getRoleName() : null;
                    })
                    .filter(name -> name != null)
                    .collect(Collectors.toList());
            dto.setRoles(roleNames);
            return dto;
        }).collect(Collectors.toList());

        PageResult<UserPageDTO> result = new PageResult<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(records);
        result.setTotal(page.getTotal());
        result.setPageNum((int) page.getCurrent());
        result.setPageSize((int) page.getSize());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createUser(UserCreateDTO createDTO) {
        // 校验用户名唯一
        UserDO existing = userMapper.selectByUsername(createDTO.getUsername().toLowerCase());
        if (existing != null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, UserErrorCode.USERNAME_EXISTS.getMessage());
        }

        // 校验手机号唯一
        String phoneHash = DigestUtil.sha256Hex(createDTO.getPhone());
        UserDO phoneUser = userMapper.selectByPhoneHash(phoneHash);
        if (phoneUser != null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, UserErrorCode.PHONE_EXISTS.getMessage());
        }

        // 创建用户
        UserDO user = new UserDO();
        user.setUsername(createDTO.getUsername());
        user.setPassword(passwordEncoder.encode(UserConstants.DEFAULT_PASSWORD));
        user.setRealName(createDTO.getRealName());
        user.setPhone(encryptUtils.encrypt(createDTO.getPhone()));
        user.setPhoneHash(phoneHash);
        user.setStatus(UserConstants.STATUS_ENABLED);
        userMapper.insert(user);

        // 建立角色关联
        saveUserRoles(user.getId(), createDTO.getRoleIds());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUser(Long id, UserUpdateDTO updateDTO) {
        UserDO user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }

        // 手机号唯一性校验
        if (StringUtils.hasText(updateDTO.getPhone())) {
            String phoneHash = DigestUtil.sha256Hex(updateDTO.getPhone());
            UserDO phoneUser = userMapper.selectByPhoneHash(phoneHash);
            if (phoneUser != null && !phoneUser.getId().equals(id)) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, UserErrorCode.PHONE_EXISTS.getMessage());
            }
            user.setPhone(encryptUtils.encrypt(updateDTO.getPhone()));
            user.setPhoneHash(phoneHash);
        }

        if (StringUtils.hasText(updateDTO.getRealName())) {
            user.setRealName(updateDTO.getRealName());
        }
        if (updateDTO.getStatus() != null) {
            user.setStatus(updateDTO.getStatus());
        }
        userMapper.updateById(user);

        // 更新角色关联
        if (updateDTO.getRoleIds() != null) {
            userRoleMapper.deleteByUserId(id);
            saveUserRoles(id, updateDTO.getRoleIds());
            permissionCacheService.clearUserPermissions(id);
            permissionCacheService.clearUserDataPermission(id);
            permissionCacheService.clearUserMenus(id);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id) {
        checkNotAdminOrSelf(id);
        userMapper.deleteById(id);
        userRoleMapper.deleteByUserId(id);
        permissionCacheService.clearUserPermissions(id);
        permissionCacheService.clearUserDataPermission(id);
        permissionCacheService.clearUserMenus(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteUsers(List<Long> ids) {
        for (Long id : ids) {
            deleteUser(id);
        }
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        UserDO user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        // 禁止禁用超级管理员
        if (UserConstants.STATUS_DISABLED == status && UserConstants.SUPER_ADMIN_USER_ID.equals(id)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, UserErrorCode.CANNOT_DISABLE_ADMIN.getMessage());
        }
        // 禁止禁用自己
        Long currentUserId = getCurrentUserId();
        if (UserConstants.STATUS_DISABLED == status && id.equals(currentUserId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, UserErrorCode.CANNOT_DISABLE_SELF.getMessage());
        }

        user.setStatus(status);
        userMapper.updateById(user);

        // 禁用时将 Token 加入黑名单（简化处理：清除缓存）
        if (UserConstants.STATUS_DISABLED == status) {
            permissionCacheService.clearUserPermissions(id);
            permissionCacheService.clearUserDataPermission(id);
            permissionCacheService.clearUserMenus(id);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateStatus(List<Long> ids, Integer status) {
        for (Long id : ids) {
            updateStatus(id, status);
        }
    }

    @Override
    public void resetPassword(Long id) {
        UserDO user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        user.setPassword(passwordEncoder.encode(UserConstants.DEFAULT_PASSWORD));
        userMapper.updateById(user);

        // 清除错误计数和锁定
        redisUtils.delete(UserConstants.LOGIN_FAIL_KEY + user.getUsername());
        redisUtils.delete(UserConstants.LOGIN_LOCK_KEY + user.getUsername());
    }

    // ========== UserApi 实现 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createUser(com.spmp.user.api.dto.UserCreateDTO dto) {
        // 校验用户名唯一（用户名=手机号）
        UserDO existing = userMapper.selectByUsername(dto.getUsername().toLowerCase());
        if (existing != null) {
            // 用户名已存在，直接返回已有用户ID
            return existing.getId();
        }

        // 校验手机号唯一（phone_hash）
        String phoneHash = cn.hutool.crypto.digest.DigestUtil.sha256Hex(dto.getUsername());
        UserDO phoneUser = userMapper.selectByPhoneHash(phoneHash);
        if (phoneUser != null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "该手机号已注册");
        }

        // 创建用户
        UserDO user = new UserDO();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRealName(dto.getUsername());
        user.setPhone(encryptUtils.encrypt(dto.getUsername()));
        user.setPhoneHash(phoneHash);
        user.setStatus(UserConstants.STATUS_ENABLED);
        userMapper.insert(user);

        // 通过 roleCode 查找角色并关联
        if (StringUtils.hasText(dto.getRoleCode())) {
            RoleDO role = roleMapper.selectByRoleCode(dto.getRoleCode());
            if (role != null) {
                UserRoleDO ur = new UserRoleDO();
                ur.setUserId(user.getId());
                ur.setRoleId(role.getId());
                userRoleMapper.insert(ur);
            }
        }

        log.info("通过 UserApi 创建用户成功，userId={}, username={}", user.getId(), dto.getUsername());
        return user.getId();
    }

    @Override
    public UserBriefDTO getUserById(Long userId) {
        UserDO user = userMapper.selectById(userId);
        if (user == null) {
            return null;
        }
        return toUserBriefDTO(user);
    }

    @Override
    public List<UserBriefDTO> getUsersByRoleCode(String roleCode) {
        RoleDO role = roleMapper.selectByRoleCode(roleCode);
        if (role == null) {
            return new ArrayList<>();
        }
        List<Long> userIds = userRoleMapper.selectUserIdsByRoleId(role.getId());
        return userIds.stream()
                .map(userMapper::selectById)
                .filter(u -> u != null && u.getStatus() == UserConstants.STATUS_ENABLED)
                .map(this::toUserBriefDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserBriefDTO> getUsersByIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return new ArrayList<>();
        }
        return userIds.stream()
                .map(userMapper::selectById)
                .filter(u -> u != null)
                .map(this::toUserBriefDTO)
                .collect(Collectors.toList());
    }

    // ========== 私有方法 ==========

    private void saveUserRoles(Long userId, List<Long> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return;
        }
        for (Long roleId : roleIds) {
            UserRoleDO ur = new UserRoleDO();
            ur.setUserId(userId);
            ur.setRoleId(roleId);
            userRoleMapper.insert(ur);
        }
    }

    private void checkNotAdminOrSelf(Long id) {
        if (UserConstants.SUPER_ADMIN_USER_ID.equals(id)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, UserErrorCode.CANNOT_DELETE_ADMIN.getMessage());
        }
        Long currentUserId = getCurrentUserId();
        if (id.equals(currentUserId)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, UserErrorCode.CANNOT_DELETE_SELF.getMessage());
        }
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getDetails() instanceof io.jsonwebtoken.Claims) {
            io.jsonwebtoken.Claims claims = (io.jsonwebtoken.Claims) auth.getDetails();
            Object userId = claims.get("userId");
            if (userId instanceof Number) {
                return ((Number) userId).longValue();
            }
        }
        return null;
    }

    private String maskPhone(String encryptedPhone) {
        try {
            String phone = encryptUtils.decrypt(encryptedPhone);
            return EncryptUtils.mask(phone, 3, 4);
        } catch (Exception e) {
            return "***";
        }
    }

    private UserBriefDTO toUserBriefDTO(UserDO user) {
        UserBriefDTO dto = new UserBriefDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRealName(user.getRealName());
        dto.setPhone(maskPhone(user.getPhone()));
        return dto;
    }
}
