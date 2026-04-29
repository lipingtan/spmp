package com.spmp.user.domain.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

/**
 * 修改密码入参。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class PasswordUpdateDTO {

    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    private String newPassword;
}
