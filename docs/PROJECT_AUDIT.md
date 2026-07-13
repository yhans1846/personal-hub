# Personal Hub - 项目审计报告

> 生成日期：2026-07-13
> 审计范围：personal-hub-server + personal-hub-web 全量代码

---

## 项目整体评分

```
目录结构          ★★★★★  清晰，前后端按领域对齐

代码质量          ★★★★☆  整体良好，any 类型和跨模块泄漏是主要扣分项

文档完整度        ★★★★☆  文档齐全，部分需与代码同步

规范统一程度      ★★★☆☆  前端 API 层双目录、CSS 原始像素值、重复模式未抽象

技术债            ★★★☆☆  主要集中在 any 类型(31处)、跨模块实体泄漏(20处)和重复代码模式
```

---

## 一、技术债统计

### TODO / FIXME

| 文件 | 位置 | 原因 | 建议 |
|------|------|------|------|
| `NotificationBell.vue` | L52 | `TODO_OVERDUE` 常量作为通知类型图标映射键 | 无需处理，实际是业务常量命名，不是待办 |
| `NotificationServiceImpl.java` | L95 | 注释中使用 `TODO_OVERDUE` 作为通知类型名 | 同上，不是真正的 TODO |
| 其余文件 | — | **0 个 TODO/FIXME** | 项目维护良好 |

### console / println

| 文件 | 位置 | 原因 | 建议 |
|------|------|------|------|
| 前端 | — | **0 个 console.log** | ✅ 优秀 |
| 后端 | — | **0 个 System.out.println** | ✅ 全部使用 Slf4j |

有 7 处 `console.warn`，均为 store fallback 日志（后端加载失败→使用本地缓存），符合规范，保留。

### any 类型（前端主要技术债）

| 文件 | 出现次数 | 说明 |
|------|---------|------|
| `UiDatePicker.vue` | 3 处 | modelValue 用 any |
| `UiSelect.vue` | 3 处 | modelValue/options 用 any |
| `CommandPalette.vue` | 2 处 | item/icon 用 any |
| `authStore.ts` | 1 处 | setUser 参数用 any |
| `layoutStore.ts` | 3 处 | saveToStorage 和 find 用 any |
| `readingConfigStore.ts` | 4 处 | merge/debounce/backend 用 any |
| `notificationConfigStore.ts` | 1 处 | backend find 用 any |
| `system/api.ts` | 1 处 | user 用 any |
| `StatsView.vue` | 2 处 | formatter/data 用 any |
| `Login.vue` | 1 处 | catch(e: any) |
| `CategoryManage.vue` | 2 处 | catch(e: any) |
| `tag/Manage.vue` | 2 处 | catch(e: any) |
| `Editor.vue` | 5 处 | callback/data 用 any |
| `file/List.vue` | 1 处 | catch(e: any) |
| `ImportMarkdownDialog.vue` | 1 处 | catch(e: any) |
| `AppearanceSettings.vue` | 2 处 | value/config 用 any |
| `DashboardManager.vue` | 1 处 | forEach 用 any |
| `ExperimentalFeatures.vue` | 1 处 | handleToggle 用 any |
| `MenuManager.vue` | 1 处 | ref 用 any |
| **合计** | **31+ 处** | 另有 18 处 `as any` 强制类型转换 |

### debugger

**0 个 debugger 语句** ✅

### 注释掉的代码

| 文件 | 位置 | 原因 | 建议 |
|------|------|------|------|
| `application-dev.yml` | L31 | `# log-impl: org.apache.ibatis.logging.stdout.StdOutImpl` | 删除 |

---

## 二、大文件统计

### 超过 300 行

| 行数 | 文件 | 模块 |
|------|------|------|
| 649 | `CategoryManage.vue` | 前端 - category |
| 647 | `tag/Manage.vue` | 前端 - knowledge/tag |
| 573 | `AppLayout.vue` | 前端 - components |
| 549 | `ImportMarkdownDialog.vue` | 前端 - knowledge/note |
| 536 | `Editor.vue` | 前端 - knowledge/note |
| 535 | `Preview.vue` | 前端 - knowledge/note |
| 516 | `DiaryDialog.vue` | 前端 - knowledge/diary |
| 514 | `ReadingDialog.vue` | 前端 - knowledge/reading |
| 474 | `ReadingExperience.vue` | 前端 - settings/components |
| 384 | `Dashboard.vue` | 前端 - dashboard |
| 346 | `StudyPlanDialog.vue` | 前端 - planning/studyplan |
| 315 | `DataManagement.vue` | 前端 - settings/components |
| 308 | `PreviewToc.vue` | 前端 - knowledge/preview |
| **305** | **`DashboardServiceImpl.java`** | **后端 - ph-dashboard** |

### 超过 500 行

共 **8 个文件**（均为前端 .vue），见上表前 8 行。

### 超过 800 行

**0 个文件** ✅

### 拆分建议

