package com.spmp.user.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 用户名密码登录入参。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class LoginDTO {

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "验证码不能为空")
    private String captchaCode;

    @NotBlank(message = "验证码key不能为空")
    private String captchaKey;

    @NotBlank(message = "客户端类型不能为空")
    private String clientType;
}
