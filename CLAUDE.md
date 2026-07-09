## 项目简介

你正在协助开发 **Personal Hub**。

这是一个基于 **Spring Boot 3 + Vue 3** 的个人知识管理系统，仅供个人长期使用。

项目采用前后端分离架构：

- backend：Spring Boot 3（JDK 21）
- frontend：Vue 3 + TypeScript + Vite
- docs：项目设计文档

请始终遵循项目文档进行开发，不要自行修改架构、技术栈或编码规范。

------

# 文档说明

项目所有设计均存放于 `docs/` 目录。

开发任何功能之前，请优先阅读相关文档。

## docs/PROJECT.md

项目介绍。

包含：

- 项目定位
- 开发目标
- 功能规划
- 项目原则

任何新的功能设计，都应该符合项目定位。

------

## docs/TECH_STACK.md

项目技术栈。

包含：

- 后端技术
- 前端技术
- 开发工具
- 依赖版本

不得擅自更换框架。

新增依赖时，应尽量保持当前技术栈一致。

------

## docs/ROADMAP.md

开发路线图。

包含：

- 当前开发阶段
- 已完成功能
- 下一阶段计划
- 后续功能规划

新增功能时，应参考 Roadmap 的开发顺序。

------

## docs/DATABASE.md

数据库设计文档。

包含：

- 数据表
- 字段设计
- 表之间关系
- 数据库约束

新增业务功能时，应优先复用已有表结构。

如确需新增数据表，请同步更新 DATABASE.md。

------

## docs/API.md

接口设计规范。

包含：

- RESTful API
- 请求路径
- 请求参数
- 返回结构

新增接口时，应保持接口风格统一。

修改接口时，应同步更新 API.md。

------

## docs/STYLE_GUIDE.md

项目编码规范。

包含：

- Java 编码规范
- Vue 编码规范
- 命名规范
- 目录规范
- 注释规范
- Git Commit 规范

所有代码必须遵循该文档。

禁止出现风格不统一的代码。

------

## docs/CHANGELOG.md

项目版本记录。

每完成一个功能模块或重要修改后，请建议更新 CHANGELOG.md。

------

# 开发原则

开发过程中请遵循以下原则：

1. 优先保证代码可读性。
2. 优先保证代码可维护性。
3. 保持项目结构统一。
4. 不生成重复代码。
5. 优先复用已有代码。
6. 不随意引入新的第三方依赖。
7. 保持前后端命名一致。
8. 保持接口风格统一。
9. 保持数据库设计一致。
10. 所有新增功能应同步更新对应文档。

------

# 开发流程

对于每个新需求，请按照以下流程执行：

1. 理解需求。
2. 查阅相关 docs 文档。
3. 分析是否影响数据库、接口或现有模块。
4. 给出开发方案。
5. 待确认后再生成代码。
6. 开发完成后，提醒更新相关文档（如 API、DATABASE、CHANGELOG 等）。

未经确认，不要直接修改大量代码或重构整个项目。

------

# 输出要求

生成代码时：

- 保持模块职责清晰。
- 保持命名统一。
- 添加必要的中文注释。
- 使用当前项目已有技术方案。
- 不使用已废弃或过时的 API。
- 不生成未被使用的代码。
- 不随意修改已有业务逻辑。

如发现项目文档与代码存在冲突，请优先指出问题，并等待确认后再修改。

所有新增功能同步更新docs

<!-- superpowers-zh:begin (do not edit between these markers) -->
# Superpowers-ZH 中文增强版

本项目已安装 superpowers-zh 技能框架（20 个 skills）。

## 核心规则

1. **收到任务时，先检查是否有匹配的 skill** — 哪怕只有 1% 的可能性也要检查
2. **设计先于编码** — 收到功能需求时，先用 brainstorming skill 做需求分析
3. **测试先于实现** — 写代码前先写测试（TDD）
4. **验证先于完成** — 声称完成前必须运行验证命令