| 文件 | 建议策略 |
|------|---------|
| `CategoryManage.vue` (649行) | 拆为：统计卡片区 + 分类Grid列表 + 拖拽排序逻辑 |
| `tag/Manage.vue` (647行) | 拆为：统计卡片区 + 标签Grid列表 + 颜色筛选逻辑 |
| `AppLayout.vue` (573行) | 拆为：侧边栏组件 + 顶栏组件 + 主布局壳 |
| `ImportMarkdownDialog.vue` (549行) | 当前双Tab设计合理，可提取 Tab 内容为子组件 |
| `DiaryDialog.vue` (516行) | 提取图片上传逻辑为独立 composable |
| `ReadingDialog.vue` (514行) | 提取评分选择和章节管理为子组件 |
| `Editor.vue` (536行) | 已有 editor/ 子目录，合理 |
| `Preview.vue` (535行) | 已有 preview/ 子目录，合理 |

---

## 三、重复代码分析

### 3.1 前端 API 层双目录（权重：高）

`src/api/` 目录下有 **11 个 3 行重导出文件**：

| 文件 | 内容 |
|------|------|
| `authApi.ts` | `export * from '@/modules/system/api'` |
| `noteApi.ts` | `export * from '@/modules/knowledge/api'` |
| `todoApi.ts` | `export * from '@/modules/planning/api'` |
| `bookmarkApi.ts` | `export * from '@/modules/resource/api'` |
| `diaryApi.ts` | `export * from '@/modules/knowledge/api'` |
| `fileApi.ts` | `export * from '@/modules/resource/api'` |
| `readingApi.ts` | `export * from '@/modules/knowledge/api'` |
| `studyApi.ts` | `export * from '@/modules/knowledge/api'` |
| `studyplanApi.ts` | `export * from '@/modules/planning/api'` |
| `tagApi.ts` | `export * from '@/modules/knowledge/api'` |
| `dashboardApi.ts` | `export * from '@/modules/dashboard/api'` |

导致组件 import 来源不统一：有的从 `@/api/noteApi` 导入，有的从 `@/modules/knowledge/api` 导入。

### 3.2 前端 Dialog 组件重复模式（权重：中）

6 个 Dialog 组件使用**完全相同的生命周期模式**：

```
TodoDialog (298行) / DiaryDialog (516行) / BookmarkDialog (283行)
ReadingDialog (514行) / StudyDialog (256行) / StudyPlanDialog (346行)
```

通用模式：
```typescript
const props = withDefaults(defineProps<{ modelValue: boolean; entityId?: number }>(), {})
const emit = defineEmits<{ 'update:modelValue': [value: boolean]; 'saved': [] }>()
watch(() => props.modelValue, async (val) => { if (val && props.entityId) { loadData() } })
```

可提取 `useEntityDialog` composable 消除重复。

### 3.3 前端 Store 双存储模式重复（权重：中）

3 个 Store 各自实现同一套 localStorage + 后端同步模式：

| Store | localStorage | 后端同步 | fallback warn |
|-------|-------------|---------|---------------|
| `layoutStore.ts` | ✅ | ✅ | ✅ |
| `readingConfigStore.ts` | ✅ | ✅ | ✅ |
| `notificationConfigStore.ts` | ✅ | ✅ | ✅ |

可提取统一的 `useConfigStore` 基础 composable。

### 3.4 `estimateReadingTime` 重复实现（权重：中）

| 位置 | 速度 | 实现 |
|------|------|------|
| `utils/readingTime.ts` | 350 chars/min | 独立工具函数 |
| `Preview.vue` L44-51 | 500 chars+words/min | 内联复制 |

两个实现会给出不同结果，应统一。

### 3.5 前端 CSS 原始像素值（权重：低）

| 文件 | 位置 | 原始值 | 应使用 |
|------|------|--------|--------|
| `global.css` | L266 | `padding: 0 8px` | `var(--sp-2)` |
| `global.css` | L325 | `padding: 12px 16px` | `var(--sp-3) var(--sp-4)` |
| `global.css` | L409 | `padding: 4px` | `var(--sp-1)` |
| `global.css` | L414 | `padding: 6px 12px` | `var(--sp-2) var(--sp-3)` |
| `markdown-prose.css` | L88 | `padding: 2px 6px` | `var(--sp-1) var(--sp-2)` |
| `markdown-prose.css` | L99 | `padding: 16px` | `var(--sp-4)` |
| `markdown-prose.css` | L170 | `padding: 8px 14px` | `var(--sp-2) var(--sp-3)` |

### 3.6 后端重复文件读取代码（权重：低）

`ImportService.java` L174-186 和 `MarkdownImportService.java` L47-58 实现相同的 `BufferedReader` 读取 MultipartFile 逻辑。应提取为公共工具方法。

### 3.7 后端 DashboardMapper.xml 重复 SQL 模式（权重：低）

8 个搜索查询使用完全相同模板（SELECT id/title/snippet/date_str FROM table WHERE user_id+keyword），差异仅在于表名和字段名。

---

## 四、无用代码分析

### 4.1 前端未引用/冗余代码

