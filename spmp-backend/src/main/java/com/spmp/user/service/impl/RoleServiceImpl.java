package com.spmp.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.spmp.common.exception.BusinessException;
import com.spmp.common.exception.ErrorCode;
import com.spmp.common.result.PageResult;
import com.spmp.user.constant.UserConstants;
import com.spmp.user.constant.UserErrorCode;
import com.spmp.user.domain.dto.*;
import com.spmp.user.domain.entity.RoleDataPermissionDO;
import com.spmp.user.domain.entity.RoleDO;
import com.spmp.user.domain.entity.RoleMenuDO;
import com.spmp.user.domain.vo.RoleSimpleVO;
import com.spmp.user.repository.*;
import com.spmp.user.service.PermissionCacheService;
import com.spmp.user.service.RoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色管理服务实现。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Slf4j
@Service
public class RoleServiceImpl implements RoleService {

    private final RoleMapper roleMapper;
    private final RoleMenuMapper roleMenuMapper;
    private final RoleDataPermissionMapper roleDataPermissionMapper;
    private final UserRoleMapper userRoleMapper;
    private final PermissionCacheService permissionCacheService;

    public RoleServiceImpl(RoleMapper roleMapper, RoleMenuMapper roleMenuMapper,
                           RoleDataPermissionMapper roleDataPermissionMapper,
                           UserRoleMapper userRoleMapper,
                           PermissionCacheService permissionCacheService) {
        this.roleMapper = roleMapper;
        this.roleMenuMapper = roleMenuMapper;
        this.roleDataPermissionMapper = roleDataPermissionMapper;
        this.userRoleMapper = userRoleMapper;
        this.permissionCacheService = permissionCacheService;
    }

    @Override
    public PageResult<RolePageDTO> listRoles(RoleQueryDTO queryDTO) {
        LambdaQueryWrapper<RoleDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(queryDTO.getRoleName()), RoleDO::getRoleName, queryDTO.getRoleName());
        wrapper.like(StringUtils.hasText(queryDTO.getRoleCode()), RoleDO::getRoleCode, queryDTO.getRoleCode());
        wrapper.eq(queryDTO.getStatus() != null, RoleDO::getStatus, queryDTO.getStatus());
        wrapper.orderByAsc(RoleDO::getSort);

        IPage<RoleDO> page = roleMapper.selectPage(
                new Page<>(queryDTO.getPageNum(), queryDTO.getPageSize()), wrapper);

        List<RolePageDTO> records = page.getRecords().stream().map(role -> {
            RolePageDTO dto = new RolePageDTO();
            dto.setId(role.getId());
            dto.setRoleName(role.getRoleName());
            dto.setRoleCode(role.getRoleCode());
            dto.setDataPermissionLevel(role.getDataPermissionLevel());
            dto.setStatus(role.getStatus());
            dto.setSort(role.getSort());
            dto.setRemark(role.getRemark());
            dto.setCreateTime(role.getCreateTime());
            return dto;
        }).collect(Collectors.toList());

