import request from '@/api/request'

/**
 * 通过 Axios（带 Authorization）获取文件预览 Blob URL
 * 用于 <img> 标签展示需认证的文件
 */
export async function getFilePreviewUrl(fileId: number): Promise<string> {
  const res = await request.get(`/files/${fileId}/preview`, {
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
