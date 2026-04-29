package com.spmp.common.domain;

import java.io.Serializable;

/**
 * 聚合根标记接口。
 * <p>
 * 所有 DDD 聚合根类型必须实现此接口。
 *
 * @param <ID> 聚合根标识类型
 * @author 技术团队
 * @since 1.0.0
 */
public interface AggregateRoot<ID> extends Serializable {

    /**
     * 获取聚合根标识。
     *
     * @return 聚合根 ID
     */
    ID getId();
}
