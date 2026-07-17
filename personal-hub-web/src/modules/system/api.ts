import request from '@/api/request'
import type { Result } from '@/types/common'
import type { ProfileVO, ProfileUpdateDTO } from '@/types/user'

/** 登录 */
export function loginApi(
  username: string,
  password: string,
  captchaId: string,
  sliderX: number,
) {
  return request.post<Result<{ token: string; user: any }>>('/auth/login', {
    username,
    password,
    captchaId,
    sliderX,
  })
}

/** 书架归位验证码 */
export function getCaptcha() {
  return request.get<Result<{
    captchaId: string
    slotCount: number
    emptyIndex: number
    shelfBooks: string[]
    dragBook: string
  }>>('/auth/captcha')
}

/** 验证预检（不消费）；sliderX = 选中的槽位下标 */
export function checkCaptcha(captchaId: string, sliderX: number) {
  return request.post<Result<{ matched: boolean }>>('/auth/captcha/check', {
    captchaId,
    sliderX,
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
