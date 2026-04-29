/**
 * 基础数据 API（H5 端）
 * 级联查询接口：小区→楼栋→单元→房屋
 * 后端统一入口：GET /base/cascade?level={LEVEL}&parentId={ID}
 */
import request from '@/utils/request'

// ==================== VO 类型 ====================

/** 级联选项（通用） */
export interface CascadeOption {
  /** 选项 ID */
  id: number
  /** 选项名称 */
  name: string
}

interface RawCascadeOption {
  id?: number | string
  name?: string
  communityName?: string
  buildingName?: string
  unitName?: string
  houseName?: string
  label?: string
  text?: string
  code?: string
}

// ==================== API 方法 ====================

/**
 * 通用级联查询
 * @param level 层级：COMMUNITY / BUILDING / UNIT / HOUSE
 * @param parentId 父级 ID（查询小区时不传）
 */
function getCascadeData(level: string, parentId?: number): Promise<CascadeOption[]> {
  return request
    .get<any, RawCascadeOption[]>('/base/cascade', { params: { level, parentId } })
    .then((list) => normalizeCascadeOptions(list))
}

export function normalizeCascadeOptions(list: RawCascadeOption[] | undefined | null): CascadeOption[] {
  if (!Array.isArray(list)) {
    return []
  }
  return list
    .filter((item) => item && item.id !== undefined && item.id !== null)
    .map((item) => ({
      id: Number(item.id),
      // 兼容后端不同字段命名，统一保证下拉展示为“名称”而不是编码/ID
      name:
        item.name ||
        item.communityName ||
        item.buildingName ||
        item.unitName ||
        item.houseName ||
        item.label ||
        item.text ||
        item.code ||
        String(item.id)
    }))
}

/** 获取小区列表 */
export function getCommunities(): Promise<CascadeOption[]> {
  return getCascadeData('COMMUNITY')
}

/** 获取楼栋列表（按小区） */
export function getBuildings(communityId: number): Promise<CascadeOption[]> {
  return getCascadeData('BUILDING', communityId)
}

/** 获取单元列表（按楼栋） */
export function getUnits(buildingId: number): Promise<CascadeOption[]> {
  return getCascadeData('UNIT', buildingId)
}

/** 获取房屋列表（按单元） */
export function getHouses(unitId: number): Promise<CascadeOption[]> {
  return getCascadeData('HOUSE', unitId)
}
