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
| 新增后端模块 | PROJECT.md / DATABASE.md |
| 新增 API 接口 | API.md |
| 新增数据库表/字段 | DATABASE.md + **sql/init.sql** |
| 功能阶段完成 | PROJECT.md / CHANGELOG.md |
| UI 组件/工具函数 | STYLE_GUIDE.md |
| 项目进度推进 | PROJECT.md |
| 代码质量/架构调整 | STYLE_GUIDE.md |
| 功能新增/项目结构变化 | **README.md** |
| 模块能力变更 | **personal-hub-server/README.md** + **personal-hub-web/readme.md** |
| CLAUDE.md 含架构细节 | 移入 STYLE_GUIDE.md 对应章节 |

### 3. 逐个更新

**PROJECT.md** — 功能阶段完成时在「开发路线图」追加新阶段行；新增步骤在「开发历史」追加；模块依赖变化更新架构图
**DATABASE.md** — 追加阶段标题 + 表结构表格 + ER + 索引（沿用现有格式）
**sql/init.sql** — 有表结构变更时，将新表 DDL 追加到 init.sql，保持与已有表一致的注释/索引风格
**API.md** — 追加接口表格 + 请求/响应 JSON 示例
**CHANGELOG.md** — 追加到 [Unreleased] 当前日期条目下：`- **功能名**：变更`
**STYLE_GUIDE.md** — 共享组件表/工具函数表追加新条目
**CLAUDE.md** — 保持简洁（简介+索引+流程），不重复 docs/ 内容
**README.md** — 功能清单表追加新行、项目结构变更同步更新、快速启动说明保持最新
> ⚠️ **README.md 是 GitHub 首页门面**。修改时必须保持高级、专业、精致的调性——排版工整、用词精准、无冗余、无口语化。不为凑字数加内容，不为省事减质量。
**personal-hub-server/README.md** — 模块说明变更时同步更新
**personal-hub-web/readme.md** — 模块说明变更时同步更新

### 4. 验证

- `git diff --stat` 检查范围
- 确认每个受影响文档已更新
- 确认 CLAUDE.md 不包含 docs/ 已有内容

## 原则

精简压缩 | 不重复（CLAUDE.md 只做索引） | 沿用现有格式 | 只改受影响的文档
