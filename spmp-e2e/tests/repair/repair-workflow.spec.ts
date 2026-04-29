import { test, expect } from '@playwright/test'
import { loginAsRepairStaff } from '../helpers/auth'

test.describe('H5 维修人员端 — 工单处理流程', () => {

  test.beforeEach(async ({ page }) => {
    await loginAsRepairStaff(page)
  })

  test('登录后进入首页，我的页面有维修工作台入口', async ({ page }) => {
    // 点击底部 Tab "我的"
    await page.locator('.van-tabbar-item').filter({ hasText: '我的' }).click()
    await page.waitForLoadState('networkidle')

    // 验证维修工作台入口存在
    await expect(page.locator('.van-cell__title').filter({ hasText: '维修工作台' })).toBeVisible()
  })

  test('进入维修工作台，显示待处理和本月完成数', async ({ page }) => {
    // 从我的页面进入维修工作台
    await page.locator('.van-tabbar-item').filter({ hasText: '我的' }).click()
    await page.locator('.van-cell__title').filter({ hasText: '维修工作台' }).click()

    await page.waitForURL('**/repair/dashboard')
    await page.waitForLoadState('networkidle')

    // 验证工作台页面元素
    await expect(page.locator('.stat-label').filter({ hasText: '今日待处理' })).toBeVisible()
    await expect(page.locator('.stat-label').filter({ hasText: '本月完成' })).toBeVisible()
    await expect(page.locator('.van-cell__title').filter({ hasText: '待处理工单' })).toBeVisible()
    await expect(page.locator('.van-cell__title').filter({ hasText: '历史工单' })).toBeVisible()
    await expect(page.locator('.van-cell__title').filter({ hasText: '切换到业主端' })).toBeVisible()
  })

  test('进入待处理工单列表', async ({ page }) => {
    await page.goto('/repair/dashboard')
    await page.waitForLoadState('networkidle')

    await page.locator('.van-cell__title').filter({ hasText: '待处理工单' }).click()
    await page.waitForURL('**/repair/pending')
    await page.waitForLoadState('networkidle')

    // 验证页面标题
    await expect(page.locator('.van-nav-bar__title')).toHaveText('待处理工单')
  })

  test('进入历史工单列表', async ({ page }) => {
    await page.goto('/repair/dashboard')
    await page.waitForLoadState('networkidle')

    await page.locator('.van-cell__title').filter({ hasText: '历史工单' }).click()
    await page.waitForURL('**/repair/history')
    await page.waitForLoadState('networkidle')

    await expect(page.locator('.van-nav-bar__title')).toHaveText('历史工单')
  })

  test('从维修工作台切换到业主端', async ({ page }) => {
    await page.goto('/repair/dashboard')
    await page.waitForLoadState('networkidle')

    await page.locator('.van-cell__title').filter({ hasText: '切换到业主端' }).click()
    await page.waitForURL('**/home')

    // 验证底部 Tab 栏存在（业主端特有）
    await expect(page.locator('.van-tabbar')).toBeVisible()
  })

})
