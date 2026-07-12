import { ref, type Ref } from 'vue'
import { uploadNoteImage } from '@/modules/knowledge/api'
import { ElMessage } from 'element-plus'

/**
 * 图片上传：校验 → 确保 noteId → 逐个上传 → 回调插入编辑器
 */
export function useImageUpload(
  noteIdRef: Ref<number | null>,
  ensureNoteSaved: () => Promise<void>,
) {
  const uploading = ref(false)

  async function handleUpload(files: File[], callback: (urls: string[]) => void): Promise<void> {
    // 校验文件类型和大小
    const validFiles = files.filter(f => {
      if (!f.type.startsWith('image/')) {
        ElMessage.warning(`"${f.name}" 不是图片，已跳过`)
        return false
      }
      if (f.size > 10 * 1024 * 1024) {
        ElMessage.warning(`"${f.name}" 超过 10MB，已跳过`)
        return false
      }
      return true
    })
    if (validFiles.length === 0) return

    uploading.value = true
    try {
      // 新建笔记第一次上传前先自动保存获取 noteId
      if (!noteIdRef.value) {
        await ensureNoteSaved()
      }
      const noteId = noteIdRef.value
      if (!noteId) {
        ElMessage.error('笔记创建失败，无法上传图片')
        return
      }

      const urls: string[] = []
      for (const file of validFiles) {
        try {
          const res = await uploadNoteImage(noteId, file)
          const result = res.data.data
          const url = Object.values(result)[0]
          if (url) urls.push(url)
        } catch {
          ElMessage.error(`"${file.name}" 上传失败`)
        }
      }

      if (urls.length > 0) {
        callback(urls)
      }
    } finally {
      uploading.value = false
    }
  }

  return { uploading, handleUpload }
}
