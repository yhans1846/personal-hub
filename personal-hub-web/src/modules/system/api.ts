import request from '@/api/request'
import type { Result } from '@/types/common'
import type { ProfileVO, ProfileUpdateDTO, AuthUser } from '@/types/user'

/** 登录 */
export function loginApi(
  username: string,
  password: string,
  captchaId: string,
  sliderX: number,
) {
  return request.post<Result<{ token: string; user: AuthUser }>>('/auth/login', {
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

// ====== 数据管理（备份 / 恢复）======

/** 立即备份（落盘历史 + ZIP 流下载） */
export function backupNow() {
  return request.post('/backup/now', null, {
    responseType: 'blob',
    timeout: 120000,
  })
}

/** 导入备份 ZIP（全量覆盖） */
export function importBackup(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<Result<void>>('/backup/import', formData, {
    timeout: 120000,
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

export type BackupFrequency = 'off' | 'daily' | 'weekly'

export interface BackupSettings {
  frequency: BackupFrequency
}

export interface UserBackupItem {
  id: number
  fileSize: number
  triggerType: 'MANUAL' | 'AUTO' | string
  status: 'OK' | 'FAILED' | string
  errorMessage?: string | null
  createdAt: string
}

export function getBackupList() {
  return request.get<Result<UserBackupItem[]>>('/backup/list')
}

export function getBackupSettings() {
  return request.get<Result<BackupSettings>>('/backup/settings')
}

export function updateBackupSettings(data: BackupSettings) {
  return request.put<Result<void>>('/backup/settings', data)
}

export function downloadBackup(id: number) {
  return request.get(`/backup/${id}/download`, {
    responseType: 'blob',
    timeout: 120000,
  })
}

export function restoreBackup(id: number) {
  return request.post<Result<void>>(`/backup/${id}/restore`, null, { timeout: 120000 })
}

export function deleteBackup(id: number) {
  return request.delete<Result<void>>(`/backup/${id}`)
}

export interface ImageCaptcha {
  captchaId: string
  imageBase64: string
}

export function getImageCaptcha() {
  return request.get<Result<ImageCaptcha>>('/security/image-captcha')
}

export interface DataPurgeResult {
  backupId: number
  fileSize: number
  createdAt: string
}

export function purgeAllData(data: { captchaId: string; captchaCode: string }) {
  return request.post<Result<DataPurgeResult>>('/data/purge', data, { timeout: 180000 })
}