        PageResult<RolePageDTO> result = new PageResult<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(records);
        result.setTotal(page.getTotal());
        result.setPageNum((int) page.getCurrent());
        result.setPageSize((int) page.getSize());
        return result;
    }

    @Override
    public List<RoleSimpleVO> listAllRoles() {
        LambdaQueryWrapper<RoleDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RoleDO::getStatus, UserConstants.STATUS_ENABLED);
        wrapper.orderByAsc(RoleDO::getSort);
        return roleMapper.selectList(wrapper).stream().map(role -> {
            RoleSimpleVO vo = new RoleSimpleVO();
            vo.setId(role.getId());
            vo.setRoleName(role.getRoleName());
            vo.setRoleCode(role.getRoleCode());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createRole(RoleCreateDTO createDTO) {
        // 校验角色编码唯一
        RoleDO existing = roleMapper.selectByRoleCode(createDTO.getRoleCode().toLowerCase());
        if (existing != null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, UserErrorCode.ROLE_CODE_EXISTS.getMessage());
        }
        // 校验角色名称唯一
        LambdaQueryWrapper<RoleDO> nameWrapper = new LambdaQueryWrapper<>();
        nameWrapper.eq(RoleDO::getRoleName, createDTO.getRoleName());
        if (roleMapper.selectCount(nameWrapper) > 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, UserErrorCode.ROLE_NAME_EXISTS.getMessage());
        }

        RoleDO role = new RoleDO();
        role.setRoleName(createDTO.getRoleName());
        role.setRoleCode(createDTO.getRoleCode());
        role.setDataPermissionLevel(createDTO.getDataPermissionLevel());
        role.setSort(createDTO.getSort());
        role.setRemark(createDTO.getRemark());
        role.setStatus(UserConstants.STATUS_ENABLED);
        roleMapper.insert(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRole(Long id, RoleUpdateDTO updateDTO) {
        RoleDO role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        if (UserConstants.SUPER_ADMIN_ROLE_CODE.equals(role.getRoleCode())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, UserErrorCode.CANNOT_MODIFY_ADMIN_ROLE.getMessage());
        }

        if (StringUtils.hasText(updateDTO.getRoleName())) {
            role.setRoleName(updateDTO.getRoleName());
        }
        if (updateDTO.getDataPermissionLevel() != null) {
            role.setDataPermissionLevel(updateDTO.getDataPermissionLevel());
        }
        if (updateDTO.getStatus() != null) {
            role.setStatus(updateDTO.getStatus());
        }
        if (updateDTO.getSort() != null) {
            role.setSort(updateDTO.getSort());
        }
        if (updateDTO.getRemark() != null) {
            role.setRemark(updateDTO.getRemark());
        }
        roleMapper.updateById(role);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long id) {
        RoleDO role = roleMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        if (UserConstants.SUPER_ADMIN_ROLE_CODE.equals(role.getRoleCode())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, UserErrorCode.CANNOT_DELETE_ADMIN_ROLE.getMessage());
        }
        // 检查关联用户
        int userCount = userRoleMapper.countByRoleId(id);
        if (userCount > 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, UserErrorCode.ROLE_HAS_USERS.getMessage());
        }

        roleMapper.deleteById(id);
        roleMenuMapper.deleteByRoleId(id);
        roleDataPermissionMapper.deleteByRoleId(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteRoles(List<Long> ids) {
        for (Long id : ids) {
            deleteRole(id);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignMenus(Long roleId, List<Long> menuIds) {
        roleMenuMapper.deleteByRoleId(roleId);
        if (menuIds != null) {
            for (Long menuId : menuIds) {
                RoleMenuDO rm = new RoleMenuDO();
                rm.setRoleId(roleId);
                rm.setMenuId(menuId);
                roleMenuMapper.insert(rm);
            }
        }
        permissionCacheService.clearRoleUsersCache(roleId);
    }

    @Override
    public List<Long> getRoleMenuIds(Long roleId) {
        return roleMenuMapper.selectMenuIdsByRoleId(roleId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void configDataPermission(Long roleId, DataPermissionConfigDTO configDTO) {
        // 更新角色的数据权限级别
        RoleDO role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        role.setDataPermissionLevel(configDTO.getDataPermissionLevel());
        roleMapper.updateById(role);

        // 先删后增
        roleDataPermissionMapper.deleteByRoleId(roleId);

        String level = configDTO.getDataPermissionLevel();
        List<Long> dataIds = configDTO.getDataIds();
        if (dataIds != null && !dataIds.isEmpty()
                && !"ALL".equals(level) && !"SELF".equals(level)) {
            String dataType = getDataTypeByLevel(level);
            for (Long dataId : dataIds) {
                RoleDataPermissionDO dp = new RoleDataPermissionDO();
                dp.setRoleId(roleId);
                dp.setDataType(dataType);
                dp.setDataId(dataId);
                roleDataPermissionMapper.insert(dp);
            }
        }

        permissionCacheService.clearRoleUsersCache(roleId);
    }

    private String getDataTypeByLevel(String level) {
        switch (level) {
            case "AREA":
                return "AREA";
            case "COMMUNITY":
                return "COMMUNITY";
            case "BUILDING":
                return "BUILDING";
            default:
                return level;
        }
    }
}
