package com.spmp.user.service;

import com.spmp.user.domain.dto.MenuCreateDTO;
import com.spmp.user.domain.dto.MenuTreeDTO;
import com.spmp.user.domain.dto.MenuUpdateDTO;

import java.util.List;

/**
 * 菜单管理服务接口。
 *
 * @author 技术团队
 * @since 1.0.0
 */
public interface MenuService {

    List<MenuTreeDTO> getMenuTree(String menuName, Integer status);

    List<MenuTreeDTO> getUserMenuTree(Long userId);

    void createMenu(MenuCreateDTO createDTO);

    void updateMenu(Long id, MenuUpdateDTO updateDTO);

    void deleteMenu(Long id);
}
