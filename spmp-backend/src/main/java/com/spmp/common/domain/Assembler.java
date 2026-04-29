package com.spmp.common.domain;

/**
 * 聚合根与持久化实体之间的双向转换接口。
 * <p>
 * 负责领域对象到表对象的拆分和组装。
 *
 * @param <A> 聚合根类型
 * @param <E> 持久化实体类型
 * @author 技术团队
 * @since 1.0.0
 */
public interface Assembler<A extends AggregateRoot<?>, E> {

    /**
     * 将聚合根转换为主持久化实体。
     *
     * @param aggregate 聚合根对象
     * @return 持久化实体
     */
    E toEntity(A aggregate);

    /**
     * 将持久化实体组装为完整的聚合根。
     *
     * @param entity 持久化实体
     * @return 聚合根对象
     */
    A toAggregate(E entity);
}