| 项目 | 状态 |
|------|------|
| 未使用 Component | ✅ 无 |
| 未使用 Hook | ✅ 无（仅 1 个 composable 正常使用） |
| 未使用 Utils | ✅ `readingTime.ts` 三个函数均被引用 |
| 未使用 Type | ✅ 所有 type 均被引用 |
| 未使用 Store | ✅ 6 个 store 均被引用 |
| 冗余 API 文件 | ⚠️ 11 个重导出 shim 文件（`src/api/noteApi.ts` 等） |
| 未使用 CSS | ✅ 未发现死 CSS |
| 未使用图片/SVG/Icon | ✅ `src/assets/` 为空 |

### 4.2 后端未引用代码

| 文件 | 方法 | 状态 |
|------|------|------|
| `TagService.java:48` / `TagServiceImpl.java:192` | `getEntityIdsByTag()` | **已定义但从未调用** - 死代码 |
| `JwtUtil.java:3` | `import Claims` | **未使用的 import** |

### 4.3 Stub 实现（占位代码）

| 控制器 | 方法 | 状态 |
|--------|------|------|
| `ExportController.java` | `export()`, `download()`, `history()` | 空实现，仅返回 501 |
| `BackupController.java` | `backupNow()`, `list()`, `download()`, `importBackup()` | 空实现 |
| `UserLayoutServiceImpl.java` | `importLayout()` | 仅日志，无逻辑 |

---

## 五、架构问题（跨模块依赖泄漏）

### 5.1 后端实体泄漏（权重：高）

**总计 20 处跨模块实体/Mapper 直接引用**，违反"通过 Service 接口访问"的原则：

| 源模块 | 目标模块 | 泄漏文件 | 泄漏数 |
|--------|---------|---------|--------|
| ph-dashboard | ph-knowledge | `DashboardServiceImpl.java` | 8 处（Note/DiaryEntry/StudyRecord/ReadingRecord） |
| ph-dashboard | ph-planning | `DashboardServiceImpl.java` | 4 处（TodoTask/StudyPlan） |
| ph-dashboard | ph-resource | `DashboardServiceImpl.java` | 4 处（BookmarkUrl/FileResource） |
| ph-planning | ph-knowledge | `StudyPlanServiceImpl.java` | 2 处（StudyRecord） |
| ph-resource | ph-knowledge | `NoteFileServiceImpl.java` | 2 处（Note） |
| ph-system SQL | ph-knowledge/planning | `NotificationMapper.xml` | SQL 级别泄漏 |

### 5.2 模块职责边界

| 模块 | 职责 | 评估 |
|------|------|------|
| ph-common | 基础组件 | ✅ 清晰 |
| ph-system | 用户/认证/通知/布局/审计 | ✅ 清晰，SQL 泄漏需修复 |
| ph-knowledge | 笔记/日记/学习/阅读/标签/分类 | ✅ 清晰，体积最大 |
| ph-planning | Todo/学习计划 | ⚠️ 泄漏到 ph-knowledge |
| ph-resource | 收藏夹/文件 | ⚠️ 泄漏到 ph-knowledge |
| ph-dashboard | 聚合查询 | ❌ 严重泄漏到 3 个模块 |
| ph-storage | 文件存储抽象 | ✅ 清晰独立 |
| ph-boot | 启动入口 | ✅ 清晰 |

---

## 六、配置与安全

| 问题 | 位置 | 严重度 | 建议 |
|------|------|--------|------|
| JWT secret 占位符 | `application-prod.yml:35` | **高** | 替换为真实密钥 |
| StorageService 抛通用 RuntimeException | `LocalStorageServiceImpl.java` 多处 | 中 | 使用自定义异常 |
| ExportRequest 无校验注解 | `ExportController.java:43` | 低 | 添加 @Valid 校验 |

---

## 七、总结

### 关键发现优先级

| 优先级 | 问题 | 影响 |
|--------|------|------|
| **P0** | 跨模块实体泄漏（20处） | 破坏模块封装，后续重构困难 |
| **P1** | 前端 31+ 处 `any` 类型 | 失去 TypeScript 类型保护 |
| **P1** | 11 个 API 重导出 shim 文件 | 造成 import 路径混乱 |
| **P2** | 7 个文件 > 500 行 | 可维护性下降 |
| **P2** | Dialog 组件重复模式（6个） | 重复代码，修改需改多处 |
| **P2** | Store 双存储模式重复（3个） | 可提取公共 composable |
| **P3** | JWT secret 占位符 | 生产安全风险 |
| **P3** | CSS 原始像素值（7处） | 与设计令牌体系不一致 |
| **P3** | 空实现 stub 控制器（2个） | 占位代码需完成或移除 |

### 项目亮点

- ✅ 零 console.log（前端）/ System.out.println（后端）
- ✅ 零 debugger 语句
- ✅ 零 FIXME 注释
- ✅ 0 个文件超过 800 行
- ✅ 所有后端日志使用 Slf4j
- ✅ 前端 Pinia store 使用 Composition API 模式统一
- ✅ 整体目录结构整洁，前后端模块对齐
- ✅ TypeScript 类型定义集中管理，命名规范

---

*本报告作为 `项目治理.md` 第一阶段输出，后续阶段将基于此报告逐项治理。*
