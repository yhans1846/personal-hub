# Personal Hub CRUD UI 优化实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 将 CRUD UI 规范合并到 STYLE_GUIDE.md，创建统一 Ui 组件库，将全页面表单重构为 Dialog/Drawer

**架构：** 4 阶段增量式推进——① 更新设计文档 → ② 封装 Ui 组件 → ③a 简单实体改 Dialog → ③b 中等实体改 Drawer → ④ 清理。每阶段完成后用户确认再提交

**技术栈：** Vue 3 + TypeScript + Element Plus 2.14 + 现有 CSS 变量体系

**设计文档：** `docs/superpowers/specs/2026-07-11-crud-ui-optimization-design.md`

---

## 文件结构

### 修改/创建的文件清单

| 阶段 | 操作 | 文件 |
|------|------|------|
| 1 | 修改 | `docs/STYLE_GUIDE.md` |
| 2 | 创建 | `personal-hub-web/src/components/ui/index.ts` |
| 2 | 创建 | `personal-hub-web/src/components/ui/UiDialog.vue` |
| 2 | 创建 | `personal-hub-web/src/components/ui/UiInput.vue` |
| 2 | 创建 | `personal-hub-web/src/components/ui/UiTextarea.vue` |
| 2 | 创建 | `personal-hub-web/src/components/ui/UiSelect.vue` |
| 2 | 创建 | `personal-hub-web/src/components/ui/UiDatePicker.vue` |
| 2 | 创建 | `personal-hub-web/src/components/ui/UiButton.vue` |
| 2 | 创建 | `personal-hub-web/src/components/ui/UiSection.vue` |
| 2 | 创建 | `personal-hub-web/src/components/ui/UiCard.vue` |
| 3a | 创建 | `personal-hub-web/src/modules/planning/todo/TodoDialog.vue` |
| 3a | 修改 | `personal-hub-web/src/modules/planning/todo/List.vue` |
| 3a | 创建 | `personal-hub-web/src/modules/resource/bookmark/BookmarkDialog.vue` |
| 3a | 修改 | `personal-hub-web/src/modules/resource/bookmark/List.vue` |
| 3a | 修改 | `personal-hub-web/src/modules/knowledge/note/CategoryManage.vue` |
| 3a | 修改 | `personal-hub-web/src/modules/knowledge/tag/Manage.vue` |
| 3a | 修改 | `personal-hub-web/src/modules/knowledge/note/TagManage.vue` |
| 3a | 修改 | `personal-hub-web/src/modules/resource/bookmark/CategoryManage.vue` |
| 3a | 修改 | `personal-hub-web/src/modules/resource/file/CategoryManage.vue` |
| 3b | 创建 | `personal-hub-web/src/modules/knowledge/reading/ReadingDrawer.vue` |
| 3b | 修改 | `personal-hub-web/src/modules/knowledge/reading/List.vue` |
| 3b | 创建 | `personal-hub-web/src/modules/knowledge/study/StudyDrawer.vue` |
| 3b | 修改 | `personal-hub-web/src/modules/knowledge/study/List.vue` |
| 3b | 创建 | `personal-hub-web/src/modules/planning/studyplan/StudyPlanDrawer.vue` |
| 3b | 修改 | `personal-hub-web/src/modules/planning/studyplan/List.vue` |
| 4 | 删除 | `personal-hub-web/src/modules/planning/todo/Form.vue` |
| 4 | 删除 | `personal-hub-web/src/modules/resource/bookmark/Form.vue` |
| 4 | 删除 | `personal-hub-web/src/modules/knowledge/reading/Form.vue` |
| 4 | 删除 | `personal-hub-web/src/modules/knowledge/study/Form.vue` |
| 4 | 删除 | `personal-hub-web/src/modules/planning/studyplan/Form.vue` |
| 4 | 修改 | `personal-hub-web/src/router/index.ts` |

---

### 阶段 1：更新 STYLE_GUIDE.md

**文件：**
- 修改：`docs/STYLE_GUIDE.md`

- [ ] **步骤 1：在「UI/UX 设计原则」的「核心调性」之后、「整体布局」之前插入 CRUD 交互模式定义**

在 `STYLE_GUIDE.md` 的「核心调性」段落（line ~270）和「整体布局」（line ~272）之间，插入三个交互模式的说明：

```markdown
### CRUD 交互模式

整个系统只保留三种编辑方式：

#### ① Dialog（推荐）

适用于简单实体（字段少、操作快）：Todo、标签、分类、收藏夹、文件分类等。

特点：不跳转页面、保留当前列表状态、操作完成立即关闭。

流程：点击新增 → Dialog 弹出 → 填写 → 保存 → 关闭 → 刷新列表

**禁止：** 列表 → 跳转新页面 → 填写 → 返回列表

#### ② Drawer（侧边抽屉）

适用于中等复杂度实体（需预览上下文）：学习记录、阅读记录、学习计划、文件详情等。

特点：保留列表、支持快速切换、阅读体验更好。类似 Linear Issue / GitHub Issue。

流程：列表 → 打开 Drawer → 编辑 → 关闭

#### ③ Full Page（独立页面）

仅适用于需要专注编辑的场景：Markdown 编辑器、日记编辑、Dashboard 自定义、大型配置页面。

不要使用 Dialog。
```

- [ ] **步骤 2：在 CRUD 交互模式之后插入 Dialog 设计规范**

