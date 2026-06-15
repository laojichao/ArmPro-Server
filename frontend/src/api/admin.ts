import { get, post, put, del } from '@/utils/request'

// ==================== Dashboard ====================

export interface DashboardStats {
  totalUsers: number
  totalApps: number
  todayTasks: number
  todayIncome: number
}

export interface UserGrowthItem {
  date: string
  count: number
}

export interface AppUsageItem {
  appName: string
  userCount: number
}

export interface RecentLoginUser {
  id: number
  username: string
  email: string
  loginTime: string
  ip: string
}

export function getDashboardStats() {
  return get<DashboardStats>('/admin/dashboard/stats')
}

export function getUserGrowthTrend() {
  return get<UserGrowthItem[]>('/admin/dashboard/user-growth')
}

export function getAppUsageStats() {
  return get<AppUsageItem[]>('/admin/dashboard/app-usage')
}

export function getRecentLogins() {
  return get<RecentLoginUser[]>('/admin/dashboard/recent-logins')
}

// ==================== 用户管理 ====================

export interface AdminUser {
  id: number
  username: string
  email: string
  regTime: string
  loginCount: number
  vipExpire: string | null
  isVip: boolean
  isBanned: boolean
  lastLoginTime: string
  lastLoginIp: string
}

export interface UserListParams {
  page: number
  pageSize: number
  username?: string
  email?: string
}

export interface PageResult<T> {
  list: T[]
  total: number
}

export function getUserList(params: UserListParams) {
  return get<PageResult<AdminUser>>('/admin/users', params)
}

export function getUserDetail(id: number) {
  return get<AdminUser>(`/admin/users/${id}`)
}

export function banUser(id: number) {
  return post<string>(`/admin/users/${id}/ban`)
}

export function unbanUser(id: number) {
  return post<string>(`/admin/users/${id}/unban`)
}

// ==================== 应用管理 ====================

export interface AdminSoft {
  id: number
  softName: string
  appKey: string
  packageName: string
  userId: number
  username: string
  createTime: string
  totalUser: number
  todayUser: number
  status: number
}

export interface SoftListParams {
  page: number
  pageSize: number
  softName?: string
  packageName?: string
  username?: string
}

export function getSoftList(params: SoftListParams) {
  return get<PageResult<AdminSoft>>('/admin/softs', params)
}

export function getSoftDetail(id: number) {
  return get<AdminSoft>(`/admin/softs/${id}`)
}

export function deleteSoft(id: number) {
  return del<string>(`/admin/softs/${id}`)
}

// ==================== 卡密管理 ====================

export interface AdminCard {
  id: number
  card: string
  type: number
  value: number
  mark: string
  softId: number
  softName: string
  usable: boolean
  userId: number | null
  username: string | null
  useTime: string | null
  createTime: string
}

export interface CardListParams {
  page: number
  pageSize: number
  card?: string
  type?: number
  usable?: boolean
  softId?: number
}

export interface GenerateCardParams {
  count: number
  type: number
  value: number
  mark: string
  softId: number
}

export interface CardStats {
  total: number
  used: number
  unused: number
  todayGenerated: number
  todayUsed: number
}

export function getCardList(params: CardListParams) {
  return get<PageResult<AdminCard>>('/admin/cards', params)
}

export function generateCards(data: GenerateCardParams) {
  return post<AdminCard[]>('/admin/cards/generate', data)
}

export function getCardStats() {
  return get<CardStats>('/admin/cards/stats')
}

export function deleteCard(ids: number[]) {
  return del<string>('/admin/cards', { data: { ids } })
}

export function exportCards(params: CardListParams) {
  return get<Blob>('/admin/cards/export', params, { responseType: 'blob' })
}

// ==================== 任务管理 ====================

export interface AdminTask {
  uuid: string
  userId: number
  username: string
  softId: number
  softName: string
  status: string
  progress: number
  createTime: string
  updateTime: string
  result: string | null
}

export interface TaskListParams {
  page: number
  pageSize: number
  status?: string
  username?: string
  uuid?: string
}

export function getTaskList(params: TaskListParams) {
  return get<PageResult<AdminTask>>('/admin/tasks', params)
}

export function getTaskDetail(uuid: string) {
  return get<AdminTask>(`/admin/tasks/${uuid}`)
}

export function stopTask(uuid: string) {
  return post<string>('/admin/tasks/stop', { uuid })
}

// ==================== 系统配置 ====================

export interface SystemConfig {
  siteName: string
  siteUrl: string
  adminEmail: string
  uploadType: 'qiniu' | 'local'
  qiniuAccessKey: string
  qiniuSecretKey: string
  qiniuBucket: string
  qiniuDomain: string
  localUploadPath: string
  vipLevels: VipLevel[]
}

export interface VipLevel {
  id: number
  name: string
  days: number
  price: number
  description: string
}

export function getSystemConfig() {
  return get<SystemConfig>('/admin/config')
}

export function saveSystemConfig(data: SystemConfig) {
  return post<string>('/admin/config', data)
}

// ==================== 公告管理 ====================

export interface AdminNotice {
  id: number
  title: string
  content: string
  time: string
}

export interface NoticeListParams {
  page: number
  pageSize: number
}

export function getNoticeList(params: NoticeListParams) {
  return get<PageResult<AdminNotice>>('/admin/notices', params)
}

export function createNotice(data: { title: string; content: string }) {
  return post<string>('/admin/notices', data)
}

export function updateNotice(id: number, data: { title: string; content: string }) {
  return put<string>(`/admin/notices/${id}`, data)
}

export function deleteNotice(id: number) {
  return del<string>(`/admin/notices/${id}`)
}

// ==================== 版本管理 ====================

export interface AdminVersion {
  id: number
  versionCode: number
  versionName: string
  versionMsg: string
  downloadUrl: string
  time: string
  isForce: boolean
  status: number
}

export interface VersionListParams {
  page: number
  pageSize: number
}

export function getVersionList(params: VersionListParams) {
  return get<PageResult<AdminVersion>>('/admin/versions', params)
}

export function createVersion(data: Partial<AdminVersion>) {
  return post<string>('/admin/versions', data)
}

export function updateVersion(id: number, data: Partial<AdminVersion>) {
  return put<string>(`/admin/versions/${id}`, data)
}

export function deleteVersion(id: number) {
  return del<string>(`/admin/versions/${id}`)
}
