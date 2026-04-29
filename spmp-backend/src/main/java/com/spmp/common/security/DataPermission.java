package com.spmp.common.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 数据权限注解。
 * <p>
 * 标记需要数据权限控制的 Mapper 方法，通过参数指定表中对应的字段名。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataPermission {

    /**
     * 片区字段名。
     */
    String areaField() default "area_id";

    /**
     * 小区字段名。
     */
    String communityField() default "community_id";

    /**
     * 楼栋字段名。
     */
    String buildingField() default "building_id";

    /**
     * 仅本人级别过滤字段名。
     */
    String selfField() default "create_by";
}
