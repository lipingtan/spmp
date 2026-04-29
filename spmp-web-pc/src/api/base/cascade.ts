/**
 * 级联查询 API
 */
import request from '@/utils/request'

/** 级联节点 */
export interface CascadeItem {
  id: number
  name: string
  code: string
  status: number
}

type RawCascadeItem = Partial<CascadeItem> & {
  communityName?: string
  buildingName?: string
  unitName?: string
  districtName?: string
}

function normalizeCascadeItems(list: RawCascadeItem[]): CascadeItem[] {
  return list
    .map((item) => {
      const id = Number(item.id)
      if (!Number.isFinite(id) || id <= 0) return null
      const name =
        item.name ||
        item.communityName ||
        item.buildingName ||
        item.unitName ||
        item.districtName ||
        item.code ||
        String(id)
      return {
        id,
        name,
        code: item.code || '',
        status: Number(item.status ?? 0)
      }
    })
    .filter((item): item is CascadeItem => item !== null)
}

/** 级联树节点 */
export interface CascadeTreeNode {
  id: number
  name: string
  code: string
  children?: CascadeTreeNode[]
}

/**
 * 级联数据查询（逐级加载）
 * @param level 层级：DISTRICT / COMMUNITY / BUILDING / UNIT / HOUSE
 * @param parentId 父级 ID（顶级片区时不传）
 */
export function getCascadeData(level: string, parentId?: number): Promise<CascadeItem[]> {
  return request
    .get('/base/cascade', { params: { level, parentId } })
    .then((data) => normalizeCascadeItems(Array.isArray(data) ? data : []))
}

/**
 * 完整树查询（小区下楼栋→单元→房屋）
 * @param communityId 小区 ID
 */
export function getCascadeTree(communityId: number): Promise<CascadeTreeNode[]> {
  return request.get('/base/cascade/tree', { params: { communityId } })
}
