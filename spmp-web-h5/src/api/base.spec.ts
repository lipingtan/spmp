import { describe, expect, it } from 'vitest'
import { normalizeCascadeOptions } from './base'

describe('base cascade normalize', () => {
  it('prefers communityName when name is missing', () => {
    const result = normalizeCascadeOptions([{ id: 1, communityName: '阳光花园' }])
    expect(result).toEqual([{ id: 1, name: '阳光花园' }])
  })

  it('falls back to code then id string', () => {
    const result = normalizeCascadeOptions([
      { id: 2, code: 'COMM-002' },
      { id: 3 }
    ])
    expect(result).toEqual([
      { id: 2, name: 'COMM-002' },
      { id: 3, name: '3' }
    ])
  })
})
