package com.spmp.common.sequence.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 序列号段模型。
 *
 * @author 技术团队
 * @since 1.0.0
 */
@Getter
@AllArgsConstructor
public class SequenceRange {

    private final long start;
    private final long end;
}
