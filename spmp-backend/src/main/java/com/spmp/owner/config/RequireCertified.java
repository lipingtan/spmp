package com.spmp.owner.config;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 业主认证状态校验注解。
 * <p>
 * 标注在 H5 端需要已认证业主才能访问的接口方法上。
 * 由 {@link RequireCertifiedAspect} 切面拦截，校验当前业主是否已完成认证。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireCertified {

    /**
     * 未认证时的提示信息。
     */
    String message() default "该功能需要完成业主认证后才能使用";
}
