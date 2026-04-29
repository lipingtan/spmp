package com.spmp.user.domain.dto;

import lombok.Data;

/**
 * Token 响应。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class TokenDTO {

    private String accessToken;
    private String refreshToken;
    private Long userId;
    private String username;
    private String realName;
    private String avatar;
}
