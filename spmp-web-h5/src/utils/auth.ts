/**
 * JWT 认证工具
 */

export function getTokenRoles(token?: string | null): string[] {
  if (!token) return []
  try {
    const payloadPart = token.split('.')[1]
    if (!payloadPart) return []
    const base64 = payloadPart.replace(/-/g, '+').replace(/_/g, '/')
    const padded = base64 + '='.repeat((4 - (base64.length % 4)) % 4)
    const payload = JSON.parse(window.atob(padded))
    return Array.isArray(payload.roles) ? payload.roles : []
  } catch {
    return []
  }
}

export function isRepairRole(token?: string | null): boolean {
  const roles = getTokenRoles(token)
  const normalized = roles.map((role) => String(role).toLowerCase())
  return normalized.some((role) => ['repairman', 'repair_staff', 'repair', 'role_repair_staff'].includes(role))
}
