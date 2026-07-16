# Personal Hub - 项目审计报告

> 2026-07-13 · 范围：server + web 全量

---

## 项目整体评分

| 维度 | 分 | 说明 |
|------|----|------|
| 目录结构 | ★★★★★ | 前后端领域对齐 |
| 代码质量 | ★★★★☆ | any / 跨模块泄漏为主 |
| 文档 | ★★★★☆ | 齐全，需与代码同步 |
| 规范统一 | ★★★☆☆ | API 双目录、裸像素、重复模式 |
| 技术债 | ★★★☆☆ | any≈31、实体泄漏≈20 |

---

## 一、技术债统计

### TODO / FIXME
无真实 TODO；`TODO_OVERDUE` 为业务常量名。

### console / println
无 `console.log` / `System.out`；7 处 `console.warn` 为 store fallback，可保留。

### any 类型（前端）
约 **31+** 处 + 18 处 `as any`。集中：UiDatePicker/UiSelect、各 Store、Editor、StatsView、若干 catch。

### debugger
0。

### 注释掉的代码
`application-dev.yml` 中 MyBatis stdout 注释可删。

---

## 二、大文件统计

### 超过 300 行
CategoryManage 649 · tag/Manage 647 · AppLayout 573 · ImportMarkdownDialog 549 · Editor 536 · Preview 535 · DiaryDialog 516 · ReadingDialog 514 · ReadingExperience 474 · Dashboard 384 · StudyPlanDialog 346 · DataManagement 315 · PreviewToc 308 · DashboardServiceImpl 305

### 超过 500 / 800 行
>500：上表前 8 个前端文件｜>800：**0**

### 拆分建议
大 Manage/AppLayout/Dialog 拆统计区+列表+拖拽；Import/Diary/Reading 抽子组件/composable；Editor/Preview 已有子目录可维持。

---

## 三、重复代码分析

### 3.1 前端 API 层双目录（高）
`src/api/*Api.ts` 共 11 个 3 行 re-export → `modules/*/api`，import 路径不统一。

### 3.2 Dialog 生命周期（中）
Todo/Diary/Bookmark/Reading/Study/StudyPlan 同构 `modelValue`+`entityId`+watch 加载 → 可抽 `useEntityDialog`。

### 3.3 Store 双存储（中）
layout / readingConfig / notificationConfig：localStorage + 后端 + fallback → 可抽公共 composable。

### 3.4 `estimateReadingTime`（中）
`utils/readingTime.ts`（350 cpm）与 Preview 内联（500）不一致，应统一。

### 3.5 CSS 裸像素（低）
`global.css` / `markdown-prose.css` 若干 padding 未用 `--sp-*`。

### 3.6 后端（低）
Import 读 MultipartFile 重复；DashboardMapper 搜索 SQL 模板重复。

---

## 四、无用代码分析

### 4.1 前端
无死组件/Hook/Type/Store；⚠️ 11 个 API shim；assets 空。

### 4.2 后端
`TagService.getEntityIdsByTag` 未调用；`JwtUtil` 未用 import。

### 4.3 Stub
ExportController / BackupController 501 空实现；`importLayout` 仅打日志。

---

## 五、架构问题（跨模块依赖泄漏）

### 5.1 后端实体泄漏（高）
约 **20** 处：dashboard→knowledge/planning/resource；planning/resource→knowledge；NotificationMapper SQL 跨域。

### 5.2 模块职责边界
common/system/knowledge/storage/boot ✅｜planning/resource ⚠️｜**dashboard ❌ 严重泄漏**

---

## 六、配置与安全

| 问题 | 严重度 |
|------|--------|
| prod JWT secret 占位 | 高 |
| Storage 抛 RuntimeException | 中 |
| ExportRequest 缺 @Valid | 低 |

---

## 七、总结

### 关键发现优先级

| 级 | 问题 |
|----|------|
| P0 | 跨模块实体泄漏 ~20 |
| P1 | any ~31；API shim ×11 |
| P2 | >500 行文件；Dialog/Store 重复 |
| P3 | JWT 占位；裸像素；stub 控制器 |

### 项目亮点
无 console.log/println/debugger/FIXME；无 >800 行文件；Slf4j；Pinia Composition；目录与领域对齐。
