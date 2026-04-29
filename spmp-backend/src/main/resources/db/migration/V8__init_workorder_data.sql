-- ============================================
-- CR04 工单管理 — DML 初始化数据
-- 报修类型字典 + 菜单权限 + 测试数据
-- ============================================

-- 1. 报修类型字典分类
INSERT INTO `base_dict_category` (`id`, `category_code`, `category_name`, `description`, `sort_order`, `status`, `create_by`) VALUES
(100, 'WORKORDER_TYPE', '报修类型', '工单报修类型分类', 10, 'ENABLED', 'system');

-- 2. 报修类型字典值
INSERT INTO `base_dict` (`id`, `category_id`, `category_code`, `dict_code`, `dict_name`, `dict_value`, `sort_order`, `status`, `create_by`) VALUES
(1001, 100, 'WORKORDER_TYPE', 'WATER_ELECTRIC', '水电维修', 'WATER_ELECTRIC', 1, 'ENABLED', 'system'),
(1002, 100, 'WORKORDER_TYPE', 'DOOR_WINDOW', '门窗维修', 'DOOR_WINDOW', 2, 'ENABLED', 'system'),
(1003, 100, 'WORKORDER_TYPE', 'PIPELINE', '管道疏通', 'PIPELINE', 3, 'ENABLED', 'system'),
(1004, 100, 'WORKORDER_TYPE', 'PUBLIC_FACILITY', '公共设施', 'PUBLIC_FACILITY', 4, 'ENABLED', 'system'),
(1005, 100, 'WORKORDER_TYPE', 'OTHER', '其他', 'OTHER', 5, 'ENABLED', 'system');

-- 3. 菜单初始化数据（sys_menu 表，ID 从 12000 开始）
INSERT INTO `sys_menu` (`id`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `permission`, `icon`, `sort`, `status`, `create_by`, `update_by`) VALUES
(12000, '工单管理', 0, 'D', '/workorder', NULL, NULL, 'tickets', 5, 0, 'system', 'system');

INSERT INTO `sys_menu` (`id`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `permission`, `icon`, `sort`, `status`, `create_by`, `update_by`) VALUES
(12100, '工单列表', 12000, 'M', '/workorder/list', 'workorder/order/index', 'workorder:list', 'list', 1, 0, 'system', 'system'),
(12200, '工单统计', 12000, 'M', '/workorder/statistics', 'workorder/statistics/index', 'workorder:statistics', 'chart', 2, 0, 'system', 'system');

INSERT INTO `sys_menu` (`id`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `permission`, `icon`, `sort`, `status`, `create_by`, `update_by`) VALUES
(12101, '工单查询', 12100, 'B', NULL, NULL, 'workorder:list', NULL, 1, 0, 'system', 'system'),
(12102, '工单详情', 12100, 'B', NULL, NULL, 'workorder:detail', NULL, 2, 0, 'system', 'system'),
(12103, '工单派发', 12100, 'B', NULL, NULL, 'workorder:dispatch', NULL, 3, 0, 'system', 'system'),
(12104, '工单取消', 12100, 'B', NULL, NULL, 'workorder:cancel', NULL, 4, 0, 'system', 'system'),
(12105, '工单导出', 12100, 'B', NULL, NULL, 'workorder:export', NULL, 5, 0, 'system', 'system'),
(12201, '统计查看', 12200, 'B', NULL, NULL, 'workorder:statistics', NULL, 1, 0, 'system', 'system');

-- 4. 超级管理员角色关联工单管理菜单
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, id FROM `sys_menu` WHERE id >= 12000 AND id < 13000;
