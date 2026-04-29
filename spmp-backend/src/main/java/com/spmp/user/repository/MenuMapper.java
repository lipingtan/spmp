package com.spmp.user.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.spmp.user.domain.entity.MenuDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 菜单 Mapper 接口。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Mapper
public interface MenuMapper extends BaseMapper<MenuDO> {

    /**
     * 查询用户拥有的权限标识列表。
     *
     * @param userId 用户 ID
     * @return 权限标识列表
     */
    @Select("SELECT DISTINCT m.permission FROM sys_menu m " +
            "INNER JOIN sys_role_menu rm ON m.id = rm.menu_id " +
            "INNER JOIN sys_user_role ur ON rm.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND m.permission IS NOT NULL " +
            "AND m.permission != '' AND m.status = 0 AND m.del_flag = 0")
    List<String> selectPermissionsByUserId(@Param("userId") Long userId);

    /**
     * 查询用户拥有的菜单列表（不含按钮）。
     *
     * @param userId 用户 ID
     * @return 菜单列表
     */
    @Select("SELECT DISTINCT m.* FROM sys_menu m " +
            "INNER JOIN sys_role_menu rm ON m.id = rm.menu_id " +
            "INNER JOIN sys_user_role ur ON rm.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND m.menu_type != 'B' " +
            "AND m.status = 0 AND m.del_flag = 0 ORDER BY m.sort")
    List<MenuDO> selectMenusByUserId(@Param("userId") Long userId);
}
