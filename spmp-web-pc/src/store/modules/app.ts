/**
 * 应用状态管理
 * - 侧边栏折叠状态（持久化到 localStorage）
 * - 语言设置（预留）
 */

import { defineStore } from 'pinia'
import { ref } from 'vue'

const THEME_STORAGE_KEY = 'pc_theme_mode'
export type ThemeMode = 'business' | 'luxury-dark' | 'aurora-local'

export const useAppStore = defineStore('app', () => {
  // 从 localStorage 读取侧边栏折叠状态
  const sidebarCollapsed = ref<boolean>(
    localStorage.getItem('sidebar_collapsed') === 'true'
  )
  const locale = ref<string>('zh-CN')
  const themeMode = ref<ThemeMode>('business')
  const themeTransitionActive = ref<boolean>(false)

  /**
   * 切换侧边栏折叠状态并持久化
   */
  function toggleSidebar(): void {
    sidebarCollapsed.value = !sidebarCollapsed.value
    localStorage.setItem('sidebar_collapsed', String(sidebarCollapsed.value))
  }

  function applyTheme(mode: ThemeMode): void {
    themeTransitionActive.value = true
    themeMode.value = mode
    // aurora-local 为布局级主题，不走全局变量覆盖
    document.documentElement.setAttribute('data-theme', mode === 'aurora-local' ? 'business' : mode)
    localStorage.setItem(THEME_STORAGE_KEY, mode)
    window.setTimeout(() => {
      themeTransitionActive.value = false
    }, 420)
  }

  function initTheme(): void {
    const saved = localStorage.getItem(THEME_STORAGE_KEY) as ThemeMode | null
    if (saved === 'business' || saved === 'luxury-dark' || saved === 'aurora-local') {
      applyTheme(saved)
      return
    }
    applyTheme('business')
  }

  return {
    sidebarCollapsed,
    locale,
    themeMode,
    themeTransitionActive,
    toggleSidebar,
    applyTheme,
    initTheme
  }
})
