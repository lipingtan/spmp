package com.spmp.user.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.spmp.user.domain.entity.RoleDataPermissionDO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色数据权限关联 Mapper 接口。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Mapper
public interface RoleDataPermissionMapper extends BaseMapper<RoleDataPermissionDO> {

    /**
     * 查询角色的数据权限关联列表。
     */
    @Select("SELECT * FROM sys_role_data_permission WHERE role_id = #{roleId}")
    List<RoleDataPermissionDO> selectByRoleId(@Param("roleId") Long roleId);

    /**
     * 删除角色的所有数据权限关联。
     */
    @Delete("DELETE FROM sys_role_data_permission WHERE role_id = #{roleId}")
    int deleteByRoleId(@Param("roleId") Long roleId);
}
