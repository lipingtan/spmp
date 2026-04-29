package com.spmp.user.domain.dto;

import lombok.Data;
import java.util.Date;

/**
 * 角色分页列表出参。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class RolePageDTO {

    private Long id;
    private String roleName;
    private String roleCode;
    private String dataPermissionLevel;
    private Integer status;
    private Integer sort;
    private String remark;
    private Date createTime;
}
