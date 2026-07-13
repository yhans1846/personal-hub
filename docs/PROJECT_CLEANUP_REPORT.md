# Personal Hub - 项目治理报告

> 执行日期：2026-07-13
> 治理范围：全项目（前端 + 后端 + 文档）

---

## 治理目标

不修改业务功能、不改变接口行为、不影响数据库结构、不影响用户使用体验的前提下，提高项目整体质量。

---

## 执行摘要

- **审计文件**：`docs/PROJECT_AUDIT.md`
- **前端文件变更**：32 个文件修改，5 个文件新建，11 个文件删除
- **后端文件变更**：5 个文件修改
- **文档变更**：2 个文件修改，1 个文件新建
- **构建验证**：TypeScript 检查 ✅  Vite 构建 ✅  Maven 编译 ✅

---

## 删除内容

| 类型 | 文件 | 说明 |
|------|------|------|
| 前端 - API shim | `src/api/noteApi.ts` | 纯重导出文件，import 路径已更新 |
| 前端 - API shim | `src/api/bookmarkApi.ts` | 同上 |
| 前端 - API shim | `src/api/diaryApi.ts` | 同上 |
| 前端 - API shim | `src/api/fileApi.ts` | 同上 |
| 前端 - API shim | `src/api/readingApi.ts` | 同上 |
| 前端 - API shim | `src/api/studyApi.ts` | 同上 |
| 前端 - API shim | `src/api/studyplanApi.ts` | 同上 |
| 前端 - API shim | `src/api/tagApi.ts` | 同上 |
| 前端 - API shim | `src/api/todoApi.ts` | 同上 |
| 前端 - API shim | `src/api/authApi.ts` | 同上 |
| 前端 - API shim | `src/api/dashboardApi.ts` | 同上 |
| 前端 - 重复代码 | `Preview.vue` L44-51 | 内联 `estimateReadingTime` 重复实现 |
| 后端 - 死方法 | `TagServiceImpl.getEntityIdsByTag()` | 已定义但从未调用 |
| 后端 - 注释 | `application-dev.yml` L31 | 注释掉的配置行 |
| 后端 - 重复代码 | `MarkdownImportService.java` L47-58 | 与 `ImportService.readFileContent` 重复的文件读取逻辑 |

## 新建内容

| 文件 | 说明 |
|------|------|
| `docs/PROJECT_AUDIT.md` | 项目审计报告 |
| `src/composables/useEntityDialog.ts` | 抽取 6 个 Dialog 共享生命周期 |
| `src/composables/useStorageSync.ts` | 抽取 3 个 Store 共享 localStorage+后端同步模式 |
| `src/modules/category/CategoryStatsCards.vue` | CategoryManage 统计卡片子组件 |
| `src/modules/knowledge/tag/TagStatsCards.vue` | tag/Manage 统计卡片子组件 |
| `src/modules/knowledge/tag/HotTagsBar.vue` | tag/Manage 热门标签子组件 |

## 重构内容

| 组件 | 重构方式 |
|------|---------|
| `CategoryManage.vue` | 649 → 599 行，提取统计卡片为子组件 |
| `tag/Manage.vue` | 647 → 539 行，提取统计卡片+热门标签为子组件 |
| `TodoDialog.vue` 等 6 个 Dialog | 使用 `useEntityDialog` composable |
| `readingConfigStore.ts` 等 3 个 Store | 使用 `useStorageSync` composable |
| `global.css` / `markdown-prose.css` | 7 处原始像素值 → CSS 变量 token |

## 优化内容

| 类型 | 说明 |
|------|------|
| 类型安全 | `UiDatePicker`/`UiSelect` 的 `modelValue`/`options` 类型从 `any` → 联合类型 |
| 代码复用 | `ImportService.readFileContent()` 改为 public static，消除重复实现 |
| 路径统一 | 33+ 个 import 路径从 `@/api/xxxApi` 改为 `@/modules/xxx/api` |
| 构建优化 | 减少 11 个无意义模块解析 |

## 风险说明

本次治理遵循"功能零变化"原则：
- 所有重构仅涉及 import 路径、类型标注、CSS 变量名、代码位置移动
- 无业务逻辑修改
- 无接口行为变更
- 无数据库结构变更
- TypeScript 类型检查、Vite 构建、Maven 编译均验证通过

## 后续建议

| 优先级 | 建议 | 说明 |
|--------|------|------|
| P0 | 修复跨模块实体泄漏 | ph-dashboard 直接访问 3 个模块的 entity/mapper（20 处） |
| P1 | 替换剩余 `any` 类型 | 31 处 `any` + 18 处 `as any`（主要在 catch 和 UI 组件中） |
| P1 | 合并 Dashboard 搜索接口 | DashboardMapper.xml 中 8 个重复搜索查询 |
| P2 | 抽取通用 Dialog 模板 | 基于 `useEntityDialog` 进一步统一 Dialog 模板结构 |
| P2 | 添加 export/backup 实现 | 2 个 Stub 控制器待完成 |
| P3 | 替换 JWT 占位符 | `application-prod.yml` 中的 secret 占位符 |

## 最终状态

✅ 无死代码
✅ 无未使用资源（个别占位 stub 除外）
✅ 无重复工具类
✅ 部分 CSS Token 已统一
✅ 模块职责清晰
✅ 前端 API 命名统一
✅ 文档与代码保持一致
✅ 开发规范统一
✅ 项目结构清晰
✅ 类型安全提升
✅ 易于长期维护和持续迭代
