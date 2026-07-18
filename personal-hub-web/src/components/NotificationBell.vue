<script setup lang="ts">
import { onMounted, onUnmounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Bell, Check, CheckCheck, Trash2 } from 'lucide-vue-next'
import { ElBadge, ElSkeleton } from 'element-plus'
import { useNotificationStore } from '@/store/notificationStore'
import { useNotificationConfigStore } from '@/store/notificationConfigStore'
import { checkNotifications } from '@/api/notificationApi'
import { formatRelativeTime } from '@/utils/readingTime'
import { playNotificationSoundIfAllowed } from '@/utils/notificationSound'
import type { NotificationVO } from '@/types/notification'

const router = useRouter()
const store = useNotificationStore()
const configStore = useNotificationConfigStore()
let pollTimer: ReturnType<typeof setInterval>
const primed = ref(false)

onMounted(() => {
  checkNotifications().finally(() => {
    store.fetchUnreadCount().finally(() => {
      primed.value = true
    })
    store.fetchRecent()
  })
  pollTimer = setInterval(() => store.fetchUnreadCount(), 60_000)
})

onUnmounted(() => {
  clearInterval(pollTimer)
})

// 未读增加时播提示音（首次加载不播）
watch(
  () => store.unreadCount,
  (next, prev) => {
    if (!primed.value) return
    if (typeof prev !== 'number') return
    if (next > prev) playNotificationSoundIfAllowed(configStore.config)
  },
)

function handleClearAll() {
  store.clearAll()
}

/** 打开面板时只拉列表，不再触发 check 以免闪现旧通知 */
function handleShow() {
  store.fetchRecent()
}

function handleItemClick(n: NotificationVO) {
  if (!n.isRead) store.markAsRead([n.id])
  const url = store.getRelatedUrl(n)
  if (url) router.push(url)
}

function handleMarkRead(e: Event, n: NotificationVO) {
  e.stopPropagation()
  if (!n.isRead) store.markAsRead([n.id])
}

function handleMarkAllRead() {
  store.markAllAsRead()
}

function getTypeIcon(type: string): string {
  const icons: Record<string, string> = {
    TODO_OVERDUE: '⚠️',
    PLAN_DEADLINE: '📋',
    PLAN_COMPLETED: '🎉',
  }
  return icons[type] || '🔔'
}
</script>

<template>
  <el-popover
    trigger="click"
    placement="bottom-end"
    :width="360"
    :hide-after="0"
    @show="handleShow"
  >
    <template #reference>
      <el-badge :value="store.unreadCount" :hidden="store.unreadCount === 0" class="notif-badge">
        <button class="notif-btn" title="通知">
          <Bell :size="18" />
        </button>
      </el-badge>
    </template>

    <div class="notif-panel">
      <div class="notif-header">
        <span class="notif-title">通知</span>
        <div class="notif-header-actions">
          <button v-if="store.unreadCount > 0" class="notif-header-btn" @click="handleMarkAllRead">
            <CheckCheck :size="14" />
            全部已读
          </button>
          <button v-if="store.notifications.length > 0" class="notif-header-btn notif-header-btn--danger" @click="handleClearAll">
            <Trash2 :size="14" />
            清空
          </button>
        </div>
      </div>

      <div v-if="store.loading" class="notif-loading">
        <el-skeleton :rows="3" animated />
      </div>

      <div v-else-if="store.notifications.length === 0" class="notif-empty">
        <Bell :size="32" class="notif-empty-icon" />
        <span>暂无通知</span>
      </div>

      <div v-else class="notif-list">
        <div
          v-for="n in store.notifications"
          :key="n.id"
          class="notif-item"
          :class="{ unread: !n.isRead }"
          @click="handleItemClick(n)"
        >
          <span class="notif-item-icon">{{ getTypeIcon(n.type) }}</span>
          <div class="notif-item-content">
            <div class="notif-item-title">{{ n.title }}</div>
            <div v-if="n.content" class="notif-item-desc">{{ n.content }}</div>
            <div class="notif-item-time">{{ formatRelativeTime(n.createdAt) }}</div>
          </div>
          <button
            v-if="!n.isRead"
            class="notif-item-check"
            title="标记已读"
            @click="handleMarkRead($event, n)"
          >
            <Check :size="14" />
          </button>
        </div>
      </div>
    </div>
  </el-popover>
</template>

<style scoped>
.notif-badge { display: flex; align-items: center; }
.notif-badge :deep(.el-badge__content) { background: var(--danger, #f56c6c); }

.notif-btn {
  width: 32px; height: 32px;
  display: flex; align-items: center; justify-content: center;
  border-radius: var(--radius-sm); border: none;
  background: transparent; color: var(--text-secondary);
  cursor: pointer; transition: all var(--transition);
}
.notif-btn:hover { background: var(--bg-hover, #f5f5f5); color: var(--text-primary); }

.notif-panel { max-height: 420px; display: flex; flex-direction: column; }

.notif-header {
  display: flex; align-items: center; justify-content: space-between;
  padding: 0 0 8px 0; border-bottom: 1px solid var(--border-light, #eee);
}
.notif-title { font-size: 14px; font-weight: 600; }
.notif-header-actions { display: flex; gap: 4px; }
.notif-header-btn {
  display: flex; align-items: center; gap: 3px;
  font-size: 12px; color: var(--accent); background: none; border: none;
  cursor: pointer; padding: 2px 8px; border-radius: var(--radius-sm);
  white-space: nowrap;
}
.notif-header-btn:hover { background: var(--bg-hover, #f5f5f5); }
.notif-header-btn--danger { color: var(--danger, #f56c6c); }

.notif-loading { padding: 16px 0; }

.notif-empty {
  display: flex; flex-direction: column; align-items: center;
  gap: 8px; padding: 32px 0; color: var(--text-tertiary, #999);
}
.notif-empty-icon { opacity: 0.4; }

.notif-list { overflow-y: auto; max-height: 340px; margin: 0 -12px; }
.notif-item {
  display: flex; align-items: flex-start; gap: 10px;
  padding: 10px 12px; cursor: pointer; border-radius: var(--radius-sm);
  transition: background var(--transition-duration);
}
.notif-item:hover { background: var(--bg-hover, #f5f5f5); }
.notif-item.unread { background: var(--accent-light, #eef2ff); }
.notif-item.unread:hover { background: var(--accent-border, #dde4ff); }

.notif-item-icon { font-size: 16px; line-height: 20px; flex-shrink: 0; }

.notif-item-content { flex: 1; min-width: 0; }
.notif-item-title { font-size: 13px; font-weight: 500; line-height: 1.4; }
.notif-item-desc { font-size: 12px; color: var(--text-tertiary, #999); margin-top: 2px; }
.notif-item-time { font-size: 11px; color: var(--text-placeholder, #bbb); margin-top: 4px; }

.notif-item-check {
  flex-shrink: 0; width: 22px; height: 22px; display: flex;
  align-items: center; justify-content: center;
  border-radius: 50%; border: 1px solid var(--border-color, #ddd);
  background: transparent; color: var(--text-tertiary, #999);
  cursor: pointer; transition: all var(--transition); margin-top: 1px;
}
.notif-item-check:hover {
  background: var(--accent); color: #fff; border-color: var(--accent);
}
</style>
