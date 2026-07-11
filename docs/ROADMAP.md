# Personal Hub - 开发路线图

## 当前状态：所有阶段已完成 ✅

| 阶段 | 内容 | 状态 |
|------|------|------|
| 一（MVP） | 用户认证、笔记、学习记录、待办、文件管理 | ✅ |
| 二 | 日记、收藏夹、学习计划、阅读记录 | ✅ |
| 三 | Dashboard、数据统计、全局搜索、统一标签 | ✅ |
| 四 | UI 优化、功能增强、通知系统 | ✅ |
| 五 | 工作台自定义布局（菜单/Dashboard 卡片自定义） | ✅ |
| 六 | CRUD UI 优化 + 分类系统合并 | ✅ |

### 第六阶段详情
- 统一 UI 组件库（UiDialog/UiInput/UiSelect 等 8 个）
- 所有 CRUD 改为 Dialog/Drawer 交互模式（Todo/Bookmark/日记/Drawer 等）
- 分类系统合并：note_category/bookmark_category/file_category → 统一 `category` 表
- 废弃 note_tag 表及代码清理（已由统一 tag 替代）
- 分类管理页面 Tab 切换（笔记/收藏夹/文件）

## 后续可扩展

| 功能 | 优先级 | 说明 |
|------|--------|------|
| AI 笔记总结/问答 | 低 | 接入大模型 |
| OCR 图片识别 | 低 | 文字提取 |
| PDF 在线预览 | 中 | 浏览器端 |
| MD 导出 PDF/HTML | 中 | |
| 数据备份 | 中 | 定时备份 |
| WebDAV 同步 | 低 | 文件同步 |
| Docker 部署 | 中 | 容器化 |
