import request from '@/api/request'
import type { Result } from '@/types/common'

/** 登录 */
export function loginApi(username: string, password: string) {
  return request.post<Result<{ token: string; user: any }>>('/auth/login', {
    username,
    password
  })
}
