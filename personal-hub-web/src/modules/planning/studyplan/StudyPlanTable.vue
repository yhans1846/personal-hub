<script setup lang="ts">
import { Link2, MoreHorizontal } from 'lucide-vue-next'
import type { StudyPlanVO } from '@/types/studyplan'
import { formatDate, formatUpdated } from '@/utils/formatTime'

defineProps<{
  list: StudyPlanVO[]
  pageSize: number
  statusMeta: (status: number) => { color: string; label: string; done: boolean }
  softTagStyle: (color?: string) => Record<string, string>
  sourceDisplay: (source: string | null) => { icon: string; text: string } | null
  onRowAction: (cmd: string, plan: StudyPlanVO) => void
  openEdit: (id: number) => void
}>()
</script>

<template>
  <div class="product-table">
    <div class="pt-head">
      <div class="col-name">名称</div>
      <div class="col-cat">分类</div>
      <div class="col-status">状态</div>
      <div class="col-progress">进度</div>
      <div class="col-date">开始</div>
      <div class="col-date">结束</div>
      <div class="col-source">来源</div>
      <div class="col-author">作者</div>
      <div class="col-updated">更新</div>
      <div class="col-remark">备注</div>
      <div class="col-actions" />
    </div>
    <!-- 固定 10 行槽位，均分剩余高度，不满 10 条留空行位 -->
    <div class="pt-body">
      <div
        v-for="plan in list"
        :key="plan.id"
        class="pt-row"
        @click="openEdit(plan.id)"
      >
        <div class="col-name">
          <div class="name-title">{{ plan.name }}</div>
        </div>
        <div class="col-cat">
          <template v-if="plan.tags?.length">
            <span
              v-for="t in plan.tags"
              :key="t.id"
              class="soft-tag"
              :style="softTagStyle(t.color)"
            >{{ t.name }}</span>
          </template>
          <span v-else class="muted">—</span>
        </div>
        <div class="col-status">
          <span class="status-dot-label">
            <template v-if="statusMeta(plan.status).done">✅</template>
            <i v-else class="status-dot" :style="{ background: statusMeta(plan.status).color }" />
            {{ statusMeta(plan.status).label }}
          </span>
        </div>
        <div class="col-progress">
          <el-progress
            :percentage="plan.progress ?? 0"
            :stroke-width="6"
            :color="(plan.progress ?? 0) >= 100 ? '#67c23a' : 'var(--accent)'"
          />
        </div>
        <div class="col-date cell-date">{{ formatDate(plan.startDate, '—') }}</div>
        <div class="col-date cell-date">{{ formatDate(plan.endDate, '—') }}</div>
        <div class="col-source">
          <span v-if="sourceDisplay(plan.source)" class="source-chip">
            <span>{{ sourceDisplay(plan.source)!.icon }}</span>
            {{ sourceDisplay(plan.source)!.text }}
          </span>
          <span v-else class="muted">—</span>
        </div>
        <div class="col-author cell-text">{{ plan.author || '—' }}</div>
        <div class="col-updated cell-date">{{ formatUpdated(plan.updatedAt) }}</div>
        <div class="col-remark">
          <el-tooltip v-if="plan.remark" :content="plan.remark" placement="top" :show-after="300">
            <span class="remark-text">{{ plan.remark }}</span>
          </el-tooltip>
          <span v-else class="muted">—</span>
        </div>
        <div class="col-actions" @click.stop>
          <el-tooltip v-if="plan.url" content="打开课程" placement="top">
            <a :href="plan.url" class="icon-action" target="_blank" rel="noopener noreferrer" @click.stop>
              <Link2 :size="15" />
            </a>
          </el-tooltip>
          <el-dropdown trigger="click" @command="(cmd: string) => onRowAction(cmd, plan)">
            <button type="button" class="icon-action more-btn" @click.stop>
              <MoreHorizontal :size="16" />
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="edit">编辑</el-dropdown-item>
                <el-dropdown-item command="copy">复制</el-dropdown-item>
                <el-dropdown-item command="archive">归档</el-dropdown-item>
                <el-dropdown-item command="delete" divided>删除</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
      <div
        v-for="n in Math.max(0, pageSize - list.length)"
        :key="'pad-' + n"
        class="pt-row pt-row--pad"
        aria-hidden="true"
      />
    </div>
  </div>
</template>

<style scoped>
/* 列宽 + 10 行均分（共享 .product-table 壳见 styles/product-list.css） */
.pt-head, .pt-row {
  /* 名称 | 分类 | 状态 | 进度 | 开始 | 结束 | 来源 | 作者 | 更新 | 备注 | 操作 */
  grid-template-columns:
    minmax(148px, 1.8fr)
    92px
    72px
    128px
    102px
    102px
    104px
    96px
    136px
    minmax(120px, 1.4fr)
    72px;
}
.pt-body {
  grid-template-rows: repeat(10, minmax(0, 1fr));
}

.soft-tag {
  display: inline-flex;
  align-items: center;
  padding: 1px 7px;
  border-radius: var(--radius-sm);
  font-size: 12px;
  border: 1px solid transparent;
  margin-right: 4px;
}

.remark-text {
  display: block;
  font-size: 13px;
  color: var(--text-secondary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100%;
}

.source-chip {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--text-secondary);
}

.col-actions {
  gap: 2px;
}
.icon-action {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border: none;
  border-radius: var(--radius-sm);
  background: transparent;
  color: var(--text-tertiary);
  cursor: pointer;
  text-decoration: none;
}
.icon-action:hover {
  background: var(--bg-card);
  color: var(--text-primary);
}
</style>
