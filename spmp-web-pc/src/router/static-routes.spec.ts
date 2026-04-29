import { describe, expect, it } from 'vitest'
import { asyncRoutes, constantRoutes } from './static-routes'

describe('static route config', () => {
  it('contains login and not-found routes', () => {
    const names = constantRoutes.map((route) => route.name)
    expect(names).toContain('login')
    expect(names).toContain('not-found')
  })

  it('contains billing and notice business routes', () => {
    const children = asyncRoutes[0].children ?? []
    const names = children.map((route) => route.name)
    expect(names).toContain('billing-bills')
    expect(names).toContain('notice-list')
  })
})
