package com.spmp.common.domain;

import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * 仓储抽象基类。
 * <p>
 * 封装 save/find/delete 的通用流程骨架，子类实现具体的拆分/组装逻辑。
 *
 * @param <A>  聚合根类型
 * @param <ID> 聚合根标识类型
 * @author 技术团队
 * @since 1.0.0
 */
public abstract class AbstractRepository<A extends AggregateRoot<ID>, ID>
        implements Repository<A, ID> {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public A save(A aggregate) {
        if (aggregate.getId() == null) {
            return doInsert(aggregate);
        } else {
            return doUpdate(aggregate);
        }
    }

    @Override
    public Optional<A> findById(ID id) {
        return doFind(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(ID id) {
        doDelete(id);
    }

    /**
     * 子类实现：插入聚合根（拆分到多张表）。
     *
     * @param aggregate 聚合根对象
     * @return 插入后的聚合根
     */
    protected abstract A doInsert(A aggregate);

    /**
     * 子类实现：更新聚合根（协调多张表更新）。
     *
     * @param aggregate 聚合根对象
     * @return 更新后的聚合根
     */
    protected abstract A doUpdate(A aggregate);

    /**
     * 子类实现：加载并组装聚合根。
     *
     * @param id 聚合根标识
     * @return 聚合根的 Optional 包装
     */
    protected abstract Optional<A> doFind(ID id);

    /**
     * 子类实现：删除聚合根关联数据。
     *
     * @param id 聚合根标识
     */
    protected abstract void doDelete(ID id);
}
