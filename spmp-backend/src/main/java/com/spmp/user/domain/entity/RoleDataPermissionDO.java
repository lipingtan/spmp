package com.spmp.user.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 角色数据权限关联表实体。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
@TableName("sys_role_data_permission")
public class RoleDataPermissionDO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 角色 ID */
    private Long roleId;

    /** 数据类型（AREA/COMMUNITY/BUILDING） */
    private String dataType;

    /** 关联数据 ID */
    private Long dataId;
}
