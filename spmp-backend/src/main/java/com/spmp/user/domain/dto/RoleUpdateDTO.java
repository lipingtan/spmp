package com.spmp.user.domain.dto;

import lombok.Data;

/**
 * 编辑角色入参。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class RoleUpdateDTO {

    private String roleName;
    private String dataPermissionLevel;
    private Integer status;
    private Integer sort;
    private String remark;
}