```markdown
### Dialog 设计规范

所有 Dialog 保持统一。

#### 尺寸
- 默认：520px
- 复杂：600px
- 最大：720px
- 不超过 800px

#### 圆角
统一 16px

#### Padding
统一 24px

#### Header
只保留标题 + 关闭按钮，不要图标/彩色背景/渐变

示例标题：「新建待办」「编辑标签」「新增分类」

#### Footer
永远：取消(Default) + 保存(Primary)，按钮右对齐，不超过两个按钮
```

- [ ] **步骤 3：在 Dialog 规范之后插入表单设计规范**

```markdown
### 表单设计规范

不要使用传统后台表单（Label → Input 连续堆砌）。每个字段是独立内容块。

#### Label
- 字号：14px
- 字重：500
- 颜色：`--text-primary`

#### 输入框
- 高度统一：40px
- Textarea：自动高度或 4~6 行

#### 字段间距
- 字段之间：20px
- Label 与 Input：6px
- Section 之间：24px
- 整个 Dialog padding：24px
```

- [ ] **步骤 4：在表单规范之后插入页面布局规范**

```markdown
### 页面布局规范

页面层级：Page → Card → Section → Field

而不是：Page → Form → 几十个 FormItem

推荐结构示例：
```
Card
    基本信息
        标题
        描述
    时间
        截止日期
    其它
        标签
```
```

- [ ] **步骤 5：在「共享组件」列表之前插入 CRUD 页面映射表**

在 line ~334（按钮规范之后）和 line ~336（共享组件之前）之间插入：

```markdown
### CRUD 页面映射规范

| 功能 | UI 模式 |
|------|---------|
| Todo | Dialog |
| Tag | Dialog |
| Category（笔记/收藏夹/文件） | Dialog |
| Bookmark | Dialog |
| File Category | Dialog |
| Reading Record | Drawer |
| Study Record | Drawer |
| Study Plan | Drawer |
| Markdown 编辑器 | Full Page |
| 日记编辑 | Full Page |

禁止独立 Create/Edit 页面，新增/编辑共用一个 Dialog/Drawer 组件。
```

- [ ] **步骤 6：在映射表之后插入组件封装要求**

```markdown
### 组件封装要求

业务页面禁止直接大量使用 Element Plus 原生组件进行页面布局。统一封装以下组件：

```
UiDialog / UiInput / UiTextarea / UiSelect / UiDatePicker / UiButton / UiSection / UiCard
```

业务页面只负责组合业务，不负责样式。以后修改 UI 只改一处。
```

- [ ] **步骤 7：更新现有「新页面模板」行 (line ~361)**

将 `PageHeader → ListToolbar（#filters 插槽）→ loading skeleton → EmptyState → 内容 → ListPagination`

改为：

```
CRUD 列表页：
  PageHeader → ListToolbar（#filters 插槽）→ loading skeleton → EmptyState → 内容 → ListPagination

CRUD 操作遵循：
  Dialog（简单实体）或 Drawer（中等实体），不跳转独立页面
```

- [ ] **步骤 8：更新 Checklist (lines ~409-416)**

在现有 7 条后面增加两条：

```
8. ✅ Dialog/Drawer 替代独立路由表单页
9. ✅ 新增/编辑共用一个 Dialog/Drawer 组件
```

- [ ] **步骤 9：提交**

```bash
git add docs/STYLE_GUIDE.md
git commit -m "docs: 将 CRUD UI 交互规范合并到 STYLE_GUIDE.md"
```

---

### 阶段 2：封装 Ui 组件库

**文件：**
- 创建：`personal-hub-web/src/components/ui/index.ts`
- 创建：`personal-hub-web/src/components/ui/UiDialog.vue`
- 创建：`personal-hub-web/src/components/ui/UiInput.vue`
- 创建：`personal-hub-web/src/components/ui/UiTextarea.vue`
- 创建：`personal-hub-web/src/components/ui/UiSelect.vue`
- 创建：`personal-hub-web/src/components/ui/UiDatePicker.vue`
- 创建：`personal-hub-web/src/components/ui/UiButton.vue`
- 创建：`personal-hub-web/src/components/ui/UiSection.vue`
- 创建：`personal-hub-web/src/components/ui/UiCard.vue`

- [ ] **步骤 1：创建 UiDialog.vue**

```vue
<script setup lang="ts">
defineOptions({ inheritAttrs: false })

const props = withDefaults(defineProps<{
  modelValue: boolean
  title: string
  width?: string | number
}>(), { width: 520 })

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()
</script>

<template>
  <el-dialog
    :model-value="props.modelValue"
    :width="props.width"
    :title="props.title"
    align-center
    @update:model-value="emit('update:modelValue', $event)"
    v-bind="$attrs"
  >
    <template v-for="(_, slot) in $slots" #[slot]="scope">
      <slot :name="slot" v-bind="scope" />
    </template>
  </el-dialog>
</template>
```

- [ ] **步骤 2：创建 UiInput.vue**

```vue
<script setup lang="ts">
defineOptions({ inheritAttrs: false })

const props = defineProps<{
  modelValue?: string | number
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string | number]
}>()
</script>

<template>
  <el-input
    :model-value="props.modelValue"
    @update:model-value="emit('update:modelValue', $event)"
    v-bind="$attrs"
  />
</template>
```

- [ ] **步骤 3：创建 UiTextarea.vue**

```vue
<script setup lang="ts">
defineOptions({ inheritAttrs: false })

const props = defineProps<{
  modelValue?: string
}>()

const emit = defineEmits<{
  'update:modelValue': [value: string]
}>()
</script>

<template>
  <el-input
    :model-value="props.modelValue"
    type="textarea"
    :autosize="{ minRows: 4, maxRows: 12 }"
    @update:model-value="emit('update:modelValue', $event)"
    v-bind="$attrs"
  />
</template>
```

