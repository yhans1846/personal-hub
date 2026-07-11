---
name: update-docs
description: 更新 Personal Hub 项目文档。当用户要求更新文档、同步进度、整理 docs、添加 changelog、或在提交前需要同步文档时触发。也用于将 CLAUDE.md 中的内容梳理到 docs/ 对应文件中。
---

# 更新项目文档

同步项目 docs/ 下的文档以反映最新代码变更和项目进度。

## 工作流程

### 1. 了解当前状态

读取以下信息：
- `git log --oneline -10`
- 可能改动涉及的模块/功能（从对话或 diff 中获取）
- 所有 docs/ 文件快速浏览最新内容

### 2. 匹配变更与文档

| 变更类型 | 需更新文档 |
|----------|-----------|
| 新增后端模块 | PROJECT.md / DATABASE.md / DEVELOPMENT_PLAN.md |
| 新增 API 接口 | API.md |
| 新增数据库表/字段 | DATABASE.md + **sql/init.sql** |
| 功能阶段完成 | ROADMAP.md / CHANGELOG.md |
| UI 组件/工具函数 | STYLE_GUIDE.md |
| 项目进度推进 | DEVELOPMENT_PLAN.md / PROJECT.md |
| 代码质量/架构调整 | STYLE_GUIDE.md |
| 功能新增/项目结构变化 | **README.md** |
| CLAUDE.md 含架构细节 | 移入 STYLE_GUIDE.md 对应章节 |

### 3. 逐个更新

**PROJECT.md** — 功能规划中新增已完成阶段，模块依赖图加连线
**ROADMAP.md** — 新增「当前阶段：第X阶段 ✅」列出完成项
**DATABASE.md** — 追加阶段标题 + 表结构表格 + ER + 索引（沿用现有格式）
**sql/init.sql** — 有表结构变更时，将新表 DDL 追加到 init.sql，保持与已有表一致的注释/索引风格
**API.md** — 追加接口表格 + 请求/响应 JSON 示例
**CHANGELOG.md** — 追加到 [Unreleased] 当前日期条目下：`- **功能名**：变更`
**DEVELOPMENT_PLAN.md** — 进度总览表追加 Step 行
**STYLE_GUIDE.md** — 共享组件表/工具函数表追加新条目
**CLAUDE.md** — 保持简洁（简介+索引+流程），不重复 docs/ 内容
**README.md** — 功能清单表追加新行、项目结构变更同步更新、快速启动说明保持最新

### 4. 验证

- `git diff --stat` 检查范围
- 确认每个受影响文档已更新
- 确认 CLAUDE.md 不包含 docs/ 已有内容

## 原则

精简压缩 | 不重复（CLAUDE.md 只做索引） | 沿用现有格式 | 只改受影响的文档
