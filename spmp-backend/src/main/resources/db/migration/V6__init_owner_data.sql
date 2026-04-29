-- ============================================
-- CR03 业主管理 — DML 脚本
-- 初始化菜单数据（sys_menu 表，ID 从 11000 开始）
-- ============================================

-- 一级目录：业主管理
INSERT INTO `sys_menu` (`id`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `permission`, `icon`, `sort`, `status`, `create_by`, `update_by`) VALUES
(11000, '业主管理', 0, 'D', '/owner', NULL, NULL, 'user', 4, 0, 'system', 'system');

-- 二级菜单
INSERT INTO `sys_menu` (`id`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `permission`, `icon`, `sort`, `status`, `create_by`, `update_by`) VALUES
(11100, '业主列表', 11000, 'M', '/owner/owners', 'owner/owner/index', 'owner:owner:list', 'peoples', 1, 0, 'system', 'system'),
(11200, '认证审批', 11000, 'M', '/owner/certifications', 'owner/certification/index', 'owner:certify:list', 'check', 2, 0, 'system', 'system');

-- 三级按钮权限 — 业主列表
INSERT INTO `sys_menu` (`id`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `permission`, `icon`, `sort`, `status`, `create_by`, `update_by`) VALUES
(11101, '业主查询', 11100, 'B', NULL, NULL, 'owner:owner:list', NULL, 1, 0, 'system', 'system'),
(11102, '业主新增', 11100, 'B', NULL, NULL, 'owner:owner:create', NULL, 2, 0, 'system', 'system'),
(11103, '业主编辑', 11100, 'B', NULL, NULL, 'owner:owner:edit', NULL, 3, 0, 'system', 'system'),
(11104, '业主删除', 11100, 'B', NULL, NULL, 'owner:owner:delete', NULL, 4, 0, 'system', 'system'),
(11105, '房产绑定', 11100, 'B', NULL, NULL, 'owner:property:binding', NULL, 5, 0, 'system', 'system'),
(11106, '解除绑定', 11100, 'B', NULL, NULL, 'owner:property:unbind', NULL, 6, 0, 'system', 'system');

-- 三级按钮权限 — 认证审批
INSERT INTO `sys_menu` (`id`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `permission`, `icon`, `sort`, `status`, `create_by`, `update_by`) VALUES
(11201, '认证查询', 11200, 'B', NULL, NULL, 'owner:certify:list', NULL, 1, 0, 'system', 'system'),
(11202, '认证审批', 11200, 'B', NULL, NULL, 'owner:certify:approve', NULL, 2, 0, 'system', 'system');

-- 超级管理员角色关联业主管理菜单
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, id FROM `sys_menu` WHERE id >= 11000 AND id < 12000;
