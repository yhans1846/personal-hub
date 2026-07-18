<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Bell, BellOff, Volume2, RotateCcw, Moon } from 'lucide-vue-next'
import { useNotificationConfigStore } from '@/store/notificationConfigStore'
import { storeToRefs } from 'pinia'
import { playNotificationSound } from '@/utils/notificationSound'

const store = useNotificationConfigStore()
const { config } = storeToRefs(store)
// 常量数组不是 state，不能走 storeToRefs
const NOTIFICATION_TYPES = store.NOTIFICATION_TYPES
const SOUND_OPTIONS = store.SOUND_OPTIONS

const requesting = ref(false)

function handleSoundToggle() {
  const next = !config.value.soundEnabled
  store.updateConfig({ soundEnabled: next })
  if (next) playNotificationSound(config.value.soundName)
}

function handleSoundSelect(soundName: string) {
  // 先播（保留用户点击手势），再持久化；同一项再点也会再响
  playNotificationSound(soundName)
  if (config.value.soundName !== soundName) {
    store.updateConfig({ soundName })
  }
}

async function handleDesktopToggle() {
  if (!('Notification' in window)) {
    ElMessage.warning('当前浏览器不支持桌面通知')
    return
  }

  // storeToRefs 得到的是 Ref，脚本内须用 .value
  if (config.value.desktopEnabled) {
    store.updateConfig({ desktopEnabled: false })
    ElMessage.success('已关闭桌面通知')
    return
  }

  // 开启 → 请求权限
  requesting.value = true
  const granted = await store.requestDesktopPermission()
  requesting.value = false

  if (granted) {
    ElMessage.success('已开启桌面通知')
    // 发送测试通知
    try {
      const n = new Notification('Personal Hub', {
        body: '桌面通知已开启',
        icon: '/favicon.ico',
      })
      setTimeout(() => n.close(), 3000)
    } catch { /* ignore */ }
  } else {
    ElMessage.warning('桌面通知权限被拒绝，请在浏览器设置中允许')
    store.updateConfig({ desktopEnabled: false })
  }
}

async function handleReset() {
  await store.resetConfig()
  ElMessage.success('通知设置已恢复默认')
}
</script>

<template>
  <div class="notification-settings">
    <!-- 桌面通知 -->
    <section class="setting-section">
      <div class="section-title-row">
        <h3 class="section-title">桌面通知</h3>
        <button class="reset-link" @click="handleReset">
          <RotateCcw :size="12" />
          恢复默认
        </button>
      </div>
      <div class="setting-row">
        <div class="setting-row-info">
          <span class="setting-row-label">启用桌面通知</span>
          <span class="setting-row-desc">浏览器外收到推送提醒</span>
        </div>
        <button
          :class="['toggle-btn', { active: config.desktopEnabled }]"
          :disabled="requesting"
          @click="handleDesktopToggle"
        >
          <Bell v-if="config.desktopEnabled" :size="16" />
          <BellOff v-else :size="16" />
          {{ config.desktopEnabled ? '已开启' : '已关闭' }}
        </button>
      </div>
    </section>

    <!-- 通知类型 -->
    <section class="setting-section">
      <h3 class="section-title">通知类型</h3>
      <div class="chip-group">
        <button
          v-for="nt in NOTIFICATION_TYPES"
          :key="nt.value"
          :class="['chip', { active: config.enabledTypes.includes(nt.value) }]"
          @click="store.toggleNotificationType(nt.value)"
        >
          {{ nt.label }}
        </button>
      </div>
    </section>

    <!-- 声音 -->
    <section class="setting-section">
      <h3 class="section-title">声音</h3>
      <div class="setting-row">
        <div class="setting-row-info">
          <span class="setting-row-label">通知音效</span>
        </div>
        <button
          :class="['toggle-btn', { active: config.soundEnabled }]"
          @click="handleSoundToggle"
        >
          <Volume2 :size="16" />
          {{ config.soundEnabled ? '已开启' : '已关闭' }}
        </button>
      </div>
      <div v-if="config.soundEnabled" class="sound-picker">
        <span class="setting-row-label-sm">提示音</span>
        <div class="inline-options">
          <button
            v-for="s in SOUND_OPTIONS"
            :key="s.value"
            type="button"
            :class="['inline-btn', { active: config.soundName === s.value }]"
            @click="handleSoundSelect(s.value)"
          >{{ s.label }}</button>
        </div>
      </div>
    </section>

    <!-- 免打扰 -->
    <section class="setting-section">
      <h3 class="section-title">免打扰</h3>
      <div class="setting-row">
        <div class="setting-row-info">
          <span class="setting-row-label">免打扰模式</span>
          <span class="setting-row-desc">开启后在设定时间段内不推送通知</span>
        </div>
        <button
          :class="['toggle-btn', { active: config.doNotDisturb }]"
          @click="store.updateConfig({ doNotDisturb: !config.doNotDisturb })"
        >
          <Moon :size="16" />
          {{ config.doNotDisturb ? '已开启' : '已关闭' }}
        </button>
      </div>
      <div v-if="config.doNotDisturb" class="dnd-times">
        <div class="time-field">
          <span class="time-label">开始</span>
          <input
            type="time"
            :value="config.dndStart"
            class="time-input"
            @change="store.updateConfig({ dndStart: ($event.target as HTMLInputElement).value })"
          />
        </div>
        <span class="time-sep">至</span>
        <div class="time-field">
          <span class="time-label">结束</span>
          <input
            type="time"
            :value="config.dndEnd"
            class="time-input"
            @change="store.updateConfig({ dndEnd: ($event.target as HTMLInputElement).value })"
          />
        </div>
      </div>
    </section>

  </div>
