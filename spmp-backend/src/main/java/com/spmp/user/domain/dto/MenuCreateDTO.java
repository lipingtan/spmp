package com.spmp.user.domain.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 新增菜单入参。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class MenuCreateDTO {

    @NotBlank(message = "菜单名称不能为空")
    private String menuName;

    private Long parentId = 0L;

    @NotBlank(message = "菜单类型不能为空")
    private String menuType;

    private String path;
    private String component;
    private String permission;
    private String icon;

    @NotNull(message = "排序号不能为空")
    private Integer sort;
}
