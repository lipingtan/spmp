-- V12__create_notice_tables.sql
-- CR06 公告管理模块建表语句

CREATE TABLE `nt_announcement` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `title` VARCHAR(200) NOT NULL COMMENT '公告标题',
    `content` LONGTEXT NOT NULL COMMENT '公告内容（富文本）',
    `notice_type` VARCHAR(20) NOT NULL COMMENT '公告类型（NORMAL/EMERGENCY/ACTIVITY）',
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING_APPROVAL' COMMENT '公告状态（PENDING_APPROVAL/PUBLISHED/WITHDRAWN）',
    `approval_status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '审批状态（PENDING/APPROVED/REJECTED）',
    `top_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '是否置顶（0-否 1-是）',
    `publish_time` DATETIME DEFAULT NULL COMMENT '发布时间',
    `expire_time` DATETIME DEFAULT NULL COMMENT '过期时间',
    `creator_id` BIGINT NOT NULL COMMENT '创建人ID',
    `del_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记（0-正常 1-删除）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '创建人',
    `update_by` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '更新人',
    PRIMARY KEY (`id`),
    KEY `idx_status_top_publish` (`status`, `top_flag`, `publish_time`),
    KEY `idx_creator_id` (`creator_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='公告主表';

CREATE TABLE `nt_announcement_scope` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `announcement_id` BIGINT NOT NULL COMMENT '公告ID',
    `scope_type` VARCHAR(20) NOT NULL COMMENT '范围类型（ALL/COMMUNITY/BUILDING）',
    `target_id` BIGINT DEFAULT NULL COMMENT '目标ID（ALL时可为空）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_announcement_id` (`announcement_id`),
    KEY `idx_scope_type_target` (`scope_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='公告推送范围表';

CREATE TABLE `nt_announcement_target_snapshot` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `announcement_id` BIGINT NOT NULL COMMENT '公告ID',
    `user_id` BIGINT NOT NULL COMMENT '目标用户ID',
    `snapshot_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '快照时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_announcement_user` (`announcement_id`, `user_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='公告目标用户快照表';

CREATE TABLE `nt_announcement_read_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `announcement_id` BIGINT NOT NULL COMMENT '公告ID',
    `user_id` BIGINT NOT NULL COMMENT '已读用户ID',
    `read_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '阅读时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_announcement_read_user` (`announcement_id`, `user_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='公告已读记录表';

CREATE TABLE `nt_announcement_approval` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `announcement_id` BIGINT NOT NULL COMMENT '公告ID',
    `approval_status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '审批状态',
    `current_node_id` BIGINT DEFAULT NULL COMMENT '当前审批节点ID',
    `submit_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    `complete_time` DATETIME DEFAULT NULL COMMENT '完成时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_announcement_id` (`announcement_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='公告审批实例表';

CREATE TABLE `nt_announcement_approval_node` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `approval_id` BIGINT NOT NULL COMMENT '审批实例ID',
    `node_order` INT NOT NULL COMMENT '节点顺序',
    `approver_id` BIGINT NOT NULL COMMENT '审批人ID',
    `node_status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '节点状态',
    `approve_time` DATETIME DEFAULT NULL COMMENT '审批时间',
    `approve_remark` VARCHAR(256) DEFAULT NULL COMMENT '审批备注',
    PRIMARY KEY (`id`),
    KEY `idx_approval_id` (`approval_id`),
    KEY `idx_approver_id` (`approver_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='公告审批节点表';

CREATE TABLE `nt_announcement_operation_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `announcement_id` BIGINT NOT NULL COMMENT '公告ID',
    `operation_type` VARCHAR(20) NOT NULL COMMENT '操作类型（CREATE/APPROVE/REJECT/WITHDRAW/REPUSH/READ）',
    `operator_id` BIGINT NOT NULL COMMENT '操作人ID',
    `operator_name` VARCHAR(64) DEFAULT NULL COMMENT '操作人姓名',
    `remark` VARCHAR(256) DEFAULT NULL COMMENT '备注',
    `operation_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (`id`),
    KEY `idx_announcement_id` (`announcement_id`),
    KEY `idx_operator_id` (`operator_id`),
    KEY `idx_operation_time` (`operation_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='公告业务操作日志表';