## 可用 Skills

Skills 位于 `.claude/skills/` 目录，每个 skill 有独立的 `SKILL.md` 文件。

- **brainstorming**: 在任何创造性工作之前必须使用此技能——创建功能、构建组件、添加功能或修改行为。在实现之前先探索用户意图、需求和设计。
- **chinese-code-review**: 中文 review 沟通参考——话术模板、分级标注（必须修复/建议修改/仅供参考）、国内团队常见反模式应对。仅在用户显式 /chinese-code-review 时调用，不要根据上下文自动触发。
- **chinese-commit-conventions**: 中文 commit 与 changelog 配置参考——Conventional Commits 中文适配、commitlint/husky/commitizen 中文模板、conventional-changelog 中文配置。仅在用户显式 /chinese-commit-conventions 时调用，不要根据上下文自动触发。
- **chinese-documentation**: 中文文档排版参考——中英文空格、全半角标点、术语保留、链接格式、中文文案排版指北约定。仅在用户显式 /chinese-documentation 时调用，不要根据上下文自动触发。
- **chinese-git-workflow**: 国内 Git 平台配置参考——Gitee、Coding.net、极狐 GitLab、CNB 的 SSH/HTTPS/凭据/CI 接入差异与镜像同步配置。仅在用户显式 /chinese-git-workflow 时调用，不要根据上下文自动触发。
- **dispatching-parallel-agents**: 当面对 2 个以上可以独立进行、无共享状态或顺序依赖的任务时使用
- **executing-plans**: 当你有一份书面实现计划需要在单独的会话中执行，并设有审查检查点时使用
- **finishing-a-development-branch**: 当实现完成、所有测试通过、需要决定如何集成工作时使用——通过提供合并、PR 或清理等结构化选项来引导开发工作的收尾
- **mcp-builder**: MCP 服务器构建方法论 — 系统化构建生产级 MCP 工具，让 AI 助手连接外部能力
- **receiving-code-review**: 收到代码审查反馈后、实施建议之前使用，尤其当反馈不明确或技术上有疑问时——需要技术严谨性和验证，而非敷衍附和或盲目执行
- **requesting-code-review**: 完成任务、实现重要功能或合并前使用，用于验证工作成果是否符合要求
- **subagent-driven-development**: 当在当前会话中执行包含独立任务的实现计划时使用
- **systematic-debugging**: 遇到任何 bug、测试失败或异常行为时使用，在提出修复方案之前执行
- **test-driven-development**: 在实现任何功能或修复 bug 时使用，在编写实现代码之前
- **using-git-worktrees**: 当需要开始与当前工作区隔离的功能开发，或在执行实现计划之前使用——通过原生工具或 git worktree 回退机制确保隔离工作区存在
- **using-superpowers**: 在开始任何对话时使用——确立如何查找和使用技能，要求在任何响应（包括澄清性问题）之前调用 Skill 工具
- **verification-before-completion**: 在宣称工作完成、已修复或测试通过之前使用，在提交或创建 PR 之前——必须运行验证命令并确认输出后才能声称成功；始终用证据支撑断言
- **workflow-runner**: 在 Claude Code / OpenClaw / Cursor 中直接运行 agency-orchestrator YAML 工作流——无需 API key，使用当前会话的 LLM 作为执行引擎。当用户提供 .yaml 工作流文件或要求多角色协作完成任务时触发。
- **writing-plans**: 当你有规格说明或需求用于多步骤任务时使用，在动手写代码之前
- **writing-skills**: 当创建新技能、编辑现有技能或在部署前验证技能是否有效时使用

## 如何使用

当任务匹配某个 skill 时，使用 `Skill` 工具加载对应 skill 并严格遵循其流程。绝不要用 Read 工具读取 SKILL.md 文件。

如果你认为哪怕只有 1% 的可能性某个 skill 适用于你正在做的事情，你必须调用该 skill 检查。
<!-- superpowers-zh:end -->
