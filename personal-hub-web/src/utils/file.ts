import request from '@/api/request'

/**
 * 通过 Axios（带 Authorization）获取文件预览 Blob URL
 * 用于 <img> / <iframe> 展示需认证的文件
 */
export async function getFilePreviewUrl(fileId: number): Promise<string> {
  const res = await request.get(`/files/${fileId}/preview`, {
    responseType: 'blob',
  })
  return URL.createObjectURL(res.data)
}

/** 日记配图预览 Blob URL（鉴权） */
export async function getDiaryImagePreviewUrl(diaryId: number, filename: string): Promise<string> {
  const res = await request.get(`/diaries/${diaryId}/images/${encodeURIComponent(filename)}`, {
    responseType: 'blob',
  })
  return URL.createObjectURL(res.data)
}

/** 释放 Blob URL */
export function revokePreviewUrl(url: string) {
  if (url?.startsWith('blob:')) {
    URL.revokeObjectURL(url)
  }
}

/** 带鉴权下载文件到本地 */
export async function downloadFileBlob(fileId: number, fileName: string) {
  const res = await request.get(`/files/${fileId}/download`, {
    responseType: 'blob',
  })
  const url = URL.createObjectURL(res.data)
  const a = document.createElement('a')
  a.href = url
  a.download = fileName || `file-${fileId}`
  document.body.appendChild(a)
  a.click()
  a.remove()
  URL.revokeObjectURL(url)
}

/** 拉取预览文本（txt / md） */
export async function getFilePreviewText(fileId: number): Promise<string> {
  const res = await request.get(`/files/${fileId}/preview`, {
    responseType: 'blob',
  })
  return await (res.data as Blob).text()
}

export type FilePreviewKind = 'image' | 'pdf' | 'markdown' | 'text' | 'unsupported'

const IMAGE_EXT = new Set(['jpg', 'jpeg', 'png', 'gif', 'svg', 'webp', 'bmp'])

/** 根据扩展名判断预览类型 */
export function getFilePreviewKind(extOrType: string | null | undefined): FilePreviewKind {
  const ext = (extOrType || '').toLowerCase().replace(/^\./, '')
  if (IMAGE_EXT.has(ext)) return 'image'
  if (ext === 'pdf') return 'pdf'
  if (ext === 'md' || ext === 'markdown') return 'markdown'
  if (ext === 'txt' || ext === 'log' || ext === 'csv' || ext === 'json' || ext === 'xml') return 'text'
  return 'unsupported'
}
