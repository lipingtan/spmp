package com.spmp.user.domain.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * 用户查询条件。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class UserQueryDTO {

    private String username;
    private String realName;
    private String phone;
    private Integer status;
    private Long roleId;

    @Min(1)
    private Integer pageNum = 1;

    @Min(1)
    @Max(100)
    private Integer pageSize = 10;
}
