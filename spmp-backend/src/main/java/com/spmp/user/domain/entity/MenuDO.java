package com.spmp.user.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.spmp.common.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 菜单表实体。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_menu")
public class MenuDO extends BaseEntity {

    /** 菜单名称 */
    private String menuName;

    /** 父级 ID（0 为顶级） */
    private Long parentId;

    /** 菜单类型（D-目录 M-菜单 B-按钮） */
    private String menuType;

    /** 路由路径 */
    private String path;

    /** 组件路径 */
    private String component;

    /** 权限标识 */
    private String permission;

    /** 图标 */
    private String icon;

    /** 排序 */
    private Integer sort;

    /** 状态（0-启用 1-禁用） */
    private Integer status;
}
