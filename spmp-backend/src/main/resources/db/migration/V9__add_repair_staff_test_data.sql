-- ============================================
-- 补充维修人员测试数据
-- 密码: Spmp@2026 (BCrypt)
-- ============================================

-- 维修人员用户（ID 从 100 开始避免冲突）
INSERT INTO `sys_user` (`id`, `username`, `password`, `real_name`, `phone`, `phone_hash`, `status`, `del_flag`, `create_by`, `update_by`) VALUES
(100, 'repair01', '$2a$10$yUdDuM3Lbb9Lon9a76jbW.XKP3Re7LiAmis.Jenq1vGm6J08BeYd.', '张维修', 'AES_PLACEHOLDER_13900000001', 'hash_placeholder_001', 0, 0, 'system', 'system'),
(101, 'repair02', '$2a$10$yUdDuM3Lbb9Lon9a76jbW.XKP3Re7LiAmis.Jenq1vGm6J08BeYd.', '李维修', 'AES_PLACEHOLDER_13900000002', 'hash_placeholder_002', 0, 0, 'system', 'system');

-- 关联维修人员角色（role_id=5 即 repairman）
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES
(100, 5),
(101, 5);