- [ ] **步骤 4：创建 UiSelect.vue**

```vue
<script setup lang="ts">
defineOptions({ inheritAttrs: false })

const props = defineProps<{
  modelValue?: any
  options?: { value: any; label: string }[]
}>()

const emit = defineEmits<{
  'update:modelValue': [value: any]
}>()
</script>

<template>
  <el-select
    :model-value="props.modelValue"
    @update:model-value="emit('update:modelValue', $event)"
    v-bind="$attrs"
  >
    <el-option
      v-for="opt in options"
      :key="opt.value"
      :value="opt.value"
      :label="opt.label"
    />
    <template v-for="(_, slot) in $slots" #[slot]="scope">
      <slot :name="slot" v-bind="scope" />
    </template>
  </el-select>
</template>
```

- [ ] **步骤 5：创建 UiDatePicker.vue**

```vue
<script setup lang="ts">
defineOptions({ inheritAttrs: false })

const props = defineProps<{
  modelValue?: any
}>()

const emit = defineEmits<{
  'update:modelValue': [value: any]
}>()
</script>

<template>
  <el-date-picker
    :model-value="props.modelValue"
    @update:model-value="emit('update:modelValue', $event)"
    v-bind="$attrs"
  />
</template>
```

- [ ] **步骤 6：创建 UiButton.vue**

```vue
<script setup lang="ts">
defineOptions({ inheritAttrs: false })

withDefaults(defineProps<{
  type?: 'primary' | 'default' | 'danger'
  loading?: boolean
}>(), { type: 'default' })
</script>

<template>
  <el-button
    :type="type"
    :loading="loading"
    v-bind="$attrs"
  >
    <slot />
  </el-button>
</template>
```

- [ ] **步骤 7：创建 UiSection.vue**

```vue
<script setup lang="ts">
defineProps<{
  title?: string
  description?: string
}>()
</script>

<template>
  <div class="ui-section">
    <div v-if="title || description" class="ui-section__header">
      <h3 v-if="title" class="ui-section__title">{{ title }}</h3>
      <p v-if="description" class="ui-section__desc">{{ description }}</p>
    </div>
    <div class="ui-section__body">
      <slot />
    </div>
  </div>
</template>

<style scoped>
.ui-section { margin-bottom: 24px; }
.ui-section__header { margin-bottom: 16px; }
.ui-section__title { font-size: 14px; font-weight: 600; color: var(--text-primary); margin: 0; }
.ui-section__desc { font-size: 13px; color: var(--text-secondary); margin: 4px 0 0; }
.ui-section__body { display: flex; flex-direction: column; gap: 20px; }
</style>
```

- [ ] **步骤 8：创建 UiCard.vue**

```vue
<script setup lang="ts">
withDefaults(defineProps<{
  padding?: string
  hoverable?: boolean
}>(), { padding: 'var(--sp-6)', hoverable: false })
</script>

<template>
  <div
    class="ui-card"
    :class="{ 'ui-card--hoverable': hoverable }"
    :style="{ padding }"
  >
    <slot />
  </div>
</template>

<style scoped>
.ui-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  transition: box-shadow var(--transition), transform var(--transition);
}
.ui-card--hoverable:hover {
  box-shadow: var(--shadow-md);
  transform: translateY(-2px);
}
</style>
```

- [ ] **步骤 9：创建 index.ts**

```ts
export { default as UiDialog } from './UiDialog.vue'
export { default as UiInput } from './UiInput.vue'
export { default as UiTextarea } from './UiTextarea.vue'
export { default as UiSelect } from './UiSelect.vue'
export { default as UiDatePicker } from './UiDatePicker.vue'
export { default as UiButton } from './UiButton.vue'
export { default as UiSection } from './UiSection.vue'
export { default as UiCard } from './UiCard.vue'
```

- [ ] **步骤 10：提交**

```bash
git add personal-hub-web/src/components/ui/
git commit -m "feat: 封装统一 Ui 组件库（Dialog/Input/Select/Button 等 8 个）"
```

---

### 阶段 3a-1：Todo 改为 Dialog

**文件：**
- 创建：`personal-hub-web/src/modules/planning/todo/TodoDialog.vue`
- 修改：`personal-hub-web/src/modules/planning/todo/List.vue`

- [ ] **步骤 1：创建 TodoDialog.vue**

将 `Todo/Form.vue` 的 script 和 template 迁移到 Dialog 模式：

