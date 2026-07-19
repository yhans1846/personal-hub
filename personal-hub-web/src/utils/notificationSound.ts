/**
 * 通知音效：内置 src/assets/sounds/*.wav（Vite 打包）
 */
import type { NotificationConfig } from '@/types/layout'
import soundDefault from '@/assets/sounds/default.wav?url'
import soundBell from '@/assets/sounds/bell.wav?url'
import soundChime from '@/assets/sounds/chime.wav?url'
import soundPop from '@/assets/sounds/pop.wav?url'

const SOUND_FILES: Record<string, string> = {
  default: soundDefault,
  bell: soundBell,
  chime: soundChime,
  pop: soundPop,
}

let currentAudio: HTMLAudioElement | null = null

function resolveSoundUrl(soundName?: string): string {
  const key = soundName && SOUND_FILES[soundName] ? soundName : 'default'
  return SOUND_FILES[key]
}

/** 播放指定音效（打断上一段；可重复点同一项） */
export function playNotificationSound(soundName?: string): void {
  try {
    if (currentAudio) {
      currentAudio.pause()
      currentAudio = null
    }
    const audio = new Audio(resolveSoundUrl(soundName))
    audio.volume = 0.7
    currentAudio = audio
    void audio.play()
  } catch {
    /* ignore */
  }
}

/** 当前是否处于免打扰时段（支持跨午夜） */
function isInDoNotDisturb(config: Pick<NotificationConfig, 'doNotDisturb' | 'dndStart' | 'dndEnd'>): boolean {
  if (!config.doNotDisturb) return false
  const now = new Date()
  const cur = now.getHours() * 60 + now.getMinutes()
  const [sh, sm] = (config.dndStart || '22:00').split(':').map(Number)
  const [eh, em] = (config.dndEnd || '08:00').split(':').map(Number)
  const start = sh * 60 + (sm || 0)
  const end = eh * 60 + (em || 0)
  if (start === end) return true
  if (start < end) return cur >= start && cur < end
  return cur >= start || cur < end
}

/** 按用户偏好播放（关闭音效或免打扰时不播） */
export function playNotificationSoundIfAllowed(config: NotificationConfig): void {
  if (!config.soundEnabled) return
  if (isInDoNotDisturb(config)) return
  playNotificationSound(config.soundName)
}
