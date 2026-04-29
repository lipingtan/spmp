package com.spmp.user.domain.dto;

import lombok.Data;
import java.util.List;
import java.util.Set;

/**
 * 个人信息出参。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class ProfileDTO {

    private Long userId;
    private String username;
    private String realName;
    private String phone;
    private String avatar;
    private List<String> roles;
    private Set<String> permissions;
    private String dataPermissionLevel;
}
