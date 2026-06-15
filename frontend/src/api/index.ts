import { get, post } from '@/utils/request'

// 示例：用户登录
export function login(data: { username: string; password: string }) {
  return post<{ token: string }>('/auth/login', data)
}

// 示例：获取用户信息
export function getUserInfo() {
  return get<{ id: number; username: string; nickname: string }>('/user/info')
}

// 示例：获取列表数据
export function getList<T>(params?: object) {
  return get<T>('/list', params)
}
