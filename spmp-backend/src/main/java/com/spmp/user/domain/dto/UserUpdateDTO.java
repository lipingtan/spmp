package com.spmp.user.domain.dto;

import lombok.Data;

import java.util.List;

/**
 * 编辑用户入参。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class UserUpdateDTO {

    private String realName;
    private String phone;
    private List<Long> roleIds;
    private Integer status;
}
