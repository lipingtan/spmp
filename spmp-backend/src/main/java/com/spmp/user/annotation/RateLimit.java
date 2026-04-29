package com.spmp.user.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 限流注解。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /** 限流 key 前缀 */
    String key();

    /** 时间窗口（秒） */
    int window() default 60;

    /** 最大请求次数 */
    int maxCount() default 10;

    /** 限流维度 */
    String dimension() default "IP";

    /** 提示消息 */
    String message() default "请求过于频繁，请稍后重试";
}
