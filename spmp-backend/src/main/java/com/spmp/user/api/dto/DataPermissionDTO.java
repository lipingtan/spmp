package com.spmp.user.api.dto;

import lombok.Data;

import java.util.Map;
import java.util.Set;

/**
 * 数据权限信息（跨模块 DTO）。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class DataPermissionDTO {

    /** 权限级别 */
    private String level;

    /** 数据范围映射 */
    private Map<String, Set<Long>> scopeMap;

    /** 用户 ID */
    private Long userId;
}
