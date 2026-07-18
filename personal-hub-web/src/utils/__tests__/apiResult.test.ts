import { describe, expect, it, vi, afterEach } from 'vitest'
import type { AxiosResponse } from 'axios'
import type { Result, PageResult } from '@/types/common'
import { ElMessage } from 'element-plus'
import {
  getApiErrorMessage,
  handleApiError,
  unwrapPage,
  unwrapResult,
} from '../apiResult'

function axiosResult<T>(body: Result<T>): AxiosResponse<Result<T>> {
  return { data: body, status: 200, statusText: 'OK', headers: {}, config: {} as never }
}

describe('unwrapResult', () => {
  it('returns data when code is 200', async () => {
    const data = await unwrapResult(Promise.resolve(axiosResult({ code: 200, message: 'ok', data: { id: 1 } })))
    expect(data).toEqual({ id: 1 })
  })

  it('throws when business code is not success', async () => {
    await expect(
      unwrapResult(Promise.resolve(axiosResult({ code: 400, message: '参数错误', data: null }))),
    ).rejects.toThrow('参数错误')
  })
})

describe('unwrapPage', () => {
  it('returns page payload', async () => {
    const page: PageResult<{ id: number }> = { records: [{ id: 1 }], total: 1, page: 1, size: 10 }
    const data = await unwrapPage(Promise.resolve(axiosResult({ code: 200, message: 'ok', data: page })))
    expect(data.total).toBe(1)
    expect(data.records).toHaveLength(1)
  })
})

describe('getApiErrorMessage', () => {
  it('reads axios response message', () => {
    expect(getApiErrorMessage({ response: { data: { message: '上传失败' } } })).toBe('上传失败')
  })

  it('falls back to Error.message then default', () => {
    expect(getApiErrorMessage(new Error('网络中断'))).toBe('网络中断')
    expect(getApiErrorMessage({})).toBe('操作失败')
    expect(getApiErrorMessage({}, '自定义失败')).toBe('自定义失败')
  })
})

describe('handleApiError', () => {
  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('shows ElMessage.error with resolved message', () => {
    const errorSpy = vi.spyOn(ElMessage, 'error').mockImplementation(() => undefined as never)
    handleApiError({ response: { data: { message: '无权访问' } } }, '失败')
    expect(errorSpy).toHaveBeenCalledWith('无权访问')
  })
})
