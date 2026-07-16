# Personal Hub - 项目介绍

## 定位
个人知识管理系统，Spring Boot 3 + Vue 3 前后端分离。

## 设计理念
> Notion 简洁 + Linear 精致 + Raycast 现代 + Apple 克制  

非企业后台。详见 `STYLE_GUIDE.md`。

## 架构

```
Vue 3 + Vite + TS → Axios → REST → Spring Boot 3 → MyBatis-Plus → MySQL
```

## 目录

```
personal-hub-server/ · personal-hub-web/ · sql/ · docs/ · README.md
```

## 领域模块设计

```
后端 ph-*                    前端 modules/
ph-boot / ph-common          —
ph-system                    system/
ph-knowledge                 knowledge/ + category/
ph-planning                  planning/
ph-resource                  resource/
ph-dashboard                 dashboard/ + search/
```

### 划分原则
按领域聚合｜跨模块走 Service｜Dashboard 只聚合不落业务库

## 依赖关系

```
ph-system → ph-knowledge / ph-planning / ph-resource / ph-dashboard
```

## 项目原则

1. 可读性优先 2. 命名/结构统一 3. 领域对齐 4. 公共沉 common  
5. 慎加依赖 6. 前后端命名一致 7. RESTful + Result 8. 表设计一致  
9. 纯函数优先 10. 改代码先改文档

---

# 开发路线图

## 已完成 ✅

| 阶段 | 内容 |
|------|------|
| 一–三 | 认证/笔记/学习/待办/文件；日记/收藏/计划/阅读；Dashboard/统计/搜索/标签 |
| 四–六 | UI/通知；工作台布局；Ui 组件 + Dialog + 统一 category |
| 七–九 | 配置分层；回收站+审计；预览阅读体验 |
| 十–十二 | 编辑器 UI；阅读配置进设置+JWT query；统一 Dialog；统计 V4 |
| 十三–十五 | Profile+待办+外观；Focus Mode |
| 十六–十七 | 部署/安全/性能；Vditor WYSIWYG |

## 后续可扩展

| 功能 | 优先级 |
|------|--------|
| AI 总结/问答 · OCR · 双向链接 · 附件 · Mermaid/KaTeX | 低 |
| PDF 预览 · MD 导出 · 数据备份 | 中 |
| Docker 部署 | ✅ 见 DEPLOYMENT.md |

---

# 开发历史

| 步骤 | 内容 | 阶段 |
|------|------|------|
| 1–10 | 脚手架/DB/Swagger；笔记/学习；多模块；质量统一；Todo/文件 | 一 |
| 11–14 | 日记/收藏；计划/阅读 | 二 |
| 15–18 | 统一标签；Dashboard/趋势/搜索 | 三 |
| 19–21 | 通知；UI/功能增强 | 四 |
| 22–22b | 工作台布局；Bento 六宫格+外部快捷 | 五 |
| 23–27 | CRUD Ui+分类合并；清理/配置/Swagger | 六–七 |
| 28–30 | 回收站+审计；预览体验；阅读配置+JWT query | 八–十一 |
| 31–36 | 统一 Dialog；统计 V4；Profile/待办/外观 | 十一–十三 |
| 37 | Focus Mode | 十五 |
