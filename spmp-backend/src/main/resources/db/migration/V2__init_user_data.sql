-- ============================================================
-- SPMP 用户权限管理模块 DML - 初始化数据
-- ============================================================

-- 1. 超级管理员账号（密码: Spmp@2026 BCrypt加密）
INSERT INTO `sys_user` (`id`, `username`, `password`, `real_name`, `phone`, `phone_hash`, `status`, `del_flag`, `create_by`, `update_by`)
VALUES (1, 'admin', '$2a$10$yUdDuM3Lbb9Lon9a76jbW.XKP3Re7LiAmis.Jenq1vGm6J08BeYd.', '超级管理员',
        'AES_PLACEHOLDER_13800000000', 'e10adc3949ba59abbe56e057f20f883e', 0, 0, 'system', 'system');

-- 2. 预置角色
INSERT INTO `sys_role` (`id`, `role_name`, `role_code`, `data_permission_level`, `status`, `sort`, `remark`, `create_by`, `update_by`) VALUES
(1, '超级管理员', 'super_admin', 'ALL', 0, 1, '系统全局管理', 'system', 'system'),
(2, '物业管理员', 'property_admin', 'COMMUNITY', 0, 2, '所管辖小区的日常管理', 'system', 'system'),
(3, '片区经理', 'area_manager', 'AREA', 0, 3, '所辖片区多个小区的监管', 'system', 'system'),
(4, '楼栋管家', 'building_steward', 'BUILDING', 0, 4, '具体楼栋的日常管理', 'system', 'system'),
(5, '维修人员', 'repairman', 'SELF', 0, 5, '处理分配的报修工单', 'system', 'system'),
(6, '业主', 'owner', 'SELF', 0, 6, '移动端物业交互', 'system', 'system');

-- 3. 管理员-角色关联
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES (1, 1);

-- 4. 预置菜单树
-- 一级目录
INSERT INTO `sys_menu` (`id`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `permission`, `icon`, `sort`, `status`, `create_by`, `update_by`) VALUES
(1, '系统管理', 0, 'D', '/system', NULL, NULL, 'setting', 1, 0, 'system', 'system'),
(2, '日志管理', 0, 'D', '/log', NULL, NULL, 'document', 2, 0, 'system', 'system');

-- 二级菜单
INSERT INTO `sys_menu` (`id`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `permission`, `icon`, `sort`, `status`, `create_by`, `update_by`) VALUES
(101, '用户管理', 1, 'M', '/system/users', 'system/user/index', 'user:user:list', 'user', 1, 0, 'system', 'system'),
(102, '角色管理', 1, 'M', '/system/roles', 'system/role/index', 'user:role:list', 'peoples', 2, 0, 'system', 'system'),
(103, '菜单管理', 1, 'M', '/system/menus', 'system/menu/index', 'user:menu:list', 'tree-table', 3, 0, 'system', 'system'),
(201, '登录日志', 2, 'M', '/log/login-logs', 'log/loginLog/index', 'user:log:list', 'logininfor', 1, 0, 'system', 'system'),
(202, '操作日志', 2, 'M', '/log/operation-logs', 'log/operationLog/index', 'user:log:list', 'form', 2, 0, 'system', 'system');

-- 三级按钮权限 - 用户管理
INSERT INTO `sys_menu` (`id`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `permission`, `icon`, `sort`, `status`, `create_by`, `update_by`) VALUES
(1011, '用户查询', 101, 'B', NULL, NULL, 'user:user:list', NULL, 1, 0, 'system', 'system'),
(1012, '用户新增', 101, 'B', NULL, NULL, 'user:user:create', NULL, 2, 0, 'system', 'system'),
(1013, '用户编辑', 101, 'B', NULL, NULL, 'user:user:edit', NULL, 3, 0, 'system', 'system'),
(1014, '用户删除', 101, 'B', NULL, NULL, 'user:user:delete', NULL, 4, 0, 'system', 'system'),
(1015, '密码重置', 101, 'B', NULL, NULL, 'user:user:reset-pwd', NULL, 5, 0, 'system', 'system');

-- 三级按钮权限 - 角色管理
INSERT INTO `sys_menu` (`id`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `permission`, `icon`, `sort`, `status`, `create_by`, `update_by`) VALUES
(1021, '角色查询', 102, 'B', NULL, NULL, 'user:role:list', NULL, 1, 0, 'system', 'system'),
(1022, '角色新增', 102, 'B', NULL, NULL, 'user:role:create', NULL, 2, 0, 'system', 'system'),
(1023, '角色编辑', 102, 'B', NULL, NULL, 'user:role:edit', NULL, 3, 0, 'system', 'system'),
(1024, '角色删除', 102, 'B', NULL, NULL, 'user:role:delete', NULL, 4, 0, 'system', 'system'),
(1025, '权限分配', 102, 'B', NULL, NULL, 'user:role:assign', NULL, 5, 0, 'system', 'system');

-- 三级按钮权限 - 菜单管理
INSERT INTO `sys_menu` (`id`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `permission`, `icon`, `sort`, `status`, `create_by`, `update_by`) VALUES
(1031, '菜单查询', 103, 'B', NULL, NULL, 'user:menu:list', NULL, 1, 0, 'system', 'system'),
(1032, '菜单新增', 103, 'B', NULL, NULL, 'user:menu:create', NULL, 2, 0, 'system', 'system'),
(1033, '菜单编辑', 103, 'B', NULL, NULL, 'user:menu:edit', NULL, 3, 0, 'system', 'system'),
(1034, '菜单删除', 103, 'B', NULL, NULL, 'user:menu:delete', NULL, 4, 0, 'system', 'system');

-- 5. 超级管理员角色-菜单关联（全部菜单）
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, id FROM `sys_menu`;

-- 6. 角色数据权限示例
INSERT INTO `sys_role_data_permission` (`role_id`, `data_type`, `data_id`) VALUES
(3, 'AREA', 1),
(2, 'COMMUNITY', 1),
(4, 'BUILDING', 1);
