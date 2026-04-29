package com.spmp.user.api.dto;

import lombok.Data;

/**
 * 用户简要信息（跨模块 DTO）。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class UserBriefDTO {

    private Long id;
    private String username;
    private String realName;
    /** 手机号（脱敏） */
    private String phone;
}
