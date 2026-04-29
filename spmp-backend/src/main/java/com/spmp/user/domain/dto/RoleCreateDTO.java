package com.spmp.user.domain.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

/**
 * 新增角色入参。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class RoleCreateDTO {

    @NotBlank(message = "角色名称不能为空")
    private String roleName;

    @NotBlank(message = "角色编码不能为空")
    private String roleCode;

    @NotBlank(message = "数据权限级别不能为空")
    private String dataPermissionLevel;

    private Integer sort = 0;
    private String remark;
}
