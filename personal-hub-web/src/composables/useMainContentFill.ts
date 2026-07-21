import { onMounted, onUnmounted } from 'vue'

const FILL = 'main-content--fill'
const EDITOR = 'main-content--editor'

/** 挂载时给主内容区加 fill，卸载时移除（与学习计划一致） */
export function useMainContentFill() {
  onMounted(() => {
    document.querySelector('.main-content')?.classList.add(FILL)
  })
  onUnmounted(() => {
    document.querySelector('.main-content')?.classList.remove(FILL)
  })
}

/**
 * 笔记编辑页：取消内容区 80% 限宽与主区大 padding，
 * 与原「专注模式」可视宽度一致（侧栏仍保留）。
 */
export function useMainContentEditor() {
  onMounted(() => {
    document.querySelector('.main-content')?.classList.add(EDITOR)
  })
  onUnmounted(() => {
    document.querySelector('.main-content')?.classList.remove(EDITOR)
  })
}