```vue
<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { createTodo, updateTodo, getTodoById } from '@/api/todoApi'
import { ElMessage } from 'element-plus'
import { UiDialog, UiInput, UiTextarea, UiDatePicker, UiButton, UiSection } from '@/components/ui'

const props = withDefaults(defineProps<{
  modelValue: boolean
  entityId?: number
}>(), { entityId: undefined })

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'saved': []
}>()

const isEdit = () => props.entityId !== undefined

const form = ref({ title: '', content: '', priority: 2, dueDate: null as string | null })
const saving = ref(false)

const priorityOptions = [
  { value: 1, label: '高', type: 'danger' as const },
  { value: 2, label: '中', type: 'warning' as const },
  { value: 3, label: '低', type: 'info' as const }
]

watch(() => props.modelValue, async (val) => {
  if (val) {
    if (isEdit()) {
      const res = await getTodoById(props.entityId!)
      const r = res.data.data
      form.value = { title: r.title, content: r.content || '', priority: r.priority, dueDate: r.dueDate }
    } else {
      form.value = { title: '', content: '', priority: 2, dueDate: null }
    }
  }
})

async function handleSave() {
  if (!form.value.title.trim()) { ElMessage.warning('请输入任务标题'); return }
  saving.value = true
  try {
    if (isEdit()) {
      await updateTodo(props.entityId!, form.value)
      ElMessage.success('已更新')
    } else {
      await createTodo(form.value)
      ElMessage.success('已创建')
    }
    emit('update:modelValue', false)
    emit('saved')
  } finally { saving.value = false }
}
</script>

<template>
  <UiDialog
    :model-value="modelValue"
    :title="isEdit ? '编辑任务' : '新建任务'"
    @update:model-value="emit('update:modelValue', $event)"
  >
    <el-form label-position="top">
      <el-form-item label="任务标题" required>
        <UiInput v-model="form.title" placeholder="例如：完成接口文档编写" maxlength="200" show-word-limit />
      </el-form-item>
      <el-form-item label="优先级">
        <el-radio-group v-model="form.priority">
          <el-radio v-for="item in priorityOptions" :key="item.value" :value="item.value">
            <el-tag :type="item.type" size="small">{{ item.label }}</el-tag>
          </el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="截止日期">
        <UiDatePicker v-model="form.dueDate" type="date" value-format="YYYY-MM-DD" placeholder="选择截止日期" style="width:100%" clearable />
      </el-form-item>
      <el-form-item label="任务内容">
        <UiTextarea v-model="form.content" placeholder="补充任务详情（可选）" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="emit('update:modelValue', false)">取消</el-button>
      <UiButton type="primary" :loading="saving" @click="handleSave">保存</UiButton>
    </template>
  </UiDialog>
</template>
```

- [ ] **步骤 2：修改 TodoList.vue**

在 `TodoList.vue` 中：
- 导入 `TodoDialog` 组件
- 添加 `dialogVisible` ref 和 `editId` ref
- 创建按钮的 `@click` 改为 `dialogVisible = true`
- 编辑按钮的 `@click` 改为 `editId = item.id; dialogVisible = true`
- 在模板末尾添加 `<TodoDialog v-model="dialogVisible" :entity-id="editId" @saved="fetchList" />`

需要查看 `TodoList.vue` 的完整内容来确定精确插入位置。关键改动模式：

```diff
+ import { ref } from 'vue'
+ import TodoDialog from './TodoDialog.vue'

+ const dialogVisible = ref(false)
+ const editId = ref<number | undefined>()

+ function openCreate() {
+   editId.value = undefined
+   dialogVisible.value = true
+ }
+
+ function openEdit(id: number) {
+   editId.value = id
+   dialogVisible.value = true
+ }

  // 创建按钮：@click="openCreate"
  // 编辑按钮：@click="openEdit(item.id)"

+ <TodoDialog v-model="dialogVisible" :entity-id="editId" @saved="fetchList" />
```

- [ ] **步骤 3：提交**

```bash
git add personal-hub-web/src/modules/planning/todo/
git commit -m "refactor: Todo 改为 Dialog 形式，移除独立路由表单"
```

---

### 阶段 3a-2：Bookmark 改为 Dialog

**文件：**
- 创建：`personal-hub-web/src/modules/resource/bookmark/BookmarkDialog.vue`
- 修改：`personal-hub-web/src/modules/resource/bookmark/List.vue`

- [ ] **步骤 1：创建 BookmarkDialog.vue**

与 TodoDialog 相同的模式，基于 `bookmark/Form.vue` 的字段（title / url / description / categoryId / tagIds）。

