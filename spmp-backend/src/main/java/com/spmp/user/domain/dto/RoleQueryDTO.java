package com.spmp.user.domain.dto;

import lombok.Data;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * 角色查询条件。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class RoleQueryDTO {

    private String roleName;
    private String roleCode;
    private Integer status;

    @Min(1)
    private Integer pageNum = 1;

    @Min(1)
    @Max(100)
    private Integer pageSize = 10;
}
