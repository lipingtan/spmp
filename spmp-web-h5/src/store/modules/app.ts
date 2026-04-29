/**
 * 应用状态管理（H5 端）
 * - 语言设置（预留）
 * - 页面过渡动画名称
 */

import { defineStore } from 'pinia'
import { ref } from 'vue'

const THEME_STORAGE_KEY = 'h5_theme_mode'
export type ThemeMode = 'business' | 'luxury-dark'

export const useAppStore = defineStore('app', () => {
  // 当前语言
  const locale = ref<string>('zh-CN')

  // 页面过渡动画名称（由路由守卫动态设置）
  const transitionName = ref<string>('slide-left')
  const themeMode = ref<ThemeMode>('business')
  const themeTransitionActive = ref(false)

  /**
   * 设置过渡动画名称
   */
  function setTransitionName(name: string): void {
    transitionName.value = name
  }

  function applyTheme(mode: ThemeMode): void {
    themeTransitionActive.value = true
    themeMode.value = mode
    document.documentElement.setAttribute('data-theme', mode)
    localStorage.setItem(THEME_STORAGE_KEY, mode)
    window.setTimeout(() => {
      themeTransitionActive.value = false
    }, 420)
  }

  function initTheme(): void {
    const saved = localStorage.getItem(THEME_STORAGE_KEY) as ThemeMode | null
    if (saved === 'business' || saved === 'luxury-dark') {
      applyTheme(saved)
      return
    }
    applyTheme('business')
  }

  return {
    locale,
    transitionName,
    themeMode,
    themeTransitionActive,
    setTransitionName
    ,
    applyTheme,
    initTheme
  }
})