```vue
<script setup lang="ts">
import { ref, watch } from 'vue'
import { createBookmark, updateBookmark, getBookmarkById, getBookmarkCategories } from '@/api/bookmarkApi'
import { getTags } from '@/api/tagApi'
import { ElMessage } from 'element-plus'
import { UiDialog, UiInput, UiTextarea, UiSelect, UiButton } from '@/components/ui'
import { FolderOpen } from 'lucide-vue-next'
import type { BookmarkCategoryVO } from '@/types/bookmark'
import type { TagVO } from '@/types/tag'

const props = withDefaults(defineProps<{
  modelValue: boolean
  entityId?: number
}>(), { entityId: undefined })

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'saved': []
}>()

const form = ref({ title: '', url: '', description: '', categoryId: null as number | null, tagIds: [] as number[] })
const categories = ref<BookmarkCategoryVO[]>([])
const tags = ref<TagVO[]>([])
const saving = ref(false)

watch(() => props.modelValue, async (val) => {
  if (!val) return
  try {
    const [catRes, tagRes] = await Promise.all([getBookmarkCategories(), getTags()])
    categories.value = catRes.data.data
    tags.value = tagRes.data.data
  } catch { /* ignore */ }

  if (props.entityId) {
    const res = await getBookmarkById(props.entityId)
    const r = res.data.data
    form.value = {
      title: r.title, url: r.url, description: r.description || '',
      categoryId: r.categoryId, tagIds: (r.tags || []).map((t: TagVO) => t.id)
    }
  } else {
    form.value = { title: '', url: '', description: '', categoryId: null, tagIds: [] }
  }
})

async function handleSave() {
  if (!form.value.title.trim()) { ElMessage.warning('请输入标题'); return }
  if (!form.value.url.trim()) { ElMessage.warning('请输入网址'); return }
  let url = form.value.url.trim()
  if (!/^https?:\/\//i.test(url)) url = 'https://' + url
  saving.value = true
  try {
    if (props.entityId) {
      await updateBookmark(props.entityId, { ...form.value, url })
      ElMessage.success('已更新')
    } else {
      await createBookmark({ ...form.value, url })
      ElMessage.success('已创建')
    }
    emit('update:modelValue', false)
    emit('saved')
  } finally { saving.value = false }
}
</script>

<template>
  <UiDialog
    :model-value="modelValue"
    :title="entityId ? '编辑收藏' : '新建收藏'"
    @update:model-value="emit('update:modelValue', $event)"
  >
    <el-form label-position="top">
      <el-form-item label="标题" required>
        <UiInput v-model="form.title" placeholder="例如：GitHub" maxlength="255" show-word-limit />
      </el-form-item>
      <el-form-item label="网址" required>
        <UiInput v-model="form.url" placeholder="例如：https://github.com" maxlength="2048" />
      </el-form-item>
      <el-form-item label="描述">
        <UiTextarea v-model="form.description" placeholder="简要描述（可选）" />
      </el-form-item>
      <el-form-item label="分类">
        <UiSelect v-model="form.categoryId" :options="categories.map(c => ({ value: c.id, label: c.name }))" placeholder="选择分类" clearable>
          <template #default>
            <el-option v-for="c in categories" :key="c.id" :value="c.id" :label="c.name">
              <span><FolderOpen :size="14" /> {{ c.name }}</span>
            </el-option>
          </template>
        </UiSelect>
      </el-form-item>
      <el-form-item label="标签">
        <el-select v-model="form.tagIds" multiple placeholder="选择标签" style="width:100%" clearable>
          <el-option v-for="t in tags" :key="t.id" :value="t.id" :label="t.name">
            <span class="tag-option"><span class="tag-dot" :style="{ background: t.color }" /> {{ t.name }}</span>
          </el-option>
        </el-select>
        <div class="form-hint">选择已有标签，可在标签管理页面创建新标签</div>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="emit('update:modelValue', false)">取消</el-button>
      <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
    </template>
  </UiDialog>
</template>

<style scoped>
.tag-option { display: flex; align-items: center; gap: 6px; }
.tag-dot { width: 8px; height: 8px; border-radius: 50%; display: inline-block; }
.form-hint { font-size: var(--text-xs); color: var(--text-tertiary); margin-top: 4px; }
</style>
```

- [ ] **步骤 2：修改 BookmarkList.vue**

在 `BookmarkList.vue` 中：
- 导入 `BookmarkDialog` 组件
- 添加 `dialogVisible` ref 和 `editId` ref
- 创建按钮改为打开 Dialog
- 编辑按钮改为打开 Dialog
- 在模板末尾添加 `<BookmarkDialog v-model="dialogVisible" :entity-id="editId" @saved="fetchList" />`

- [ ] **步骤 3：提交**

```bash
git add personal-hub-web/src/modules/resource/bookmark/
git commit -m "refactor: Bookmark 改为 Dialog 形式"
```

---

### 阶段 3a-3：标签/分类 Manage 页面使用 UiDialog

**文件：**
- 修改：`personal-hub-web/src/modules/knowledge/note/CategoryManage.vue`
- 修改：`personal-hub-web/src/modules/knowledge/tag/Manage.vue`
- 修改：`personal-hub-web/src/modules/knowledge/note/TagManage.vue`
- 修改：`personal-hub-web/src/modules/resource/bookmark/CategoryManage.vue`
- 修改：`personal-hub-web/src/modules/resource/file/CategoryManage.vue`

- [ ] **步骤 1：修改 Tag Manage.vue 使用 UiDialog**

将 `el-dialog` 替换为 `UiDialog`，保留相同的逻辑。

```diff
+ import { UiDialog, UiInput, UiButton } from '@/components/ui'

- <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑标签' : '新建标签'" width="420px" top="30vh">
+ <UiDialog v-model="dialogVisible" :title="isEdit ? '编辑标签' : '新建标签'" width="420">
    <!-- 内容不变 -->
-   <template #footer>
-     <el-button @click="dialogVisible = false">取消</el-button>
-     <el-button type="primary" @click="handleSave">保存</el-button>
-   </template>
- </el-dialog>
+   <template #footer>
+     <el-button @click="dialogVisible = false">取消</el-button>
+     <UiButton type="primary" @click="handleSave">保存</UiButton>
+   </template>
+ </UiDialog>
```

同时将 `<el-input>` 替换为 `<UiInput>`，将 width 改为 520（规范默认值）或保持 420。

- [ ] **步骤 2：修改每个 CategoryManage.vue**

同上模式：替换 `el-dialog` → `UiDialog`，`el-input` → `UiInput`，`el-input-number` → 保留或视情况替换。

- [ ] **步骤 3：提交**

```bash
git add personal-hub-web/src/modules/knowledge/note/CategoryManage.vue personal-hub-web/src/modules/knowledge/note/TagManage.vue personal-hub-web/src/modules/knowledge/tag/Manage.vue personal-hub-web/src/modules/resource/bookmark/CategoryManage.vue personal-hub-web/src/modules/resource/file/CategoryManage.vue
git commit -m "refactor: 标签/分类管理页面使用 UiDialog 统一风格"
```

---

### 阶段 3b-1：Reading Record 改为 Drawer

**文件：**
- 创建：`personal-hub-web/src/modules/knowledge/reading/ReadingDrawer.vue`
- 修改：`personal-hub-web/src/modules/knowledge/reading/List.vue`

- [ ] **步骤 1：创建 ReadingDrawer.vue**

基于 `reading/Form.vue` 的完整表单，替换 `el-dialog` → `el-drawer`：

