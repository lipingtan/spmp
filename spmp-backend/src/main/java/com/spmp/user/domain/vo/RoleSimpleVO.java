package com.spmp.user.domain.vo;

import lombok.Data;

/**
 * 角色下拉选项值对象。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class RoleSimpleVO {

    private Long id;
    private String roleName;
    private String roleCode;
}
