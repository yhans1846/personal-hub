import { onMounted, onUnmounted } from 'vue'

const FILL = 'main-content--fill'

/** 挂载时给主内容区加 fill，卸载时移除（与学习计划一致） */
export function useMainContentFill() {
  onMounted(() => {
    document.querySelector('.main-content')?.classList.add(FILL)
  })
  onUnmounted(() => {
    document.querySelector('.main-content')?.classList.remove(FILL)
  })
}
