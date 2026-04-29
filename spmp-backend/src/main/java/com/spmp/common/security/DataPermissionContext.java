package com.spmp.common.security;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 数据权限上下文。
 * <p>
 * 存储当前用户的数据权限级别和对应的数据范围。
 * 通用化设计：scopeMap 支持多字段多值的 IN 查询。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Data
public class DataPermissionContext {

    /** 权限级别 */
    private DataPermissionLevel level;

    /**
     * 数据范围映射。
     * <p>
     * key = 数据库字段名（如 area_id、community_id、building_id）
     * value = 该字段允许的 ID 集合
     */
    private Map<String, Set<Long>> scopeMap = new HashMap<>();

    /** 用户 ID（SELF 级别使用） */
    private Long userId;

    /** 用户名（SELF 级别 create_by 过滤使用） */
    private String username;

    /** ThreadLocal 存储当前线程的数据权限上下文 */
    private static final ThreadLocal<DataPermissionContext> CONTEXT_HOLDER = new ThreadLocal<>();

    /**
     * 设置当前线程的数据权限上下文。
     *
     * @param context 数据权限上下文
     */
    public static void setContext(DataPermissionContext context) {
        CONTEXT_HOLDER.set(context);
    }

    /**
     * 获取当前线程的数据权限上下文。
     *
     * @return 数据权限上下文，可能为 null
     */
    public static DataPermissionContext getContext() {
        return CONTEXT_HOLDER.get();
    }

    /**
     * 清除当前线程的数据权限上下文。
     */
    public static void clearContext() {
        CONTEXT_HOLDER.remove();
    }
}
