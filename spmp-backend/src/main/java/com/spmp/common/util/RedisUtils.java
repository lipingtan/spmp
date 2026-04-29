package com.spmp.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * Redis 缓存工具类。
 * <p>
 * 封装常用的 get/set/delete/expire 操作，所有方法内部 try-catch，
 * 操作失败时记录 WARN 日志并返回默认值，不抛出异常。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Slf4j
@Component
public class RedisUtils {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisUtils(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 存储缓存数据（无过期时间）。
     *
     * @param key   缓存键
     * @param value 缓存值
     */
    public void set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
        } catch (Exception e) {
            log.warn("Redis set 失败, key={}", key, e);
        }
    }

    /**
     * 存储带过期时间的缓存数据。
     *
     * @param key     缓存键
     * @param value   缓存值
     * @param timeout 过期时间
     * @param unit    时间单位
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
        } catch (Exception e) {
            log.warn("Redis set 失败, key={}, timeout={}", key, timeout, e);
        }
    }

    /**
     * 获取缓存数据并反序列化为指定类型。
     *
     * @param key   缓存键
     * @param clazz 目标类型
     * @param <T>   返回类型
     * @return 缓存值，不存在或失败时返回 null
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                return null;
            }
            return (T) value;
        } catch (Exception e) {
            log.warn("Redis get 失败, key={}", key, e);
            return null;
        }
    }

    /**
     * 删除单个缓存。
     *
     * @param key 缓存键
     * @return 删除成功返回 true，失败返回 false
     */
    public Boolean delete(String key) {
        try {
            return redisTemplate.delete(key);
        } catch (Exception e) {
            log.warn("Redis delete 失败, key={}", key, e);
            return false;
        }
    }

    /**
     * 批量删除缓存。
     *
     * @param keys 缓存键集合
     * @return 删除的数量，失败返回 0
     */
    public Long delete(Collection<String> keys) {
        try {
            return redisTemplate.delete(keys);
        } catch (Exception e) {
            log.warn("Redis batch delete 失败, keys={}", keys, e);
            return 0L;
        }
    }

    /**
     * 设置缓存过期时间。
     *
     * @param key     缓存键
     * @param timeout 过期时间
     * @param unit    时间单位
     * @return 设置成功返回 true，失败返回 false
     */
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        try {
            return redisTemplate.expire(key, timeout, unit);
        } catch (Exception e) {
            log.warn("Redis expire 失败, key={}", key, e);
            return false;
        }
    }

    /**
     * 判断缓存 key 是否存在。
     *
     * @param key 缓存键
     * @return 存在返回 true，不存在或失败返回 false
     */
    public Boolean hasKey(String key) {
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            log.warn("Redis hasKey 失败, key={}", key, e);
            return false;
        }
    }

    /**
     * 原子递增操作。
     *
     * @param key   缓存键
     * @param delta 递增值
     * @return 递增后的值，失败返回 0
     */
    public Long increment(String key, long delta) {
        try {
            return redisTemplate.opsForValue().increment(key, delta);
        } catch (Exception e) {
            log.warn("Redis increment 失败, key={}, delta={}", key, delta, e);
            return 0L;
        }
    }
}
