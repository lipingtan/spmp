-- ============================================
-- CR06 公告管理 — DML 初始化数据
-- 菜单权限初始化（权限码已对齐后端 @PreAuthorize：
--   notice:list / notice:detail / notice:create / notice:withdraw
--   notice:repush / notice:stats / notice:approve）
-- ============================================

-- 1. 公告管理一级目录
INSERT INTO `sys_menu` (`id`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `permission`, `icon`, `sort`, `status`, `create_by`, `update_by`) VALUES
(13000, '公告管理', 0, 'D', '/notice', NULL, NULL, 'bell', 7, 0, 'system', 'system');

-- 2. 二级菜单（公告列表 / 发布公告 / 审批列表）
INSERT INTO `sys_menu` (`id`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `permission`, `icon`, `sort`, `status`, `create_by`, `update_by`) VALUES
(13100, '公告列表', 13000, 'M', '/notice/list',    'notice/list/index',    'notice:list',    'list',  1, 0, 'system', 'system'),
(13200, '发布公告', 13000, 'M', '/notice/create',  'notice/create/index',  'notice:create',  'edit',  2, 0, 'system', 'system'),
(13300, '审批列表', 13000, 'M', '/notice/approve', 'notice/approve/index', 'notice:approve', 'stamp', 3, 0, 'system', 'system');

-- 3. 按钮级权限
INSERT INTO `sys_menu` (`id`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `permission`, `icon`, `sort`, `status`, `create_by`, `update_by`) VALUES
(13101, '公告查询', 13100, 'B', NULL, NULL, 'notice:list',     NULL, 1, 0, 'system', 'system'),
(13102, '公告详情', 13100, 'B', NULL, NULL, 'notice:detail',   NULL, 2, 0, 'system', 'system'),
(13103, '公告撤回', 13100, 'B', NULL, NULL, 'notice:withdraw', NULL, 3, 0, 'system', 'system'),
(13104, '公告重推', 13100, 'B', NULL, NULL, 'notice:repush',   NULL, 4, 0, 'system', 'system'),
(13105, '阅读统计', 13100, 'B', NULL, NULL, 'notice:stats',    NULL, 5, 0, 'system', 'system'),
(13201, '提交发布', 13200, 'B', NULL, NULL, 'notice:create',   NULL, 1, 0, 'system', 'system'),
(13301, '审批通过', 13300, 'B', NULL, NULL, 'notice:approve',  NULL, 1, 0, 'system', 'system'),
(13302, '审批驳回', 13300, 'B', NULL, NULL, 'notice:approve',  NULL, 2, 0, 'system', 'system');

-- 4. 超级管理员角色（role_id=1）绑定本模块全部菜单/按钮
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, id FROM `sys_menu` WHERE id >= 13000 AND id < 14000;