```vue
<script setup lang="ts">
import { ref, watch } from 'vue'
import { createReading, updateReading, getReadingById } from '@/api/readingApi'
import { ElMessage } from 'element-plus'
import { Star } from 'lucide-vue-next'
import { UiInput, UiTextarea, UiDatePicker, UiButton, UiSection } from '@/components/ui'

const props = withDefaults(defineProps<{
  modelValue: boolean
  entityId?: number
}>(), { entityId: undefined })

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'saved': []
}>()

const form = ref({
  bookTitle: '', author: '', coverUrl: '', totalChapters: 0, currentChapter: 0,
  progress: 0, rating: undefined as number | undefined, totalDuration: undefined as number | undefined,
  status: 0, notes: '', startDate: null as string | null, endDate: null as string | null
})
const saving = ref(false)

const statusOptions = [
  { value: 0, label: '未读' }, { value: 1, label: '在读' }, { value: 2, label: '读完' }
]

watch(() => props.modelValue, async (val) => {
  if (!val) return
  if (props.entityId) {
    const r = (await getReadingById(props.entityId)).data.data
    form.value = {
      bookTitle: r.bookTitle, author: r.author || '', coverUrl: r.coverUrl || '',
      totalChapters: r.totalChapters, currentChapter: r.currentChapter, progress: r.progress,
      rating: r.rating, totalDuration: r.totalDuration, status: r.status,
      notes: r.notes || '', startDate: r.startDate, endDate: r.endDate
    }
  } else {
    form.value = {
      bookTitle: '', author: '', coverUrl: '', totalChapters: 0, currentChapter: 0,
      progress: 0, rating: undefined, totalDuration: undefined, status: 0,
      notes: '', startDate: null, endDate: null
    }
  }
})

async function handleSave() {
  if (!form.value.bookTitle.trim()) { ElMessage.warning('请输入书名'); return }
  saving.value = true
  try {
    if (props.entityId) {
      await updateReading(props.entityId, form.value)
      ElMessage.success('已更新')
    } else {
      await createReading(form.value)
      ElMessage.success('已创建')
    }
    emit('update:modelValue', false)
    emit('saved')
  } finally { saving.value = false }
}

function onProgressChange(val: number) {
  form.value.progress = val
  if (val >= 100) form.value.status = 2
  else if (val > 0 && form.value.status === 0) form.value.status = 1
}
</script>

<template>
  <el-drawer
    :model-value="modelValue"
    :title="entityId ? '编辑阅读记录' : '添加书籍'"
    :size="560"
    @update:model-value="emit('update:modelValue', $event)"
  >
    <el-form label-position="top">
      <UiSection title="基本信息">
        <el-form-item label="书名" required>
          <UiInput v-model="form.bookTitle" placeholder="例如：深入理解Java虚拟机" maxlength="255" show-word-limit />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="作者"><UiInput v-model="form.author" placeholder="作者（可选）" maxlength="200" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="封面图"><UiInput v-model="form.coverUrl" placeholder="封面图片URL（可选）" /></el-form-item>
          </el-col>
        </el-row>
      </UiSection>

      <UiSection title="阅读进度">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="总章节"><el-input-number v-model="form.totalChapters" :min="0" style="width:100%" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="当前章节"><el-input-number v-model="form.currentChapter" :min="0" :max="form.totalChapters || 9999" style="width:100%" /></el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="阅读状态">
          <el-radio-group v-model="form.status">
            <el-radio v-for="s in statusOptions" :key="s.value" :value="s.value">{{ s.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="阅读进度">
          <el-slider v-model="form.progress" :min="0" :max="100" @change="onProgressChange">
            <span class="progress-label">{{ form.progress }}%</span>
          </el-slider>
        </el-form-item>
      </UiSection>

      <UiSection title="评分与时长">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="评分">
              <div class="star-rating">
                <button v-for="i in 5" :key="i" class="star-btn" :class="{ active: i <= (form.rating || 0) }" @click="form.rating = i">
                  <Star :size="18" :fill="i <= (form.rating || 0) ? 'var(--warning)' : 'none'" :color="i <= (form.rating || 0) ? 'var(--warning)' : 'var(--text-tertiary)'" />
                </button>
                <span v-if="!form.rating" class="star-hint">点击评分</span>
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="阅读时长（分钟）"><el-input-number v-model="form.totalDuration" :min="0" :step="10" style="width:100%" /></el-form-item>
          </el-col>
        </el-row>
      </UiSection>

      <UiSection title="时间">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="开始日期"><UiDatePicker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" placeholder="开始日期" style="width:100%" clearable /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="读完日期"><UiDatePicker v-model="form.endDate" type="date" value-format="YYYY-MM-DD" placeholder="读完日期" style="width:100%" clearable /></el-form-item>
          </el-col>
        </el-row>
      </UiSection>

      <UiSection title="笔记">
        <el-form-item label="阅读笔记">
          <UiTextarea v-model="form.notes" placeholder="记录读后感、摘抄等" />
        </el-form-item>
      </UiSection>
    </el-form>

    <template #footer>
      <el-button @click="emit('update:modelValue', false)">取消</el-button>
      <UiButton type="primary" :loading="saving" @click="handleSave">保存</UiButton>
    </template>
  </el-drawer>
</template>

<style scoped>
.progress-label { font-size: var(--text-sm); font-weight: 600; margin-left: var(--sp-2); }
.star-rating { display: flex; align-items: center; gap: 2px; }
.star-btn { background: none; border: none; cursor: pointer; padding: 2px; transition: transform var(--transition); }
.star-btn:hover { transform: scale(1.2); }
.star-btn.active { transform: scale(1.05); }
.star-hint { font-size: var(--text-xs); color: var(--text-tertiary); margin-left: var(--sp-2); }
</style>
```

