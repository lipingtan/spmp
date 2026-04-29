package com.spmp.user.domain.dto;

import lombok.Data;

/**
 * 修改个人信息入参。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class ProfileUpdateDTO {

    private String realName;
    private String phone;
}
