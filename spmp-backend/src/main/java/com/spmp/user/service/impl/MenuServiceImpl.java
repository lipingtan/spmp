package com.spmp.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.spmp.common.exception.BusinessException;
import com.spmp.common.exception.ErrorCode;
import com.spmp.user.constant.UserErrorCode;
import com.spmp.user.domain.dto.MenuCreateDTO;
import com.spmp.user.domain.dto.MenuTreeDTO;
import com.spmp.user.domain.dto.MenuUpdateDTO;
import com.spmp.user.domain.entity.MenuDO;
import com.spmp.user.repository.MenuMapper;
import com.spmp.user.repository.RoleMenuMapper;
import com.spmp.user.service.MenuService;
import com.spmp.user.service.PermissionCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 菜单管理服务实现。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Slf4j
@Service
public class MenuServiceImpl implements MenuService {

    private final MenuMapper menuMapper;
    private final RoleMenuMapper roleMenuMapper;
    private final PermissionCacheService permissionCacheService;

    public MenuServiceImpl(MenuMapper menuMapper, RoleMenuMapper roleMenuMapper,
                           PermissionCacheService permissionCacheService) {
        this.menuMapper = menuMapper;
        this.roleMenuMapper = roleMenuMapper;
        this.permissionCacheService = permissionCacheService;
    }

    @Override
    public List<MenuTreeDTO> getMenuTree(String menuName, Integer status) {
        LambdaQueryWrapper<MenuDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(menuName), MenuDO::getMenuName, menuName);
        wrapper.eq(status != null, MenuDO::getStatus, status);
        wrapper.orderByAsc(MenuDO::getSort);
        List<MenuDO> menus = menuMapper.selectList(wrapper);
        return buildTree(menus);
    }

    @Override
    public List<MenuTreeDTO> getUserMenuTree(Long userId) {
        return permissionCacheService.getUserMenuTree(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createMenu(MenuCreateDTO createDTO) {
        // 校验同一父级下名称不重复
        LambdaQueryWrapper<MenuDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MenuDO::getParentId, createDTO.getParentId());
        wrapper.eq(MenuDO::getMenuName, createDTO.getMenuName());
        if (menuMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, UserErrorCode.MENU_NAME_DUPLICATE.getMessage());
        }

        MenuDO menu = new MenuDO();
        menu.setMenuName(createDTO.getMenuName());
        menu.setParentId(createDTO.getParentId());
        menu.setMenuType(createDTO.getMenuType());
        menu.setPath(createDTO.getPath());
        menu.setComponent(createDTO.getComponent());
        menu.setPermission(createDTO.getPermission());
        menu.setIcon(createDTO.getIcon());
        menu.setSort(createDTO.getSort());
        menu.setStatus(0);
        menuMapper.insert(menu);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMenu(Long id, MenuUpdateDTO updateDTO) {
        MenuDO menu = menuMapper.selectById(id);
        if (menu == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }

        if (StringUtils.hasText(updateDTO.getMenuName())) {
            menu.setMenuName(updateDTO.getMenuName());
        }
        if (updateDTO.getPath() != null) {
            menu.setPath(updateDTO.getPath());
        }
        if (updateDTO.getComponent() != null) {
            menu.setComponent(updateDTO.getComponent());
        }
        if (updateDTO.getPermission() != null) {
            menu.setPermission(updateDTO.getPermission());
        }
        if (updateDTO.getIcon() != null) {
            menu.setIcon(updateDTO.getIcon());
        }
        if (updateDTO.getSort() != null) {
            menu.setSort(updateDTO.getSort());
        }
        if (updateDTO.getStatus() != null) {
            menu.setStatus(updateDTO.getStatus());
        }
        menuMapper.updateById(menu);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMenu(Long id) {
        // 检查子菜单
        LambdaQueryWrapper<MenuDO> childWrapper = new LambdaQueryWrapper<>();
        childWrapper.eq(MenuDO::getParentId, id);
        if (menuMapper.selectCount(childWrapper) > 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, UserErrorCode.MENU_HAS_CHILDREN.getMessage());
        }
        // 检查角色关联
        if (roleMenuMapper.countByMenuId(id) > 0) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, UserErrorCode.MENU_USED_BY_ROLE.getMessage());
        }
        menuMapper.deleteById(id);
    }

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
