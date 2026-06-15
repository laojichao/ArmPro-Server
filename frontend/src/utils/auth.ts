import { useUserStore } from '@/stores/user'
import router from '@/router'

const TOKEN_KEY = 'token'

/**
 * 获取 Token
 */
export function getToken(): string {
  return localStorage.getItem(TOKEN_KEY) || ''
}

/**
 * 存储 Token
 */
export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token)
}

/**
 * 移除 Token
 */
export function removeToken(): void {
  localStorage.removeItem(TOKEN_KEY)
}

/**
 * 检查是否已登录
 */
export function isLoggedIn(): boolean {
  return !!getToken()
}

/**
 * 退出登录
 * 清除本地存储的 Token 和用户信息，并跳转到登录页
 */
export function logout(): void {
  const userStore = useUserStore()
  userStore.clearToken()
  removeToken()
  router.push('/login')
}

/**
 * 获取用户角色列表
 */
export function getUserRoles(): string[] {
  const userStore = useUserStore()
  return userStore.userInfo?.roles || []
}

/**
 * 检查是否为管理员
 */
export function isAdmin(): boolean {
  const roles = getUserRoles()
  return roles.includes('admin') || roles.includes('ADMIN')
}
