package com.spmp.user.domain.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 菜单树节点。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class MenuTreeDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String menuName;
    private Long parentId;
    private String menuType;
    private String path;
    private String component;
    private String permission;
    private String icon;
    private Integer sort;
    private Integer status;
    private List<MenuTreeDTO> children;
}
