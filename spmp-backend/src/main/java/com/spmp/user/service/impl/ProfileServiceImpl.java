package com.spmp.user.service.impl;

import cn.hutool.crypto.digest.DigestUtil;
import com.spmp.common.exception.BusinessException;
import com.spmp.common.exception.ErrorCode;
import com.spmp.common.security.DataPermissionContext;
import com.spmp.common.util.EncryptUtils;
import com.spmp.common.util.RedisUtils;
import com.spmp.user.constant.UserConstants;
import com.spmp.user.constant.UserErrorCode;
import com.spmp.user.domain.dto.PasswordUpdateDTO;
import com.spmp.user.domain.dto.ProfileDTO;
import com.spmp.user.domain.dto.ProfileUpdateDTO;
import com.spmp.user.domain.entity.RoleDO;
import com.spmp.user.domain.entity.UserDO;
import com.spmp.user.repository.RoleMapper;
import com.spmp.user.repository.UserMapper;
import com.spmp.user.repository.UserRoleMapper;
import com.spmp.user.service.PermissionCacheService;
import com.spmp.user.service.ProfileService;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 个人中心服务实现。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Slf4j
@Service
public class ProfileServiceImpl implements ProfileService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final EncryptUtils encryptUtils;
    private final RedisUtils redisUtils;
    private final PermissionCacheService permissionCacheService;

    public ProfileServiceImpl(UserMapper userMapper, UserRoleMapper userRoleMapper,
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
    public ProfileDTO getProfile() {
        Long userId = getCurrentUserId();
        UserDO user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }

        ProfileDTO dto = new ProfileDTO();
        dto.setUserId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRealName(user.getRealName());
        dto.setPhone(maskPhone(user.getPhone()));
        dto.setAvatar(user.getAvatar());

        // 角色列表
        List<Long> roleIds = userRoleMapper.selectRoleIdsByUserId(userId);
        List<String> roleNames = roleIds.stream()
                .map(roleMapper::selectById)
                .filter(r -> r != null)
                .map(RoleDO::getRoleName)
                .collect(Collectors.toList());
        dto.setRoles(roleNames);

        // 权限标识
        Set<String> permissions = permissionCacheService.getUserPermissions(userId);
        dto.setPermissions(permissions);

        // 数据权限级别
        DataPermissionContext context = permissionCacheService.getDataPermission(userId);
        if (context != null && context.getLevel() != null) {
            dto.setDataPermissionLevel(context.getLevel().name());
        }

        return dto;
    }

    @Override
    public void updateProfile(ProfileUpdateDTO updateDTO) {
        Long userId = getCurrentUserId();
        UserDO user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }

        if (StringUtils.hasText(updateDTO.getRealName())) {
            user.setRealName(updateDTO.getRealName());
        }
        if (StringUtils.hasText(updateDTO.getPhone())) {
            String phoneHash = DigestUtil.sha256Hex(updateDTO.getPhone());
            UserDO phoneUser = userMapper.selectByPhoneHash(phoneHash);
            if (phoneUser != null && !phoneUser.getId().equals(userId)) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, UserErrorCode.PHONE_EXISTS.getMessage());
            }
            user.setPhone(encryptUtils.encrypt(updateDTO.getPhone()));
            user.setPhoneHash(phoneHash);
        }
        userMapper.updateById(user);
    }

    @Override
    public void updatePassword(PasswordUpdateDTO passwordDTO) {
        Long userId = getCurrentUserId();
        UserDO user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }

        // 校验旧密码
        if (!passwordEncoder.matches(passwordDTO.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, UserErrorCode.OLD_PASSWORD_ERROR.getMessage());
        }

        // 校验新密码不能与旧密码相同
        if (passwordEncoder.matches(passwordDTO.getNewPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, UserErrorCode.NEW_PASSWORD_SAME.getMessage());
        }

        // 校验密码强度
        if (!passwordDTO.getNewPassword().matches(UserConstants.PASSWORD_PATTERN)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, UserErrorCode.PASSWORD_TOO_WEAK.getMessage());
        }

        user.setPassword(passwordEncoder.encode(passwordDTO.getNewPassword()));
        userMapper.updateById(user);

        // 清除缓存，强制重新登录
        permissionCacheService.clearUserPermissions(userId);
        permissionCacheService.clearUserDataPermission(userId);
        permissionCacheService.clearUserMenus(userId);
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getDetails() instanceof Claims) {
            Claims claims = (Claims) auth.getDetails();
            Object userId = claims.get("userId");
            if (userId instanceof Number) {
                return ((Number) userId).longValue();
            }
        }
        throw new BusinessException(ErrorCode.UNAUTHORIZED);
    }

    private String maskPhone(String encryptedPhone) {
        try {
            String phone = encryptUtils.decrypt(encryptedPhone);
            return EncryptUtils.mask(phone, 3, 4);
        } catch (Exception e) {
            return "***";
        }
    }
}
