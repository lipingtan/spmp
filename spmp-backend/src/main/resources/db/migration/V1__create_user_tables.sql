-- ============================================================
-- SPMP 用户权限管理模块 DDL
-- 表前缀：sys_
-- 引擎：InnoDB | 字符集：utf8mb4_general_ci
-- ============================================================

-- 1. 用户表
CREATE TABLE `sys_user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username` VARCHAR(64) NOT NULL COMMENT '用户名',
    `password` VARCHAR(128) NOT NULL COMMENT '密码(BCrypt加密)',
    `real_name` VARCHAR(64) NOT NULL COMMENT '姓名',
    `phone` VARCHAR(256) NOT NULL COMMENT '手机号(AES加密)',
    `phone_hash` VARCHAR(64) NOT NULL COMMENT '手机号SHA-256哈希(用于查询和唯一索引)',
    `avatar` VARCHAR(256) DEFAULT NULL COMMENT '头像URL(预留)',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态(0-启用 1-禁用)',
    `del_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记(0-正常 1-删除)',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '创建人',
    `update_by` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '更新人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_phone_hash` (`phone_hash`),
    KEY `idx_status_del_flag` (`status`, `del_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户表';

-- 2. 角色表
CREATE TABLE `sys_role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `role_name` VARCHAR(64) NOT NULL COMMENT '角色名称',
    `role_code` VARCHAR(64) NOT NULL COMMENT '角色编码',
    `data_permission_level` VARCHAR(20) NOT NULL DEFAULT 'SELF' COMMENT '数据权限级别(ALL/AREA/COMMUNITY/BUILDING/SELF)',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态(0-启用 1-禁用)',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `remark` VARCHAR(256) DEFAULT NULL COMMENT '备注',
    `del_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记(0-正常 1-删除)',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '创建人',
    `update_by` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '更新人',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_code` (`role_code`),
    UNIQUE KEY `uk_role_name` (`role_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色表';

-- 3. 菜单表
CREATE TABLE `sys_menu` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `menu_name` VARCHAR(64) NOT NULL COMMENT '菜单名称',
    `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父级ID(0为顶级)',
    `menu_type` CHAR(1) NOT NULL COMMENT '菜单类型(D-目录 M-菜单 B-按钮)',
    `path` VARCHAR(256) DEFAULT NULL COMMENT '路由路径',
    `component` VARCHAR(256) DEFAULT NULL COMMENT '组件路径',
    `permission` VARCHAR(128) DEFAULT NULL COMMENT '权限标识',
    `icon` VARCHAR(64) DEFAULT NULL COMMENT '图标',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态(0-启用 1-禁用)',
    `del_flag` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记(0-正常 1-删除)',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `create_by` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '创建人',
    `update_by` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '更新人',
    PRIMARY KEY (`id`),
    KEY `idx_parent_id` (`parent_id`),
    KEY `idx_permission` (`permission`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='菜单表';

-- 4. 用户角色关联表
CREATE TABLE `sys_user_role` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户角色关联表';

-- 5. 角色菜单关联表
CREATE TABLE `sys_role_menu` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `menu_id` BIGINT NOT NULL COMMENT '菜单ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_menu` (`role_id`, `menu_id`),
    KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色菜单关联表';

-- 6. 角色数据权限关联表
CREATE TABLE `sys_role_data_permission` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `data_type` VARCHAR(20) NOT NULL COMMENT '数据类型(AREA/COMMUNITY/BUILDING)',
    `data_id` BIGINT NOT NULL COMMENT '关联数据ID',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_type_data` (`role_id`, `data_type`, `data_id`),
    KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色数据权限关联表';

-- 7. 登录日志表
CREATE TABLE `sys_login_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username` VARCHAR(64) NOT NULL COMMENT '用户名',
    `login_ip` VARCHAR(64) DEFAULT NULL COMMENT '登录IP',
    `login_location` VARCHAR(128) DEFAULT NULL COMMENT '登录地点',
    `browser` VARCHAR(128) DEFAULT NULL COMMENT '浏览器',
    `os` VARCHAR(128) DEFAULT NULL COMMENT '操作系统',
    `login_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    `login_result` TINYINT NOT NULL DEFAULT 0 COMMENT '登录结果(0-成功 1-失败)',
    `fail_reason` VARCHAR(256) DEFAULT NULL COMMENT '失败原因',
    PRIMARY KEY (`id`),
    KEY `idx_username` (`username`),
    KEY `idx_login_time` (`login_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='登录日志表';

-- 8. 操作日志表
CREATE TABLE `sys_operation_log` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `operator_id` BIGINT DEFAULT NULL COMMENT '操作人ID',
    `operator_name` VARCHAR(64) DEFAULT NULL COMMENT '操作人用户名',
    `module` VARCHAR(64) NOT NULL COMMENT '模块名称',
    `operation_type` VARCHAR(32) NOT NULL COMMENT '操作类型(CREATE/UPDATE/DELETE/EXPORT/OTHER)',
    `description` VARCHAR(256) DEFAULT NULL COMMENT '操作描述',
    `request_method` VARCHAR(10) DEFAULT NULL COMMENT '请求方法(GET/POST/PUT/DELETE)',
    `request_url` VARCHAR(256) DEFAULT NULL COMMENT '请求URL',
    `request_params` TEXT DEFAULT NULL COMMENT '请求参数(JSON,password脱敏)',
    `response_result` TEXT DEFAULT NULL COMMENT '响应结果(JSON,超2000字符截断)',
    `operation_ip` VARCHAR(64) DEFAULT NULL COMMENT '操作IP',
    `operation_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    `cost_time` BIGINT DEFAULT NULL COMMENT '耗时(ms)',
    PRIMARY KEY (`id`),
    KEY `idx_operator_id` (`operator_id`),
    KEY `idx_operation_time` (`operation_time`),
    KEY `idx_module` (`module`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='操作日志表';
