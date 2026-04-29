-- ============================================================
-- SPMP 基础数据管理模块 DDL
-- 表前缀：bs_
-- 引擎：InnoDB | 字符集：utf8mb4_general_ci
-- ============================================================

-- 1. 片区表
CREATE TABLE `bs_district` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `district_name` VARCHAR(64) NOT NULL COMMENT '片区名称',
    `district_code` VARCHAR(20) NOT NULL COMMENT '片区编码(DIS+6位序号)',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态(0-启用 1-停用)',
    `remark` VARCHAR(256) DEFAULT NULL COMMENT '备注',
    `del_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记(0-正常 1-删除)',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '创建人',
    `update_by` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '更新人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_district_name` (`district_name`, `del_flag`),
    UNIQUE KEY `uk_district_code` (`district_code`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='片区表';

-- 2. 小区表
CREATE TABLE `bs_community` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `community_name` VARCHAR(128) NOT NULL COMMENT '小区名称',
    `community_code` VARCHAR(20) NOT NULL COMMENT '小区编码(COM+6位序号)',
    `address` VARCHAR(256) NOT NULL COMMENT '小区地址',
    `district_id` BIGINT NOT NULL COMMENT '所属片区ID',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    `property_company` VARCHAR(128) DEFAULT NULL COMMENT '物业公司名称',
    `area` DECIMAL(12,2) DEFAULT NULL COMMENT '小区面积(平方米)',
    `longitude` DECIMAL(10,7) DEFAULT NULL COMMENT '经度',
    `latitude` DECIMAL(10,7) DEFAULT NULL COMMENT '纬度',
    `image` VARCHAR(512) DEFAULT NULL COMMENT '小区图片URL',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态(0-启用 1-停用)',
    `remark` VARCHAR(256) DEFAULT NULL COMMENT '备注',
    `del_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记(0-正常 1-删除)',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '创建人',
    `update_by` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '更新人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_community_code` (`community_code`),
    UNIQUE KEY `uk_community_name_district` (`community_name`, `district_id`, `del_flag`),
    KEY `idx_district_id` (`district_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='小区表';

-- 3. 楼栋表
CREATE TABLE `bs_building` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `building_name` VARCHAR(64) NOT NULL COMMENT '楼栋名称',
    `building_code` VARCHAR(32) NOT NULL COMMENT '楼栋编号',
    `community_id` BIGINT NOT NULL COMMENT '所属小区ID',
    `above_ground_floors` INT NOT NULL DEFAULT 0 COMMENT '地上层数',
    `underground_floors` INT NOT NULL DEFAULT 0 COMMENT '地下层数',
    `units_per_floor` INT NOT NULL DEFAULT 0 COMMENT '每层户数',
    `building_type` VARCHAR(20) NOT NULL DEFAULT 'RESIDENTIAL' COMMENT '楼栋类型(RESIDENTIAL/COMMERCIAL/GARAGE/MIXED/OTHER)',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态(0-启用 1-停用)',
    `remark` VARCHAR(256) DEFAULT NULL COMMENT '备注',
    `del_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记(0-正常 1-删除)',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '创建人',
    `update_by` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '更新人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_building_code_community` (`building_code`, `community_id`, `del_flag`),
    KEY `idx_community_id` (`community_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='楼栋表';

-- 4. 单元表
CREATE TABLE `bs_unit` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `unit_name` VARCHAR(64) NOT NULL COMMENT '单元名称',
    `unit_code` VARCHAR(32) NOT NULL COMMENT '单元编号',
    `building_id` BIGINT NOT NULL COMMENT '所属楼栋ID',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态(0-启用 1-停用)',
    `remark` VARCHAR(256) DEFAULT NULL COMMENT '备注',
    `del_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记(0-正常 1-删除)',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '创建人',
    `update_by` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '更新人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_unit_code_building` (`unit_code`, `building_id`, `del_flag`),
    KEY `idx_building_id` (`building_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='单元表';

-- 5. 房屋表
CREATE TABLE `bs_house` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `house_code` VARCHAR(32) NOT NULL COMMENT '房屋编号',
    `unit_id` BIGINT NOT NULL COMMENT '所属单元ID',
    `floor` INT NOT NULL COMMENT '楼层',
    `building_area` DECIMAL(10,2) NOT NULL COMMENT '建筑面积(平方米)',
    `usable_area` DECIMAL(10,2) DEFAULT NULL COMMENT '使用面积(平方米)',
    `house_status` VARCHAR(20) NOT NULL DEFAULT 'VACANT' COMMENT '房屋状态(VACANT/OCCUPIED/RENTED/RENOVATING)',
    `house_type` VARCHAR(20) NOT NULL DEFAULT 'RESIDENCE' COMMENT '房屋类型(RESIDENCE/SHOP/PARKING/OFFICE/OTHER)',
    `remark` VARCHAR(256) DEFAULT NULL COMMENT '备注',
    `del_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记(0-正常 1-删除)',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '创建人',
    `update_by` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '更新人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_house_code_unit` (`house_code`, `unit_id`, `del_flag`),
    KEY `idx_unit_id` (`unit_id`),
    KEY `idx_house_status` (`house_status`),
    KEY `idx_house_type` (`house_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='房屋表';

-- 6. 房屋状态变更历史表
CREATE TABLE `bs_house_status_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `house_id` BIGINT NOT NULL COMMENT '房屋ID',
    `old_status` VARCHAR(20) DEFAULT NULL COMMENT '变更前状态',
    `new_status` VARCHAR(20) NOT NULL COMMENT '变更后状态',
    `change_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '变更时间',
    `operator_id` BIGINT DEFAULT NULL COMMENT '操作人ID',
    PRIMARY KEY (`id`),
    KEY `idx_house_id` (`house_id`),
    KEY `idx_change_time` (`change_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='房屋状态变更历史表';

-- ============================================================
-- sys_operation_log 表新增 before_data 列（支持变更前后对比）
-- ============================================================
ALTER TABLE `sys_operation_log`
    ADD COLUMN `before_data` TEXT DEFAULT NULL COMMENT '变更前数据(JSON)' AFTER `response_result`;

-- 7. 字典分类表
CREATE TABLE `base_dict_category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `category_code` VARCHAR(64) NOT NULL COMMENT '分类编码',
    `category_name` VARCHAR(128) NOT NULL COMMENT '分类名称',
    `description` VARCHAR(256) DEFAULT NULL COMMENT '描述',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `status` VARCHAR(20) NOT NULL DEFAULT 'ENABLED' COMMENT '状态(ENABLED/DISABLED)',
    `del_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记(0-正常 1-删除)',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '创建人',
    `update_by` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '更新人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_category_code` (`category_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='字典分类表';

-- 8. 字典值表
CREATE TABLE `base_dict` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `category_id` BIGINT NOT NULL COMMENT '分类ID',
    `category_code` VARCHAR(64) NOT NULL COMMENT '分类编码',
    `dict_code` VARCHAR(64) NOT NULL COMMENT '字典编码',
    `dict_name` VARCHAR(128) NOT NULL COMMENT '字典名称',
    `dict_value` VARCHAR(256) DEFAULT NULL COMMENT '字典值',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `status` VARCHAR(20) NOT NULL DEFAULT 'ENABLED' COMMENT '状态(ENABLED/DISABLED)',
    `remark` VARCHAR(256) DEFAULT NULL COMMENT '备注',
    `del_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记(0-正常 1-删除)',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '创建人',
    `update_by` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '更新人',
    PRIMARY KEY (`id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_category_code` (`category_code`),
    KEY `idx_dict_code` (`dict_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='字典值表';
