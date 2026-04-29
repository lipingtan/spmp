-- ============================================================
-- SPMP 基础数据管理模块 DML - 初始化数据
-- ============================================================

-- ============================================
-- 1. 基础数据管理菜单（ID 从 10000 开始）
-- ============================================

-- 一级目录：基础数据
INSERT INTO `sys_menu` (`id`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `permission`, `icon`, `sort`, `status`, `create_by`, `update_by`) VALUES
(10000, '基础数据', 0, 'D', '/base', NULL, NULL, 'office-building', 3, 0, 'system', 'system');

-- 二级菜单
INSERT INTO `sys_menu` (`id`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `permission`, `icon`, `sort`, `status`, `create_by`, `update_by`) VALUES
(10100, '片区管理', 10000, 'M', '/base/districts', 'base/district/index', 'base:district:list', 'location', 1, 0, 'system', 'system'),
(10200, '小区管理', 10000, 'M', '/base/communities', 'base/community/index', 'base:community:list', 'house', 2, 0, 'system', 'system'),
(10300, '楼栋管理', 10000, 'M', '/base/buildings', 'base/building/index', 'base:building:list', 'school', 3, 0, 'system', 'system'),
(10400, '单元管理', 10000, 'M', '/base/units', 'base/unit/index', 'base:unit:list', 'menu', 4, 0, 'system', 'system'),
(10500, '房屋管理', 10000, 'M', '/base/houses', 'base/house/index', 'base:house:list', 'key', 5, 0, 'system', 'system');

-- 三级按钮权限 — 片区管理
INSERT INTO `sys_menu` (`id`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `permission`, `icon`, `sort`, `status`, `create_by`, `update_by`) VALUES
(10101, '片区查询', 10100, 'B', NULL, NULL, 'base:district:list', NULL, 1, 0, 'system', 'system'),
(10102, '片区新增', 10100, 'B', NULL, NULL, 'base:district:create', NULL, 2, 0, 'system', 'system'),
(10103, '片区编辑', 10100, 'B', NULL, NULL, 'base:district:edit', NULL, 3, 0, 'system', 'system'),
(10104, '片区删除', 10100, 'B', NULL, NULL, 'base:district:delete', NULL, 4, 0, 'system', 'system');

-- 三级按钮权限 — 小区管理
INSERT INTO `sys_menu` (`id`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `permission`, `icon`, `sort`, `status`, `create_by`, `update_by`) VALUES
(10201, '小区查询', 10200, 'B', NULL, NULL, 'base:community:list', NULL, 1, 0, 'system', 'system'),
(10202, '小区新增', 10200, 'B', NULL, NULL, 'base:community:create', NULL, 2, 0, 'system', 'system'),
(10203, '小区编辑', 10200, 'B', NULL, NULL, 'base:community:edit', NULL, 3, 0, 'system', 'system'),
(10204, '小区删除', 10200, 'B', NULL, NULL, 'base:community:delete', NULL, 4, 0, 'system', 'system'),
(10205, '小区导入', 10200, 'B', NULL, NULL, 'base:community:import', NULL, 5, 0, 'system', 'system');

-- 三级按钮权限 — 楼栋管理
INSERT INTO `sys_menu` (`id`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `permission`, `icon`, `sort`, `status`, `create_by`, `update_by`) VALUES
(10301, '楼栋查询', 10300, 'B', NULL, NULL, 'base:building:list', NULL, 1, 0, 'system', 'system'),
(10302, '楼栋新增', 10300, 'B', NULL, NULL, 'base:building:create', NULL, 2, 0, 'system', 'system'),
(10303, '楼栋编辑', 10300, 'B', NULL, NULL, 'base:building:edit', NULL, 3, 0, 'system', 'system'),
(10304, '楼栋删除', 10300, 'B', NULL, NULL, 'base:building:delete', NULL, 4, 0, 'system', 'system'),
(10305, '楼栋导入', 10300, 'B', NULL, NULL, 'base:building:import', NULL, 5, 0, 'system', 'system');

-- 三级按钮权限 — 单元管理
INSERT INTO `sys_menu` (`id`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `permission`, `icon`, `sort`, `status`, `create_by`, `update_by`) VALUES
(10401, '单元查询', 10400, 'B', NULL, NULL, 'base:unit:list', NULL, 1, 0, 'system', 'system'),
(10402, '单元新增', 10400, 'B', NULL, NULL, 'base:unit:create', NULL, 2, 0, 'system', 'system'),
(10403, '单元编辑', 10400, 'B', NULL, NULL, 'base:unit:edit', NULL, 3, 0, 'system', 'system'),
(10404, '单元删除', 10400, 'B', NULL, NULL, 'base:unit:delete', NULL, 4, 0, 'system', 'system'),
(10405, '单元导入', 10400, 'B', NULL, NULL, 'base:unit:import', NULL, 5, 0, 'system', 'system');

