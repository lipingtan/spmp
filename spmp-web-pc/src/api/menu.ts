/**
 * 菜单管理 API
 */
import request from '@/utils/request'

export interface MenuTreeItem {
  id: number
  menuName: string
  parentId: number
  menuType: string
  path: string
  component: string
  permission: string
  icon: string
  sort: number
  status: number
  children: MenuTreeItem[]
}

export interface MenuCreateParams {
  menuName: string
  parentId: number
  menuType: string
  path?: string
  component?: string
  permission?: string
  icon?: string
  sort: number
}

export interface MenuUpdateParams {
  menuName?: string
  path?: string
  component?: string
  permission?: string
  icon?: string
  sort?: number
  status?: number
}

/** 菜单树查询 */
export function getMenuTree(menuName?: string, status?: number): Promise<MenuTreeItem[]> {
  return request.get('/user/menus/tree', { params: { menuName, status } })
}

/** 当前用户菜单树 */
export function getUserMenuTree(): Promise<MenuTreeItem[]> {
  return request.get('/user/menus/user-tree')
}

/** 新增菜单 */
export function createMenu(data: MenuCreateParams): Promise<void> {
  return request.post('/user/menus', data)
}

/** 编辑菜单 */
export function updateMenu(id: number, data: MenuUpdateParams): Promise<void> {
  return request.put(`/user/menus/${id}`, data)
}

/** 删除菜单 */
export function deleteMenu(id: number): Promise<void> {
  return request.delete(`/user/menus/${id}`)
}