</template>

<style scoped>
/* 间距对齐外观/阅读：区块 20 · 标题下 10 · 行间 12 · 末项清零 */
.setting-section { margin-bottom: 20px; }
.setting-section:last-child { margin-bottom: 0; }

.section-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}

.section-title {
  margin: 0 0 10px;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
}
.section-title-row .section-title { margin-bottom: 0; }

.reset-link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  color: var(--text-tertiary);
  background: none;
  border: none;
  cursor: pointer;
  padding: 2px 6px;
  border-radius: var(--radius-sm);
  transition: all var(--transition);
}
.reset-link:hover {
  color: var(--accent);
  background: var(--accent-light);
}

/* ─── 开关行 ─── */
.setting-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  gap: 16px;
}
.setting-row:last-child { margin-bottom: 0; }
.setting-row-info { display: flex; flex-direction: column; gap: 2px; }
.setting-row-label { font-size: 13px; color: var(--text-primary); }
.setting-row-label-sm { font-size: 13px; color: var(--text-secondary); min-width: 60px; }
.setting-row-desc { font-size: 11px; color: var(--text-tertiary); }

.sound-picker {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.inline-options { display: flex; gap: 6px; flex-wrap: wrap; }
.inline-btn {
  padding: 6px 14px;
  font-size: 13px;
  border: 1px solid var(--border-color);
  background: var(--bg-card);
  border-radius: var(--radius-sm);
  color: var(--text-secondary);
  cursor: pointer;
  transition: all var(--transition);
}
.inline-btn:hover { border-color: var(--accent); color: var(--accent); }
.inline-btn.active {
  border-color: var(--accent);
  color: var(--accent);
  background: color-mix(in srgb, var(--accent) 8%, transparent);
}

/* ─── 开关按钮 ─── */
.toggle-btn {
  display: flex; align-items: center; gap: 6px;
  padding: 6px 14px;
  font-size: 12px;
  border: 1px solid var(--border-color);
  background: var(--bg-card);
  border-radius: var(--radius-sm);
  color: var(--text-tertiary);
  cursor: pointer;
  transition: all var(--transition);
  white-space: nowrap;
  flex-shrink: 0;
}
.toggle-btn:hover:not(:disabled) { border-color: var(--accent); color: var(--accent); }
.toggle-btn.active {
  border-color: var(--accent);
  color: var(--accent);
  background: color-mix(in srgb, var(--accent) 8%, transparent);
}
.toggle-btn:disabled { opacity: 0.5; cursor: not-allowed; }

/* ─── Chip 组 ─── */
.chip-group { display: flex; gap: 6px; flex-wrap: wrap; }
.chip {
  padding: 6px 14px;
  font-size: 12px;
  border: 1px solid var(--border-color);
  background: var(--bg-card);
  border-radius: var(--radius-sm);
  color: var(--text-secondary);
  cursor: pointer;
  transition: all var(--transition);
}
.chip:hover { border-color: var(--accent); color: var(--accent); }
.chip.active {
  border-color: var(--accent);
  color: var(--accent);
  background: color-mix(in srgb, var(--accent) 10%, transparent);
}

/* ─── 免打扰时间 ─── */
.dnd-times { display: flex; align-items: center; gap: 8px; }
.time-field { display: flex; align-items: center; gap: 6px; }
.time-label { font-size: 12px; color: var(--text-tertiary); }
.time-input {
  padding: 6px 10px;
  font-size: 13px;
  border: 1px solid var(--border-color);
  background: var(--bg-card);
  border-radius: var(--radius-sm);
  color: var(--text-primary);
  outline: none;
}
.time-input:focus { border-color: var(--accent); }
.time-sep { font-size: 12px; color: var(--text-tertiary); }

</style>
