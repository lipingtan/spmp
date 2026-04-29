package com.spmp.common.domain;

import java.util.Optional;

/**
 * 面向聚合根的持久化仓储接口。
 * <p>
 * 提供 save/findById/delete/existsById 等方法，屏蔽底层表结构细节。
 *
 * @param <A>  聚合根类型
 * @param <ID> 聚合根标识类型
 * @author 技术团队
 * @since 1.0.0
 */
public interface Repository<A extends AggregateRoot<ID>, ID> {

    /**
     * 保存聚合根（新增或更新）。
     *
     * @param aggregate 聚合根对象
     * @return 保存后的聚合根
     */
    A save(A aggregate);

    /**
     * 根据 ID 查找聚合根。
     *
     * @param id 聚合根标识
     * @return 聚合根的 Optional 包装
     */
    Optional<A> findById(ID id);

    /**
     * 删除聚合根。
     *
     * @param id 聚合根标识
     */
    void delete(ID id);

    /**
     * 判断聚合根是否存在。
     *
     * @param id 聚合根标识
     * @return 存在返回 true
     */
    boolean existsById(ID id);
}
