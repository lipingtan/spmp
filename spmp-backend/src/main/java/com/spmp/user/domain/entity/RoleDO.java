package com.spmp.user.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.spmp.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色表实体。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
public class RoleDO extends BaseEntity {

    /** 角色名称 */
    private String roleName;

    /** 角色编码 */
    private String roleCode;

    /** 数据权限级别 */
    private String dataPermissionLevel;

    /** 状态（0-启用 1-禁用） */
    private Integer status;

    /** 排序 */
    private Integer sort;

    /** 备注 */
    private String remark;
}
