import request from '@/api/request'
import type { Result } from '@/types/common'
import type { ProfileVO, ProfileUpdateDTO } from '@/types/user'

/** 登录 */
export function loginApi(username: string, password: string) {
  return request.post<Result<{ token: string; user: any }>>('/auth/login', {
    username,
    password
  })
}

/** 获取个人资料 */
export function getProfile() {
  return request.get<Result<ProfileVO>>('/user/profile')
}

/** 更新个人资料 */
export function updateProfile(data: ProfileUpdateDTO) {
  return request.put<Result<ProfileVO>>('/user/profile', data)
}

/** 上传头像 */
export function uploadAvatar(file: File) {
  const form = new FormData()
  form.append('file', file)
  return request.post<Result<{ url: string }>>('/user/avatar', form, {
    headers: { 'Content-Type': undefined }
  })
}