- [ ] **步骤 2：修改 ReadingList.vue**

在 `ReadingList.vue` 中导入 `ReadingDrawer`，添加 `dialogVisible`/`editId`，创建/编辑按钮改为打开 Drawer。

- [ ] **步骤 3：提交**

```bash
git add personal-hub-web/src/modules/knowledge/reading/
git commit -m "refactor: Reading Record 改为 Drawer 形式"
```

---

### 阶段 3b-2：Study Record 改为 Drawer

**文件：**
- 创建：`personal-hub-web/src/modules/knowledge/study/StudyDrawer.vue`
- 修改：`personal-hub-web/src/modules/knowledge/study/List.vue`

- [ ] **步骤 1：创建 StudyDrawer.vue**

基于 `study/Form.vue`，使用 Drawer + UiSection 分块：

```vue
<script setup lang="ts">
import { ref, watch } from 'vue'
import { createStudyRecord, updateStudyRecord, getStudyRecordById } from '@/api/studyApi'
import { ElMessage } from 'element-plus'
import { UiInput, UiTextarea, UiDatePicker, UiButton, UiSection } from '@/components/ui'

const props = withDefaults(defineProps<{
  modelValue: boolean
  entityId?: number
}>(), { entityId: undefined })

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'saved': []
}>()

const form = ref({ subject: '', date: new Date().toISOString().slice(0, 10), duration: 60, content: '', reflection: '' })
const saving = ref(false)

watch(() => props.modelValue, async (val) => {
  if (!val) return
  if (props.entityId) {
    const r = (await getStudyRecordById(props.entityId)).data.data
    form.value = { subject: r.subject, date: r.date, duration: r.duration, content: r.content || '', reflection: r.reflection || '' }
  } else {
    form.value = { subject: '', date: new Date().toISOString().slice(0, 10), duration: 60, content: '', reflection: '' }
  }
})

async function handleSave() {
  if (!form.value.subject.trim()) { ElMessage.warning('请输入学习主题'); return }
  if (!form.value.duration || form.value.duration < 1) { ElMessage.warning('时长至少1分钟'); return }
  saving.value = true
  try {
    if (props.entityId) {
      await updateStudyRecord(props.entityId, form.value)
      ElMessage.success('已更新')
    } else {
      await createStudyRecord(form.value)
      ElMessage.success('已创建')
    }
    emit('update:modelValue', false)
    emit('saved')
  } finally { saving.value = false }
}
</script>

<template>
  <el-drawer
    :model-value="modelValue"
    :title="entityId ? '编辑学习记录' : '新建学习记录'"
    :size="480"
    @update:model-value="emit('update:modelValue', $event)"
  >
    <el-form label-position="top">
      <UiSection title="基本信息">
        <el-form-item label="学习主题" required>
          <UiInput v-model="form.subject" placeholder="例如：Spring Boot 基础学习" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="日期" required>
              <UiDatePicker v-model="form.date" type="date" value-format="YYYY-MM-DD" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="时长（分钟）" required>
              <el-input-number v-model="form.duration" :min="1" :max="1440" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
      </UiSection>

      <UiSection title="学习内容">
        <el-form-item label="学习内容">
          <UiTextarea v-model="form.content" placeholder="记录今天学了什么..." />
        </el-form-item>
        <el-form-item label="心得">
          <UiTextarea v-model="form.reflection" placeholder="有什么收获或思考？" />
        </el-form-item>
      </UiSection>
    </el-form>

    <template #footer>
      <el-button @click="emit('update:modelValue', false)">取消</el-button>
      <UiButton type="primary" :loading="saving" @click="handleSave">保存</UiButton>
    </template>
  </el-drawer>
</template>
```

- [ ] **步骤 2：修改 StudyList.vue**

同 Reading 模式：导入 `StudyDrawer`，添加 Dialog 控制逻辑。

- [ ] **步骤 3：提交**

```bash
git add personal-hub-web/src/modules/knowledge/study/
git commit -m "refactor: Study Record 改为 Drawer 形式"
```

---

### 阶段 3b-3：Study Plan 改为 Drawer

**文件：**
- 创建：`personal-hub-web/src/modules/planning/studyplan/StudyPlanDrawer.vue`
- 修改：`personal-hub-web/src/modules/planning/studyplan/List.vue`

- [ ] **步骤 1：创建 StudyPlanDrawer.vue**

基于 `studyplan/Form.vue`，使用 Drawer + UiSection 分块：

