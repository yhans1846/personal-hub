# Personal Hub - 项目介绍

> 职责：定位、模块架构、原则、路线图。版本见 `TECH_STACK.md`，变更见 `CHANGELOG.md`。

## 定位

个人知识管理系统，前后端分离。非企业后台。  
设计气质见 `STYLE_GUIDE.md`。

## 架构

```
Vue 3 → REST → Spring Boot 3 → MyBatis-Plus → MySQL
（登录验证码等短态数据 → Redis）
```

```
后端                         前端 modules/
ph-boot / ph-common          —
ph-system                    system/
ph-biz（knowledge/planning/  knowledge/（含 category）· planning/ · resource/
  resource/dashboard 包）     dashboard/ · search/ · stats/ · system/
```

**划分：** Maven 粗粒度（common / system / biz / boot）· 细领域用 Java package · 跨包走 Service · Dashboard 只聚合不落业务库。  
**通知：** system 定义 `PlanningNotificationSource`，biz 实现扫描；system 只写 `sys_notification`。  
**存储：** `StorageService` 无 Web 类型；业务经 `FileAssetService` + `StoragePaths`。  
**备份：** `ph-biz` 的 `DataBackupService` 导出/导入用户 ZIP（业务表 + 文件 + 资料/头像）；不含密码与通知。

```
ph-common ← ph-system ← ph-biz ← ph-boot
```

## 原则

可读优先 · 命名/结构统一 · 领域对齐 · 公共沉 common · 慎加依赖 · 前后端命名一致 · RESTful + Result · 表设计一致 · 改代码先改文档

## 文档分工

| 文档 | 写什么 |
|------|--------|
| `TECH_STACK.md` | 版本与环境 |
| `API.md` | HTTP 契约 |
| `DATABASE.md` | 表结构（对齐 `sql/init.sql`） |
| `STYLE_GUIDE.md` | 编码 + Token + 组件目录 |
| `PAGE_SPEC.md` | 页面形态与交互契约 |
| `DEPLOYMENT.md` | 部署运维 |
| `CHANGELOG.md` | 按日变更 |

## 路线图

| 阶段 | 内容 |
|------|------|
| 一–三 | 认证/笔记/学习/待办/文件；日记/收藏/计划/阅读；Dashboard/统计/搜索/标签 |
| 四–九 | UI/通知/布局；Ui 组件+分类；配置；回收站+审计；预览 |
| 十–十七 | 编辑器；设置阅读配置；Dialog；统计；Profile；Focus；部署；Vditor |
| 十八 | ph-biz 合并；列表一屏铺满；设置标签云；PAGE_SPEC 收口 |
| 十九 | V2 项目治理（死代码清理 · DRY · 目录对齐 · 文档归档） |
| 二十 | Dialog 编辑器气质；文件预览/分类；日记配图资源包；未用字段清理；登录审计 |
| 二十一 | 后端质量加固；笔记手动归档；用户数据 ZIP 备份/恢复（含资料与头像） |

## 后续可扩展

| 功能 | 优先级 |
|------|--------|
| AI 总结/问答 · OCR · 双向链接 | 低 |
| Mermaid 图表 · KaTeX 公式（Vditor 正式能力） | ✅ |
| 日记历史配图迁移 · 笔记附件进统一资源索引 | 低 |
| 自动备份 / 备份历史列表（服务端落盘 · 可回滚） | ✅ |
| MD 批量导出（勾选 ZIP）· 回收站清空 | ✅ |
| 跨模块检索（title+excerpt · 每组 20 · 标题优先） | ✅ |
| 命令面板快捷捕获（Ctrl+K 新建） | ✅ |
| 数据备份（手动 ZIP）· Docker 部署 · 文件 PDF/文本预览 | ✅ |

## 开发历史（摘要）

| 步骤 | 内容 |
|------|------|
| 1–14 | 脚手架 → 笔记/学习/Todo/文件 → 日记/收藏/计划/阅读 |
| 15–27 | 标签/Dashboard/通知/布局/Ui/分类/配置 |
| 28–37 | 回收站/预览/JWT query/Dialog/统计/Profile/Focus |
| 38–40 | ph-biz；fill 列表；规范收口 |
| 41 | V2 治理；Dialog 积木；文件预览；日记独立配图目录 |
| 42 | 存储/通知解耦；笔记归档；备份/恢复 ZIP |
