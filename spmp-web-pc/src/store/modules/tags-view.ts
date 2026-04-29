/**
 * 标签页状态管理
 * - 已访问标签列表
 * - 添加 / 关闭 / 关闭其他 / 关闭全部
 */

import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { RouteLocationNormalized } from 'vue-router'
import type { TagView } from '@/types'

export const useTagsViewStore = defineStore('tags-view', () => {
  const visitedViews = ref<TagView[]>([])

  /**
   * 添加标签（去重）
   */
  function addView(route: RouteLocationNormalized): void {
    // 隐藏路由不添加标签
    if (route.meta.hidden) return
    // 去重：已存在则不重复添加
    const exists = visitedViews.value.some((v) => v.path === route.path)
    if (exists) return

    visitedViews.value.push({
      name: route.name as string,
      path: route.path,
      fullPath: route.fullPath,
      title: (route.meta.title as string) || '未命名',
      affix: route.meta.affix as boolean | undefined
    })
  }

  /**
   * 关闭标签（affix 不可关闭），返回应激活的标签
   */
  function closeView(view: TagView): TagView | undefined {
    if (view.affix) return view

    const index = visitedViews.value.findIndex((v) => v.path === view.path)
    if (index === -1) return undefined

    visitedViews.value.splice(index, 1)

    // 返回应激活的标签：优先右侧，其次左侧，最后首页
    if (visitedViews.value.length === 0) return undefined
    if (index <= visitedViews.value.length - 1) {
      return visitedViews.value[index]
    }
    return visitedViews.value[index - 1]
  }

  /**
   * 关闭其他标签（保留当前和 affix）
   */
  function closeOtherViews(view: TagView): void {
    visitedViews.value = visitedViews.value.filter(
      (v) => v.affix || v.path === view.path
    )
  }

  /**
   * 关闭全部标签（仅保留 affix）
   */
  function closeAllViews(): void {
    visitedViews.value = visitedViews.value.filter((v) => v.affix)
  }

  return {
    visitedViews,
    addView,
    closeView,
    closeOtherViews,
    closeAllViews
  }
})
