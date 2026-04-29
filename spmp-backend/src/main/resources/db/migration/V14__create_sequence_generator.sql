-- V14__create_sequence_generator.sql
-- CR03 Redis序列号可恢复优化：通用序列号段分配表

CREATE TABLE `cm_sequence_generator` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `biz_code` VARCHAR(64) NOT NULL COMMENT '业务编码，如 PAYMENT/WORK_ORDER',
    `date_key` VARCHAR(8) NOT NULL COMMENT '日期分区，yyyyMMdd',
    `max_allocated` BIGINT NOT NULL DEFAULT 0 COMMENT '已分配到的最大值（高水位）',
    `step` INT NOT NULL DEFAULT 1000 COMMENT '业务步长',
    `version` INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_biz_date` (`biz_code`, `date_key`),
    KEY `idx_biz_code` (`biz_code`),
    KEY `idx_date_key` (`date_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='通用序列号段分配表';

INSERT INTO `cm_sequence_generator` (`biz_code`, `date_key`, `max_allocated`, `step`, `version`)
VALUES
    ('PAYMENT', '19700101', 0, 1000, 0),
    ('WORK_ORDER', '19700101', 0, 200, 0)
ON DUPLICATE KEY UPDATE
    `step` = VALUES(`step`);
