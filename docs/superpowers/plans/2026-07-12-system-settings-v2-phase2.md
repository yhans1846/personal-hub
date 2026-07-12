# 系统设置 V2 Phase 2 实现计划

> **面向 AI 代理的工作者：** 步骤使用复选框（`- [ ]`）语法跟踪进度。

**目标：** 实现通知偏好、数据备份/导出、实验功能开关、外观扩展（圆角/动画/密度）四个模块。

**技术栈：** Vue 3 + TypeScript + Pinia + Element Plus + Spring Boot 3 + MyBatis-Plus

---

## 文件结构

### 新建
| 文件 | 职责 |
|------|------|
| `components/NotificationSettings.vue` | 通知偏好面板（桌面通知/类型/音效/免打扰） |
| `components/DataManagement.vue` | 数据备份/导出/恢复面板 |
| `components/ExperimentalFeatures.vue` | 实验功能开关面板 |
| `store/notificationConfigStore.ts` | 通知配置 Store |
| `store/featureFlagStore.ts` | 实验功能 Store |
| `composables/useFeatureFlag.ts` | 功能开关读取 composable |

### 后端新建
| 文件 | 职责 |
|------|------|
| `BackupController.java` | 备份/恢复接口 |
| `ExportController.java` | 数据导出接口 |
| `DataBackupService.java` / `DataBackupServiceImpl.java` | 备份业务逻辑 |
| `DataExportService.java` / `DataExportServiceImpl.java` | 导出业务逻辑 |
| `BackupDTO.java` / `ExportDTO.java` | DTO |
| `BackupVO.java` / `ExportVO.java` | VO |
| `UserBackup.java` | Entity |

### 修改
| 文件 | 说明 |
|------|------|
| `components/AppearanceSettings.vue` | 扩展：圆角/动画/密度配置 |
| `components/PlaceholderTab.vue` | 替换为正式组件（通知/实验功能） |
| `store/layoutStore.ts` | 扩展外观配置 |
| `types/layout.ts` | 扩展类型 |
| `SettingsView.vue` | 替换占位 Tab |
| `AppLayout.vue` | 同步密度/动画 CSS 变量 |

---

### 任务 1：扩展类型定义

**文件：** 修改 `types/layout.ts`

追加：

```ts
/** 外观配置（Phase 2 扩展） */
export interface ExtendedAppearanceConfig extends AppearanceConfig {
  borderRadius: 'sm' | 'md' | 'lg' | 'xl'
  animationSpeed: 'off' | 'slow' | 'normal' | 'fast'
  density: 'comfortable' | 'standard' | 'compact'
}

/** 通知配置 */
export interface NotificationConfig {
  desktopEnabled: boolean
  enabledTypes: string[]
  soundEnabled: boolean
  soundName: string
  doNotDisturb: boolean
  dndStart: string
  dndEnd: string
}

/** 实验功能配置 */
export interface FeatureFlags {
  mermaid: boolean
  katex: boolean
  aiAssistant: boolean
  backlink: boolean
}
```

### 任务 2：通知配置 Store

**文件：** 新建 `store/notificationConfigStore.ts`

- localStorage + `layout_type='notification'` 的后端持久化
- 方法：updateConfig / resetConfig / fetchFromBackend

### 任务 3：实验功能 Store

**文件：** 新建 `store/featureFlagStore.ts`

- localStorage 持久化
- 方法：isEnabled / toggle / resetAll
- 提供全局响应式 flags 对象

### 任务 4：useFeatureFlag composable

**文件：** 新建 `composables/useFeatureFlag.ts`

- 接受 feature key，返回 `ref<boolean>` 响应式状态
- 内部使用 featureFlagStore

### 任务 5：通知设置组件

**文件：** 新建 `components/NotificationSettings.vue`

- 桌面通知开关（请求 Notification API 权限）
- 通知类型多选 Chip（新笔记/待办到期/学习计划/系统更新）
- 通知音效开关 + 下拉选择
- 免打扰模式 + 时间段设置
- 恢复默认

### 任务 6：数据管理组件

**文件：** 新建 `components/DataManagement.vue`

- 缓存管理（从 CacheSettings 迁移至此）
- 数据导出：选择模块（笔记/日记/待办/收藏夹/学习记录/阅读记录/标签/分类/配置）+ 格式（Markdown/JSON）+ 导出按钮
- 数据备份：自动备份开关 + 频率选择 + 立即备份 + 导入恢复
- 后端接口：`POST /api/export` → 异步打包 + 下载
- import/export 按钮调用对应后端 API

### 任务 7：后端 — ExportController

**文件：** 新建 `ph-system/.../controller/ExportController.java`

- `POST /api/export`：接受模块列表 + 格式参数，返回文件流
- `GET /api/export/history`：查询导出历史

### 任务 8：后端 — DataExportService

**文件：** 新建 `ph-system/.../service/DataExportService.java` + `impl/DataExportServiceImpl.java`

- 按模块聚合数据（笔记→文件系统读取 md，其他→数据库查询）
- Markdown 导出：生成目录结构 ZIP 包
- JSON 导出：生成结构化 JSON
- 异步处理 + 下载链接返回

### 任务 9：后端 — BackupController + DataBackupService

**文件：** 新建

- `POST /api/backup/now`：立即备份全量数据
- `POST /api/backup/import`：上传备份文件恢复
- `GET /api/backup/download/{id}`：下载备份

### 任务 10：扩展外观设置组件

**文件：** 修改 `components/AppearanceSettings.vue`

- 新增圆角选择（4档 via CSS 变量）
- 新增动画速度（4档 via CSS 变量）
- 新增界面密度（3档 via CSS 变量）
- 扩展 AppearanceConfig 存储

### 任务 11：实验功能组件

**文件：** 新建 `components/ExperimentalFeatures.vue`

- 从 featureFlagStore 读取所有 flag
- 每个 flag 渲染为开关 + 描述
- 切换即时生效

### 任务 12：更新 SettingsView

**文件：** 修改 `SettingsView.vue`

- 通知 Tab：PlaceholderTab → NotificationSettings
- 实验功能 Tab：PlaceholderTab → ExperimentalFeatures
- 数据 Tab：CacheSettings → DataManagement（或保留 CacheSettings 作为子模块）

### 任务 13：编译验证 + 文档

- TypeScript 检查 + Vite 构建
- 更新 CHANGELOG
