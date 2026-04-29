package com.spmp.user.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.spmp.user.domain.entity.RoleDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 角色 Mapper 接口。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Mapper
public interface RoleMapper extends BaseMapper<RoleDO> {

    /**
     * 根据角色编码查询角色。
     *
     * @param roleCode 角色编码
     * @return 角色实体
     */
    @Select("SELECT * FROM sys_role WHERE role_code = #{roleCode} AND del_flag = 0")
    RoleDO selectByRoleCode(@Param("roleCode") String roleCode);
}
