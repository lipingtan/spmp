import { Page } from '@playwright/test'

/**
 * 维修人员登录
 * 本地测试环境已配置 captcha.skip=true，验证码填任意值即可
 */
export async function loginAsRepairStaff(page: Page, username = 'worker1', password = 'Spmp@2026') {
  await page.goto('/login')
  await page.waitForLoadState('networkidle')

  await page.locator('input[placeholder*="用户名"]').fill(username)
  await page.locator('input[placeholder*="密码"]').fill(password)
  await page.locator('input[placeholder*="验证码"]').fill('test')
  await page.locator('button:has-text("登录")').click()

  // 等待跳转到首页
  await page.waitForURL('**/home', { timeout: 10000 })
}