```vue
<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { createStudyPlan, updateStudyPlan, getStudyPlanById } from '@/api/studyplanApi'
import { ElMessage } from 'element-plus'
import { UiInput, UiTextarea, UiDatePicker, UiButton, UiSection } from '@/components/ui'

const props = withDefaults(defineProps<{
  modelValue: boolean
  entityId?: number
}>(), { entityId: undefined })

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  'saved': []
}>()

const form = ref({
  name: '', goal: '', progress: 0,
  startDate: null as string | null, endDate: null as string | null, status: 0
})
const saving = ref(false)

const statusOptions = [
  { value: 0, label: '未开始' }, { value: 1, label: '进行中' },
  { value: 2, label: '已完成' }, { value: 3, label: '已放弃' }
]

const progressColor = computed(() => {
  if (form.value.progress >= 100) return 'var(--success)'
  if (form.value.progress >= 50) return 'var(--accent)'
  return 'var(--warning)'
})

watch(() => props.modelValue, async (val) => {
  if (!val) return
  if (props.entityId) {
    const r = (await getStudyPlanById(props.entityId)).data.data
    form.value = { name: r.name, goal: r.goal || '', progress: r.progress, startDate: r.startDate, endDate: r.endDate, status: r.status }
  } else {
    form.value = { name: '', goal: '', progress: 0, startDate: null, endDate: null, status: 0 }
  }
})

async function handleSave() {
  if (!form.value.name.trim()) { ElMessage.warning('请输入计划名称'); return }
  saving.value = true
  try {
    if (props.entityId) {
      await updateStudyPlan(props.entityId, form.value)
      ElMessage.success('已更新')
    } else {
      await createStudyPlan(form.value)
      ElMessage.success('已创建')
    }
    emit('update:modelValue', false)
    emit('saved')
  } finally { saving.value = false }
}
</script>

<template>
  <el-drawer
    :model-value="modelValue"
    :title="entityId ? '编辑计划' : '新建计划'"
    :size="480"
    @update:model-value="emit('update:modelValue', $event)"
  >
    <el-form label-position="top">
      <UiSection title="基本信息">
        <el-form-item label="计划名称" required>
          <UiInput v-model="form.name" placeholder="例如：Spring Boot 深入学习" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="学习目标">
          <UiTextarea v-model="form.goal" placeholder="设定学习目标（可选）" />
        </el-form-item>
      </UiSection>

      <UiSection title="时间安排">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="开始日期"><UiDatePicker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" placeholder="开始日期" style="width:100%" clearable /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束日期"><UiDatePicker v-model="form.endDate" type="date" value-format="YYYY-MM-DD" placeholder="结束日期" style="width:100%" clearable /></el-form-item>
          </el-col>
        </el-row>
      </UiSection>

      <UiSection title="进度">
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio v-for="s in statusOptions" :key="s.value" :value="s.value">{{ s.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="学习进度">
          <el-slider v-model="form.progress" :min="0" :max="100" :format-tooltip="(v: number) => v + '%'">
            <span class="progress-label" :style="{ color: progressColor }">{{ form.progress }}%</span>
          </el-slider>
        </el-form-item>
      </UiSection>
    </el-form>

    <template #footer>
      <el-button @click="emit('update:modelValue', false)">取消</el-button>
      <UiButton type="primary" :loading="saving" @click="handleSave">保存</UiButton>
    </template>
  </el-drawer>
</template>

<style scoped>
.progress-label { font-size: var(--text-sm); font-weight: 600; margin-left: var(--sp-2); }
</style>
```

- [ ] **步骤 2：修改 StudyPlanList.vue**

导入 Drawer，添加控制逻辑。

- [ ] **步骤 3：提交**

```bash
git add personal-hub-web/src/modules/planning/studyplan/
git commit -m "refactor: Study Plan 改为 Drawer 形式"
```

---

### 阶段 4：收尾清理

**文件：**
- 删除：`personal-hub-web/src/modules/planning/todo/Form.vue`
- 删除：`personal-hub-web/src/modules/resource/bookmark/Form.vue`
- 删除：`personal-hub-web/src/modules/knowledge/reading/Form.vue`
- 删除：`personal-hub-web/src/modules/knowledge/study/Form.vue`
- 删除：`personal-hub-web/src/modules/planning/studyplan/Form.vue`
- 修改：`personal-hub-web/src/router/index.ts`

- [ ] **步骤 1：删除旧 Form.vue 文件**

删除以上 5 个文件。

- [ ] **步骤 2：更新路由配置**

从 `personal-hub-web/src/router/index.ts` 中移除以下路由：
```
{ path: 'todos/new', ... }, { path: 'todos/:id/edit', ... }
{ path: 'bookmarks/new', ... }, { path: 'bookmarks/:id/edit', ... }
{ path: 'study-records/new', ... }, { path: 'study-records/:id/edit', ... }
{ path: 'readings/new', ... }, { path: 'readings/:id/edit', ... }
{ path: 'study-plans/new', ... }, { path: 'study-plans/:id/edit', ... }
```

同时删除文件顶部的对应 import。

- [ ] **步骤 3：全局验证**

```bash
cd personal-hub-web
npm run dev
```

确认无编译报错，所有 CRUD 功能正常：
- Todo: 新建/编辑/删除 → Dialog 弹出/关闭
- Bookmark: 同上
- Reading: 新建/编辑 → Drawer 弹出
- Study Record: 新建/编辑 → Drawer 弹出
- Study Plan: 新建/编辑 → Drawer 弹出
- Tag/Category: Dialog 使用 UiDialog 样式
- Note Editor / Diary: 仍为 Full Page

- [ ] **步骤 4：提交**

```bash
git add -A
git commit -m "chore: 清理旧 Form.vue 和路由，CRUD 全部迁移至 Dialog/Drawer"
```

---

## 验证方式

**每阶段提交后：**
1. 运行 `cd personal-hub-web && npm run dev` 确认无编译错误
2. 手动测试对应模块的 CRUD 功能
3. 检查深色模式切换后的 UI

**全局验证：**
- 所有列表页的"新建"和编辑操作使用 Dialog/Drawer，不跳转独立页面
- Dialog 宽度符合规范（520/600/720）
- Label/间距/按钮符合规范
- 取消按钮关闭 Dialog/Drawer，不触发路由跳转
- 保存成功后有 `ElMessage.success()` 提示
