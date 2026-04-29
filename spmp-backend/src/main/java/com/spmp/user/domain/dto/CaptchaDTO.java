package com.spmp.user.domain.dto;

import lombok.Data;

/**
 * 验证码响应。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class CaptchaDTO {

    /** 验证码 key（UUID） */
    private String captchaKey;

    /** Base64 编码的验证码图片 */
    private String captchaImage;
}
