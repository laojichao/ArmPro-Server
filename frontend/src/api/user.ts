import { get, post, put, del } from '@/utils/request'

// ========== 认证相关 ==========
export function login(data: { username: string; password: string; rememberMe?: boolean }) {
  return post<{ token: string }>('/user/login', data)
}

export function register(data: { username: string; password: string; email: string }) {
  return post<null>('/user/register', data)
}

// ========== 用户信息 ==========
export function getUserInfo() {
  return get<{
    id: number
    username: string
    nickname: string
    email: string
    avatar: string
    vipExpireTime: string
  }>('/user/info')
}

export function changePassword(data: { oldPassword: string; newPassword: string }) {
  return post<null>('/user/change-password', data)
}

export function rechargeCard(data: { cardKey: string }) {
  return post<{ expireTime: string }>('/user/recharge', data)
}

// ========== 仪表盘 ==========
export function getDashboardStats() {
  return get<{
    todayTotal: number
    todayUsed: number
    todayRemaining: number
    appCount: number
  }>('/user/dashboard/stats')
}

// ========== 应用管理 ==========
export interface SoftInfo {
  id: number
  name: string
  packageName: string
  version: string
  appKey: string
  status: number
  createdAt: string
  updatedAt: string
}

export function getSoftList(params: { page: number; pageSize: number; keyword?: string }) {
  return get<{ list: SoftInfo[]; total: number }>('/user/soft/list', params)
}

export function getSoftDetail(id: number) {
  return get<SoftInfo>(`/user/soft/${id}`)
}

export function deleteSoft(id: number) {
  return del<null>(`/user/soft/${id}`)
}

// ========== 应用配置 ==========
export interface SoftConfig {
  id: number
  softId: number
  noticeTitle: string
  noticeContent: string
  noticeStyle: string
  trialCount: number
  bindMode: string
  versionCode: number
  versionName: string
  updateUrl: string
  updateLog: string
  forceUpdate: boolean
  customModules: string
  adEnabled: boolean
  adConfig: string
}

export function getSoftConfig(softId: number) {
  return get<SoftConfig>(`/user/soft/${softId}/config`)
}

export function updateSoftConfig(softId: number, data: Partial<SoftConfig>) {
  return put<null>(`/user/soft/${softId}/config`, data)
}

// ========== 功能开关 ==========
export interface FeatureToggle {
  id: number
  name: string
  key: string
  enabled: boolean
  description: string
}

export function getFeatureToggles(softId: number) {
  return get<FeatureToggle[]>(`/user/soft/${softId}/features`)
}

export function updateFeatureToggle(softId: number, featureId: number, enabled: boolean) {
  return put<null>(`/user/soft/${softId}/features/${featureId}`, { enabled })
}

// ========== 统计数据 ==========
export interface DailyStat {
  date: string
  count: number
}

export function getSoftStats(softId: number, days?: number) {
  return get<DailyStat[]>(`/user/soft/${softId}/stats`, { days: days || 6 })
}

// ========== 卡密管理 ==========
export interface CardInfo {
  id: number
  cardKey: string
  type: string
  duration: number
  status: number
  usedBy: string
  usedAt: string
  createdAt: string
  remark: string
}

export function getCardList(params: { page: number; pageSize: number; status?: number; keyword?: string }) {
  return get<{ list: CardInfo[]; total: number }>('/user/card/list', params)
}

export function generateCards(data: {
  count: number
  duration: number
  type: string
  remark?: string
}) {
  return post<CardInfo[]>('/user/card/generate', data)
}

export function deleteCards(ids: number[]) {
  return post<null>('/user/card/delete', { ids })
}

export function freezeCards(ids: number[]) {
  return post<null>('/user/card/freeze', { ids })
}

export function unfreezeCards(ids: number[]) {
  return post<null>('/user/card/unfreeze', { ids })
}
