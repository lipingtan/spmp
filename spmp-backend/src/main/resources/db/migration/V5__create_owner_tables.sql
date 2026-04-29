-- ============================================
-- CR03 业主管理 — DDL 脚本
-- 创建 4 张业务表：ow_owner、ow_property_binding、ow_family_member、ow_certification
-- ============================================

-- 1. ow_owner — 业主表
CREATE TABLE `ow_owner` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` BIGINT DEFAULT NULL COMMENT '关联 sys_user.id，管理端录入时为空',
    `owner_name` VARCHAR(64) NOT NULL COMMENT '业主姓名',
    `phone` VARCHAR(256) NOT NULL COMMENT '手机号（AES-256 加密存储）',
    `id_card` VARCHAR(256) NOT NULL COMMENT '身份证号（AES-256 加密存储）',
    `gender` TINYINT NOT NULL DEFAULT 0 COMMENT '性别（0-未知 1-男 2-女）',
    `avatar` VARCHAR(512) DEFAULT NULL COMMENT '头像URL',
    `email` VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
    `emergency_contact` VARCHAR(64) DEFAULT NULL COMMENT '紧急联系人',
    `emergency_phone` VARCHAR(256) DEFAULT NULL COMMENT '紧急联系电话（AES-256 加密存储）',
    `owner_source` VARCHAR(20) NOT NULL DEFAULT 'ADMIN' COMMENT '业主来源（ADMIN-管理端录入 H5-自助注册）',
    `owner_status` VARCHAR(20) NOT NULL DEFAULT 'UNCERTIFIED' COMMENT '业主状态（UNCERTIFIED-未认证 CERTIFYING-认证中 CERTIFIED-已认证 DISABLED-已停用）',
    `previous_status` VARCHAR(20) DEFAULT NULL COMMENT '停用前状态（启用时恢复用）',
    `del_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记（0-正常 1-删除）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '创建人',
    `update_by` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '更新人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_phone` (`phone`, `del_flag`),
    UNIQUE KEY `uk_id_card` (`id_card`, `del_flag`),
    UNIQUE KEY `uk_user_id` (`user_id`, `del_flag`),
    KEY `idx_owner_status` (`owner_status`),
    KEY `idx_owner_source` (`owner_source`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='业主表';

-- 2. ow_property_binding — 房产绑定表
CREATE TABLE `ow_property_binding` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `owner_id` BIGINT NOT NULL COMMENT '业主ID',
    `house_id` BIGINT NOT NULL COMMENT '房屋ID（关联 bs_house.id）',
    `relation_type` VARCHAR(20) NOT NULL COMMENT '关系类型（OWNER-业主 TENANT-租户 FAMILY-家属）',
    `binding_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '绑定时间',
    `unbinding_time` DATETIME DEFAULT NULL COMMENT '解绑时间',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '绑定状态（0-有效 1-已解绑）',
    `del_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记（0-正常 1-删除）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '创建人',
    `update_by` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '更新人',
    PRIMARY KEY (`id`),
    KEY `idx_owner_id` (`owner_id`),
    KEY `idx_house_id` (`house_id`),
    KEY `idx_relation_type` (`relation_type`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='房产绑定表';

-- 3. ow_family_member — 家庭成员表
CREATE TABLE `ow_family_member` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `owner_id` BIGINT NOT NULL COMMENT '业主ID',
    `user_id` BIGINT DEFAULT NULL COMMENT '关联 sys_user.id',
    `member_name` VARCHAR(64) NOT NULL COMMENT '成员姓名',
    `phone` VARCHAR(256) NOT NULL COMMENT '手机号（AES-256 加密存储）',
    `id_card` VARCHAR(256) NOT NULL COMMENT '身份证号（AES-256 加密存储）',
    `relation` VARCHAR(20) NOT NULL COMMENT '与业主关系（SPOUSE-配偶 PARENT-父母 CHILD-子女 OTHER-其他）',
    `del_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记（0-正常 1-删除）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '创建人',
    `update_by` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '更新人',
    PRIMARY KEY (`id`),
    KEY `idx_owner_id` (`owner_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_relation` (`relation`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='家庭成员表';

-- 4. ow_certification — 认证申请表
CREATE TABLE `ow_certification` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `owner_id` BIGINT NOT NULL COMMENT '业主ID',
    `house_id` BIGINT NOT NULL COMMENT '房屋ID（关联 bs_house.id）',
    `cert_status` VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '认证状态（PENDING-待审批 APPROVED-已通过 REJECTED-已驳回）',
    `apply_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    `approve_time` DATETIME DEFAULT NULL COMMENT '审批时间',
    `approver_id` BIGINT DEFAULT NULL COMMENT '审批人ID',
    `reject_reason` VARCHAR(512) DEFAULT NULL COMMENT '驳回原因',
    `del_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记（0-正常 1-删除）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '创建人',
    `update_by` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '更新人',
    PRIMARY KEY (`id`),
    KEY `idx_owner_id` (`owner_id`),
    KEY `idx_house_id` (`house_id`),
    KEY `idx_cert_status` (`cert_status`),
    KEY `idx_apply_time` (`apply_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='认证申请表';
