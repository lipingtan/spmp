package com.spmp.user.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作日志注解。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OperationLog {

    /** 模块名称 */
    String module();

    /** 操作类型 */
    String type();

    /** 操作描述 */
    String description() default "";

    /**
     * 变更前数据查询 SpEL 表达式。
     * <p>
     * 在方法执行前通过 SpEL 表达式调用 Service 方法获取原始数据。
     * 示例: "@districtServiceImpl.getById(#id)"
     * 为空时不记录变更对比。
     */
    String beforeQuerySpEL() default "";
}
