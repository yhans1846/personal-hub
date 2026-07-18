import type { AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import type { PageResult, Result } from '@/types/common'

const SUCCESS_CODE = 200

/** 从 Axios Result 响应中取出 data；业务码非 200 时抛错 */
export async function unwrapResult<T>(
  promise: Promise<AxiosResponse<Result<T>>>,
): Promise<T> {
  const res = await promise
  const body = res.data
  if (body != null && typeof body === 'object' && 'code' in body && body.code !== SUCCESS_CODE) {
    throw new Error(body.message || '请求失败')
  }
  return body.data
}

/** 分页列表快捷解包 */
export async function unwrapPage<T>(
  promise: Promise<AxiosResponse<Result<PageResult<T>>>>,
): Promise<PageResult<T>> {
  return unwrapResult(promise)
}

/** 解析接口/网络错误文案 */
export function getApiErrorMessage(error: unknown, fallback = '操作失败'): string {
  if (error && typeof error === 'object') {
    const ax = error as { response?: { data?: { message?: string } }; message?: string }
    const fromBody = ax.response?.data?.message
    if (fromBody) return fromBody
    if (error instanceof Error && error.message) return error.message
    if (typeof ax.message === 'string' && ax.message) return ax.message
  }
  return fallback
}

/** 统一弹出错误提示 */
export function handleApiError(error: unknown, fallback = '操作失败'): string {
  const msg = getApiErrorMessage(error, fallback)
  ElMessage.error(msg)
  return msg
}
