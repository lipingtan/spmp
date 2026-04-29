package com.spmp.common.sequence.metrics;

/**
 * 序列相关 Micrometer 指标名常量。
 *
 * @author 技术团队
 * @since 1.0.0
 */
public final class SequenceMeterNames {

    private SequenceMeterNames() {
    }

    public static final String ALLOCATE_TOTAL = "sequence.allocate.total";

    public static final String ALLOCATE_DURATION = "sequence.allocate.duration";

    /** 当前 JVM 实例上各 dateKey 本地号段剩余可发数量之和（估算，用于容量观察）。 */
    public static final String RANGE_REMAINING = "sequence.range.remaining";
}
