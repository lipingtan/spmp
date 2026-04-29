package com.spmp.user.domain.dto;

import lombok.Data;

/**
 * 编辑菜单入参。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class MenuUpdateDTO {

    private String menuName;
    private String path;
    private String component;
    private String permission;
    private String icon;
    private Integer sort;
    private Integer status;
}
