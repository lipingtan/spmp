/**
 * 房产 API（H5 端）
 * 我的房产接口（查询绑定列表）
 */
import type { H5ProfileVO } from './owner'

// ==================== 说明 ====================
// 我的房产数据通过 getProfile 接口返回的 propertyBindings 字段获取
// 此模块提供便捷方法，从个人信息中提取房产列表

// ==================== 类型重导出 ====================

/** 房产绑定项（从 owner 模块重导出） */
export type { PropertyBindingItem } from './owner'

// ==================== 工具方法 ====================

/**
 * 从个人信息中提取房产绑定列表
 * @param profile H5 个人信息
 * @returns 房产绑定列表
 */
export function extractProperties(profile: H5ProfileVO) {
  return profile.propertyBindings ?? []
}