-- 三级按钮权限 — 房屋管理
INSERT INTO `sys_menu` (`id`, `menu_name`, `parent_id`, `menu_type`, `path`, `component`, `permission`, `icon`, `sort`, `status`, `create_by`, `update_by`) VALUES
(10501, '房屋查询', 10500, 'B', NULL, NULL, 'base:house:list', NULL, 1, 0, 'system', 'system'),
(10502, '房屋新增', 10500, 'B', NULL, NULL, 'base:house:create', NULL, 2, 0, 'system', 'system'),
(10503, '房屋编辑', 10500, 'B', NULL, NULL, 'base:house:edit', NULL, 3, 0, 'system', 'system'),
(10504, '房屋删除', 10500, 'B', NULL, NULL, 'base:house:delete', NULL, 4, 0, 'system', 'system'),
(10505, '房屋导入', 10500, 'B', NULL, NULL, 'base:house:import', NULL, 5, 0, 'system', 'system');

-- ============================================
-- 2. 超级管理员角色关联基础数据菜单
-- ============================================
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, id FROM `sys_menu` WHERE id >= 10000 AND id < 11000;

-- ============================================
-- 3. 测试用基础数据
-- ============================================

-- 片区测试数据（3 片区）
INSERT INTO `bs_district` (`id`, `district_name`, `district_code`, `status`, `remark`, `create_by`, `update_by`) VALUES
(1, '东区', 'DIS000001', 0, '东部片区', 'system', 'system'),
(2, '西区', 'DIS000002', 0, '西部片区', 'system', 'system'),
(3, '南区', 'DIS000003', 0, '南部片区', 'system', 'system');

-- 小区测试数据（5 小区）
INSERT INTO `bs_community` (`id`, `community_name`, `community_code`, `address`, `district_id`, `phone`, `property_company`, `status`, `create_by`, `update_by`) VALUES
(1, '阳光花园', 'COM000001', '东区阳光路100号', 1, '010-12345678', '阳光物业', 0, 'system', 'system'),
(2, '翠湖苑', 'COM000002', '东区翠湖路200号', 1, '010-23456789', '翠湖物业', 0, 'system', 'system'),
(3, '碧水湾', 'COM000003', '西区碧水路300号', 2, '010-34567890', '碧水物业', 0, 'system', 'system'),
(4, '金色家园', 'COM000004', '西区金色路400号', 2, '010-45678901', '金色物业', 0, 'system', 'system'),
(5, '绿城小区', 'COM000005', '南区绿城路500号', 3, '010-56789012', '绿城物业', 0, 'system', 'system');

-- 楼栋测试数据（8 楼栋）
INSERT INTO `bs_building` (`id`, `building_name`, `building_code`, `community_id`, `above_ground_floors`, `underground_floors`, `units_per_floor`, `building_type`, `status`, `create_by`, `update_by`) VALUES
(1, '1栋', 'B001', 1, 18, 2, 4, 'RESIDENTIAL', 0, 'system', 'system'),
(2, '2栋', 'B002', 1, 18, 2, 4, 'RESIDENTIAL', 0, 'system', 'system'),
(3, '1栋', 'B001', 2, 12, 1, 2, 'RESIDENTIAL', 0, 'system', 'system'),
(4, '1栋', 'B001', 3, 20, 2, 6, 'RESIDENTIAL', 0, 'system', 'system'),
(5, '2栋', 'B002', 3, 20, 2, 6, 'RESIDENTIAL', 0, 'system', 'system'),
(6, '1栋', 'B001', 4, 15, 1, 4, 'MIXED', 0, 'system', 'system'),
(7, '1栋', 'B001', 5, 22, 2, 4, 'RESIDENTIAL', 0, 'system', 'system'),
(8, '2栋', 'B002', 5, 22, 2, 4, 'RESIDENTIAL', 0, 'system', 'system');

-- 单元测试数据（2 单元，阳光花园1栋）
INSERT INTO `bs_unit` (`id`, `unit_name`, `unit_code`, `building_id`, `status`, `create_by`, `update_by`) VALUES
(1, '1单元', 'U001', 1, 0, 'system', 'system'),
(2, '2单元', 'U002', 1, 0, 'system', 'system');

-- 房屋测试数据（4 房屋，阳光花园1栋1单元）
INSERT INTO `bs_house` (`id`, `house_code`, `unit_id`, `floor`, `building_area`, `usable_area`, `house_status`, `house_type`, `create_by`, `update_by`) VALUES
(1, '1-1-101', 1, 1, 89.50, 72.30, 'OCCUPIED', 'RESIDENCE', 'system', 'system'),
(2, '1-1-102', 1, 1, 120.00, 98.50, 'VACANT', 'RESIDENCE', 'system', 'system'),
(3, '1-1-201', 1, 2, 89.50, 72.30, 'RENTED', 'RESIDENCE', 'system', 'system'),
(4, '1-1-202', 1, 2, 120.00, 98.50, 'RENOVATING', 'RESIDENCE', 'system', 'system');

-- 房屋状态变更历史测试数据（2 条记录）
INSERT INTO `bs_house_status_log` (`house_id`, `old_status`, `new_status`, `change_time`, `operator_id`) VALUES
(1, NULL, 'VACANT', '2026-01-01 00:00:00', 1),
(1, 'VACANT', 'OCCUPIED', '2026-03-15 10:30:00', 1);
