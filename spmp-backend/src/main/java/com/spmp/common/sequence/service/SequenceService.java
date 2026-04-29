package com.spmp.common.sequence.service;

/**
 * 序列服务。
 *
 * @author 技术团队
 * @since 1.0.0
 */
public interface SequenceService {

    /**
     * 获取下一个序列值。
     *
     * @param bizCode 业务编码
     * @param dateKey 日期分区（yyyyMMdd）
     * @return 序列值
     */
    long nextSequence(String bizCode, String dateKey);
}
