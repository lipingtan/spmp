-- ============================================
-- CR04 工单管理 — DDL 脚本
-- 创建 7 张业务表：wo_work_order、wo_work_order_image、wo_dispatch_record、
--   wo_repair_material、wo_evaluation、wo_urge_record、wo_work_order_log
-- ============================================

-- 1. wo_work_order — 工单主表
CREATE TABLE `wo_work_order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_no` VARCHAR(32) NOT NULL COMMENT '工单编号（WO+年月日+4位序号）',
    `order_type` VARCHAR(32) NOT NULL COMMENT '报修类型（字典编码 WORKORDER_TYPE）',
    `address_type` VARCHAR(20) NOT NULL DEFAULT 'HOUSE' COMMENT '地址类型（HOUSE-房屋 PUBLIC-公共区域）',
    `community_id` BIGINT NOT NULL COMMENT '小区ID（冗余，关联 bs_community.id）',
    `house_id` BIGINT DEFAULT NULL COMMENT '房屋ID（房屋报修时关联 bs_house.id）',
    `building_id` BIGINT DEFAULT NULL COMMENT '楼栋ID（公共区域报修时关联 bs_building.id）',
    `unit_id` BIGINT DEFAULT NULL COMMENT '单元ID（公共区域报修时关联 bs_unit.id，可选）',
    `reporter_id` BIGINT NOT NULL COMMENT '报修人（业主ID，关联 ow_owner.id）',
    `reporter_name` VARCHAR(64) NOT NULL COMMENT '报修人姓名（冗余）',
    `reporter_phone` VARCHAR(256) NOT NULL COMMENT '报修人手机号（AES-256 加密存储，冗余）',
    `description` TEXT NOT NULL COMMENT '问题描述',
    `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING_DISPATCH' COMMENT '工单状态（PENDING_DISPATCH-待派发 PENDING_ACCEPT-待接单 IN_PROGRESS-处理中 PENDING_VERIFY-待验收 COMPLETED-已完成 CANCELLED-已取消 FORCE_CLOSED-已关闭）',
    `repair_user_id` BIGINT DEFAULT NULL COMMENT '当前维修人员ID（关联 sys_user.id）',
    `reject_count` INT NOT NULL DEFAULT 0 COMMENT '验收不通过次数（累计）',
    `urge_count` INT NOT NULL DEFAULT 0 COMMENT '催单次数（累计）',
    `last_urge_time` DATETIME DEFAULT NULL COMMENT '最近催单时间',
    `expected_complete_time` DATETIME DEFAULT NULL COMMENT '预计完成时间',
    `actual_start_time` DATETIME DEFAULT NULL COMMENT '实际开始时间（接单时间）',
    `actual_complete_time` DATETIME DEFAULT NULL COMMENT '实际完成时间（维修完成时间）',
    `repair_duration` INT DEFAULT NULL COMMENT '实际维修时长（分钟）',
    `cancel_reason` VARCHAR(512) DEFAULT NULL COMMENT '取消/关闭原因',
    `cancel_by` BIGINT DEFAULT NULL COMMENT '取消/关闭操作人ID',
    `del_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记（0-正常 1-删除）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '创建人',
    `update_by` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '更新人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_community_id` (`community_id`),
    KEY `idx_house_id` (`house_id`),
    KEY `idx_building_id` (`building_id`),
    KEY `idx_reporter_id` (`reporter_id`),
    KEY `idx_repair_user_id` (`repair_user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_order_type` (`order_type`),
    KEY `idx_create_time` (`create_time`),
    KEY `idx_status_create_time` (`status`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='工单主表';

-- 2. wo_work_order_image — 工单图片表
CREATE TABLE `wo_work_order_image` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_id` BIGINT NOT NULL COMMENT '工单ID',
    `image_url` VARCHAR(512) NOT NULL COMMENT '图片URL',
    `image_type` VARCHAR(20) NOT NULL COMMENT '图片类型（REPORT-报修图片 REPAIR-维修图片 REJECT-验收不通过图片）',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_image_type` (`image_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='工单图片表';

-- 3. wo_dispatch_record — 派发记录表
CREATE TABLE `wo_dispatch_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_id` BIGINT NOT NULL COMMENT '工单ID',
    `repair_user_id` BIGINT NOT NULL COMMENT '维修人员ID（关联 sys_user.id）',
    `repair_user_name` VARCHAR(64) DEFAULT NULL COMMENT '维修人员姓名（冗余）',
    `dispatch_type` VARCHAR(20) NOT NULL COMMENT '派发类型（MANUAL-手动 AUTO-自动）',
    `dispatcher_id` BIGINT DEFAULT NULL COMMENT '派发人ID（手动派发时为管理员ID）',
    `dispatcher_name` VARCHAR(64) DEFAULT NULL COMMENT '派发人姓名（冗余）',
    `remark` VARCHAR(512) DEFAULT NULL COMMENT '备注',
    `dispatch_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '派发时间',
    `del_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记（0-正常 1-删除）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '创建人',
    `update_by` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '更新人',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_repair_user_id` (`repair_user_id`),
    KEY `idx_dispatch_time` (`dispatch_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='派发记录表';

-- 4. wo_repair_material — 维修材料表
CREATE TABLE `wo_repair_material` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_id` BIGINT NOT NULL COMMENT '工单ID',
    `material_name` VARCHAR(128) NOT NULL COMMENT '材料名称',
    `quantity` DECIMAL(10,2) NOT NULL COMMENT '数量',
    `unit` VARCHAR(20) DEFAULT NULL COMMENT '单位（个/米/千克等）',
    `unit_price` DECIMAL(10,2) NOT NULL COMMENT '单价',
    `total_price` DECIMAL(10,2) NOT NULL COMMENT '总价',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='维修材料表';

-- 5. wo_evaluation — 评价记录表
CREATE TABLE `wo_evaluation` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_id` BIGINT NOT NULL COMMENT '工单ID',
    `score` TINYINT NOT NULL COMMENT '评分（1-5星）',
    `content` VARCHAR(512) DEFAULT NULL COMMENT '评价内容',
    `evaluator_id` BIGINT NOT NULL COMMENT '评价人（业主ID）',
    `evaluate_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评价时间',
    `evaluate_type` VARCHAR(20) NOT NULL DEFAULT 'OWNER' COMMENT '评价类型（OWNER-业主评价 AUTO-自动验收默认）',
    `del_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记（0-正常 1-删除）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '创建人',
    `update_by` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '更新人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_id` (`order_id`),
    KEY `idx_evaluator_id` (`evaluator_id`),
    KEY `idx_score` (`score`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='评价记录表';

-- 6. wo_urge_record — 催单记录表
CREATE TABLE `wo_urge_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_id` BIGINT NOT NULL COMMENT '工单ID',
    `urge_user_id` BIGINT NOT NULL COMMENT '催单人（业主ID）',
    `urge_user_name` VARCHAR(64) DEFAULT NULL COMMENT '催单人姓名（冗余）',
    `urge_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '催单时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_urge_time` (`urge_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='催单记录表';

-- 7. wo_work_order_log — 工单操作日志表
CREATE TABLE `wo_work_order_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_id` BIGINT NOT NULL COMMENT '工单ID',
    `action` VARCHAR(32) NOT NULL COMMENT '操作类型（CREATE/DISPATCH/ACCEPT/COMPLETE/VERIFY_PASS/VERIFY_REJECT/TRANSFER/CANCEL/FORCE_CLOSE/AUTO_DISPATCH/TIMEOUT_RETURN/AUTO_VERIFY/URGE）',
    `from_status` VARCHAR(20) DEFAULT NULL COMMENT '变更前状态',
    `to_status` VARCHAR(20) DEFAULT NULL COMMENT '变更后状态',
    `operator_id` BIGINT DEFAULT NULL COMMENT '操作人ID',
    `operator_name` VARCHAR(64) DEFAULT NULL COMMENT '操作人姓名',
    `operator_type` VARCHAR(20) DEFAULT NULL COMMENT '操作人类型（OWNER/ADMIN/REPAIR/SYSTEM）',
    `remark` VARCHAR(512) DEFAULT NULL COMMENT '备注',
    `operate_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_action` (`action`),
    KEY `idx_operate_time` (`operate_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='工单操作日志表';
