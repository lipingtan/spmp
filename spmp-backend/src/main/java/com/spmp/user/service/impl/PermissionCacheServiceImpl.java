package com.spmp.user.service.impl;

import com.spmp.common.security.DataPermissionContext;
import com.spmp.common.security.DataPermissionLevel;
import com.spmp.common.util.RedisUtils;
import com.spmp.user.constant.UserConstants;
import com.spmp.user.domain.dto.MenuTreeDTO;
import com.spmp.user.domain.entity.MenuDO;
import com.spmp.user.domain.entity.RoleDataPermissionDO;
import com.spmp.user.domain.entity.RoleDO;
import com.spmp.user.repository.*;
import com.spmp.user.service.PermissionCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 权限缓存服务实现。
 * <p>
 * Redis 不可用时降级查数据库。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Slf4j
@Service
public class PermissionCacheServiceImpl implements PermissionCacheService {

    private static final long CACHE_EXPIRE_HOURS = 8;

    private final RedisUtils redisUtils;
    private final MenuMapper menuMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;
    private final RoleDataPermissionMapper roleDataPermissionMapper;

    public PermissionCacheServiceImpl(RedisUtils redisUtils, MenuMapper menuMapper,
                                      UserRoleMapper userRoleMapper, RoleMapper roleMapper,
                                      RoleDataPermissionMapper roleDataPermissionMapper) {
        this.redisUtils = redisUtils;
        this.menuMapper = menuMapper;
        this.userRoleMapper = userRoleMapper;
        this.roleMapper = roleMapper;
        this.roleDataPermissionMapper = roleDataPermissionMapper;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<String> getUserPermissions(Long userId) {
        String key = UserConstants.USER_PERMISSIONS_KEY + userId;
        try {
            Object cached = redisUtils.get(key, Object.class);
            if (cached instanceof Collection) {
                return new HashSet<>((Collection<String>) cached);
            }
        } catch (Exception e) {
            log.warn("获取用户权限缓存失败，降级查数据库, userId={}", userId);
        }
        // 降级查数据库
        Set<String> permissions = loadPermissionsFromDb(userId);
        try {
            redisUtils.set(key, permissions, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
        } catch (Exception e) {
            log.warn("写入用户权限缓存失败, userId={}", userId);
        }
        return permissions;
    }

    @Override
    public DataPermissionContext getDataPermission(Long userId) {
        String key = UserConstants.USER_DATA_PERMISSION_KEY + userId;
        try {
            DataPermissionContext cached = redisUtils.get(key, DataPermissionContext.class);
            if (cached != null) {
                return cached;
            }
        } catch (Exception e) {
            log.warn("获取用户数据权限缓存失败，降级查数据库, userId={}", userId);
        }
        // 降级查数据库
        DataPermissionContext context = loadDataPermissionFromDb(userId);
        try {
            redisUtils.set(key, context, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
        } catch (Exception e) {
            log.warn("写入用户数据权限缓存失败, userId={}", userId);
        }
        return context;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<MenuTreeDTO> getUserMenuTree(Long userId) {
        String key = UserConstants.USER_MENUS_KEY + userId;
        try {
            Object cached = redisUtils.get(key, Object.class);
            if (cached instanceof List) {
                return (List<MenuTreeDTO>) cached;
            }
        } catch (Exception e) {
            log.warn("获取用户菜单缓存失败，降级查数据库, userId={}", userId);
        }
        List<MenuTreeDTO> tree = loadMenuTreeFromDb(userId);
        try {
            redisUtils.set(key, tree, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
        } catch (Exception e) {
            log.warn("写入用户菜单缓存失败, userId={}", userId);
        }
        return tree;
    }

    @Override
    public void clearUserPermissions(Long userId) {
        redisUtils.delete(UserConstants.USER_PERMISSIONS_KEY + userId);
    }

    @Override
    public void clearUserDataPermission(Long userId) {
        redisUtils.delete(UserConstants.USER_DATA_PERMISSION_KEY + userId);
    }

    @Override
    public void clearUserMenus(Long userId) {
        redisUtils.delete(UserConstants.USER_MENUS_KEY + userId);
    }

    @Override
    public void clearRoleUsersCache(Long roleId) {
        List<Long> userIds = userRoleMapper.selectUserIdsByRoleId(roleId);
        for (Long userId : userIds) {
            clearUserPermissions(userId);
            clearUserDataPermission(userId);
            clearUserMenus(userId);
        }
    }

    /**
     * 从数据库加载用户权限标识。
     */
    private Set<String> loadPermissionsFromDb(Long userId) {
        List<String> permissions = menuMapper.selectPermissionsByUserId(userId);
        return new HashSet<>(permissions);
    }

    /**
     * 从数据库加载用户数据权限上下文（多角色并集合并）。
     */
    private DataPermissionContext loadDataPermissionFromDb(Long userId) {
        DataPermissionContext context = new DataPermissionContext();
        context.setUserId(userId);

        List<Long> roleIds = userRoleMapper.selectRoleIdsByUserId(userId);
        if (roleIds.isEmpty()) {
            context.setLevel(DataPermissionLevel.SELF);
            return context;
        }

        // 查询所有角色
        List<RoleDO> roles = roleMapper.selectBatchIds(roleIds);
        // 如果任一角色为 ALL，直接返回
        for (RoleDO role : roles) {
            if (DataPermissionLevel.ALL.name().equals(role.getDataPermissionLevel())) {
                context.setLevel(DataPermissionLevel.ALL);
                return context;
            }
        }

        // 合并 scopeMap
        Map<String, Set<Long>> scopeMap = new HashMap<>();
        boolean hasSelf = false;

        for (RoleDO role : roles) {
            String level = role.getDataPermissionLevel();
            if (DataPermissionLevel.SELF.name().equals(level)) {
                hasSelf = true;
                continue;
            }
            // 查询角色的数据权限关联
            List<RoleDataPermissionDO> dataPerms = roleDataPermissionMapper.selectByRoleId(role.getId());
            for (RoleDataPermissionDO dp : dataPerms) {
                String fieldName = getFieldNameByDataType(dp.getDataType());
                scopeMap.computeIfAbsent(fieldName, k -> new HashSet<>()).add(dp.getDataId());
            }
        }

        if (!scopeMap.isEmpty()) {
            // 有具体数据范围，取最高级别
            context.setScopeMap(scopeMap);
            if (scopeMap.containsKey("area_id")) {
                context.setLevel(DataPermissionLevel.AREA);
            } else if (scopeMap.containsKey("community_id")) {
                context.setLevel(DataPermissionLevel.COMMUNITY);
            } else {
                context.setLevel(DataPermissionLevel.BUILDING);
            }
        } else if (hasSelf) {
            context.setLevel(DataPermissionLevel.SELF);
        } else {
            context.setLevel(DataPermissionLevel.SELF);
        }

        return context;
    }

    /**
     * 根据数据类型获取数据库字段名。
     */
    private String getFieldNameByDataType(String dataType) {
        switch (dataType) {
            case "AREA":
                return "area_id";
            case "COMMUNITY":
                return "community_id";
            case "BUILDING":
                return "building_id";
            default:
                return dataType.toLowerCase() + "_id";
        }
    }

    /**
     * 从数据库加载用户菜单树。
     */
    private List<MenuTreeDTO> loadMenuTreeFromDb(Long userId) {
        List<MenuDO> menus = menuMapper.selectMenusByUserId(userId);
        return buildTree(menus);
    }

    /**
     * 构建菜单树。
     */
    private List<MenuTreeDTO> buildTree(List<MenuDO> menus) {
        Map<Long, MenuTreeDTO> map = new LinkedHashMap<>();
        for (MenuDO menu : menus) {
            MenuTreeDTO dto = new MenuTreeDTO();
            dto.setId(menu.getId());
            dto.setMenuName(menu.getMenuName());
            dto.setParentId(menu.getParentId());
            dto.setMenuType(menu.getMenuType());
            dto.setPath(menu.getPath());
            dto.setComponent(menu.getComponent());
            dto.setPermission(menu.getPermission());
            dto.setIcon(menu.getIcon());
            dto.setSort(menu.getSort());
            dto.setStatus(menu.getStatus());
            dto.setChildren(new ArrayList<>());
            map.put(menu.getId(), dto);
        }

        List<MenuTreeDTO> roots = new ArrayList<>();
        for (MenuTreeDTO dto : map.values()) {
            if (dto.getParentId() == null || dto.getParentId() == 0L) {
                roots.add(dto);
            } else {
                MenuTreeDTO parent = map.get(dto.getParentId());
                if (parent != null) {
                    parent.getChildren().add(dto);
                } else {
                    roots.add(dto);
                }
            }
        }
        return roots;
    }
}
