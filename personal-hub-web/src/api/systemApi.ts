import request from './request'
import type { Result } from '@/types/layout'

/** 导出数据 */
export function exportData(params: { modules: string[]; format: 'markdown' | 'json' }) {
  return request.post<Result<{ downloadUrl: string }>>('/export', params)
}

/** 获取导出历史 */
export function getExportHistory() {
  return request.get<Result<any[]>>('/export/history')
}

/** 立即备份 */
export function backupNow() {
  return request.post<Result<{ id: number; downloadUrl: string }>>('/backup/now')
}

/** 获取备份列表 */
export function getBackupList() {
  return request.get<Result<any[]>>('/backup/list')
}

/** 导入备份 */
export function importBackup(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<Result<void>>('/backup/import', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

/** 下载备份文件 */
export function downloadBackup(id: number) {
  return request.get<Blob>(`/backup/download/${id}`, { responseType: 'blob' })
}
