-- V11__init_billing_data.sql
-- CR05 缴费管理模块初始化数据

INSERT INTO `sys_menu` (`id`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `permission`, `icon`, `sort`, `status`, `create_by`, `update_by`) VALUES
(14000, '缴费管理', 0, 'D', '/billing', NULL, NULL, 'wallet', 4, 0, 'system', 'system'),
(14100, '费用配置', 14000, 'M', '/billing/config', 'billing/config/index', 'billing:config:list', 'setting', 1, 0, 'system', 'system'),
(14200, '账单管理', 14000, 'M', '/billing/bills', 'billing/bills/index', 'billing:bill:list', 'list', 2, 0, 'system', 'system'),
(14300, '逾期管理', 14000, 'M', '/billing/overdue', 'billing/overdue/index', 'billing:overdue:list', 'warning', 3, 0, 'system', 'system'),
(14400, '收费统计', 14000, 'M', '/billing/statistics', 'billing/statistics/index', 'billing:statistics', 'chart', 4, 0, 'system', 'system'),
(14101, '查看配置', 14100, 'B', NULL, NULL, 'billing:config:list', NULL, 1, 0, 'system', 'system'),
(14102, '编辑配置', 14100, 'B', NULL, NULL, 'billing:config:edit', NULL, 2, 0, 'system', 'system'),
(14201, '账单查询', 14200, 'B', NULL, NULL, 'billing:bill:list', NULL, 1, 0, 'system', 'system'),
(14202, '批量生成', 14200, 'B', NULL, NULL, 'billing:bill:generate', NULL, 2, 0, 'system', 'system'),
(14203, '手动创建', 14200, 'B', NULL, NULL, 'billing:bill:create', NULL, 3, 0, 'system', 'system'),
(14204, '账单减免', 14200, 'B', NULL, NULL, 'billing:bill:reduce', NULL, 4, 0, 'system', 'system'),
(14205, '账单导出', 14200, 'B', NULL, NULL, 'billing:bill:export', NULL, 5, 0, 'system', 'system'),
(14301, '逾期查看', 14300, 'B', NULL, NULL, 'billing:overdue:list', NULL, 1, 0, 'system', 'system'),
(14302, '催收操作', 14300, 'B', NULL, NULL, 'billing:overdue:urge', NULL, 2, 0, 'system', 'system'),
(14401, '统计查看', 14400, 'B', NULL, NULL, 'billing:statistics', NULL, 1, 0, 'system', 'system');

INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, id FROM `sys_menu` WHERE id >= 14000 AND id < 15000;

INSERT INTO `base_dict_category` (`id`, `category_code`, `category_name`, `description`, `sort_order`, `status`, `create_by`) VALUES
(200, 'FEE_TYPE', '费用类型', '缴费管理费用类型分类', 11, 'ENABLED', 'system');

INSERT INTO `base_dict` (`id`, `category_id`, `category_code`, `dict_code`, `dict_name`, `dict_value`, `sort_order`, `status`, `create_by`) VALUES
(2001, 200, 'FEE_TYPE', 'PROPERTY_FEE', '物业费', 'PROPERTY_FEE', 1, 'ENABLED', 'system'),
(2002, 200, 'FEE_TYPE', 'PARKING_FEE', '停车费', 'PARKING_FEE', 2, 'ENABLED', 'system'),
(2003, 200, 'FEE_TYPE', 'WATER_FEE', '水费', 'WATER_FEE', 3, 'ENABLED', 'system'),
(2004, 200, 'FEE_TYPE', 'ELECTRICITY_FEE', '电费', 'ELECTRICITY_FEE', 4, 'ENABLED', 'system'),
(2005, 200, 'FEE_TYPE', 'GAS_FEE', '燃气费', 'GAS_FEE', 5, 'ENABLED', 'system');
