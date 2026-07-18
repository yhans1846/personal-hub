# 文件模块预览改造（方案 B）

> 日期：2026-07-18  
> 状态：已落地

## 目标

文件列表内嵌预览：图片 / PDF / 文本·Markdown；Office 与压缩包仅下载。修复下载鉴权（弃用裸 `window.open`）。

## 范围

| 纳入 | 排除 |
|------|------|
| 点击卡片打开预览层 | Office 在线转换 |
| 图片：缩略图 + ImageLightbox | 视频播放 |
| PDF：iframe + blob | 独立全屏预览路由 |
| txt/md：文本拉取 + 对话框渲染 | |
| 不可预览类型：提示 + 下载 | |
| axios blob 下载（带 JWT） | |
| 类型筛选与后端扩展名对齐 | |

## 组件

- `FilePreviewDialog.vue`：按类型切换图片 / PDF iframe / 文本区 / 不支持态
- 复用 `ImageLightbox`、`getFilePreviewUrl`
- `downloadFileBlob(id, name)`：blob → `<a download>`

## 后端

- 已有 `GET /api/files/{id}/preview|download`，一般无需改
- 列表 `type` 筛选：支持分组值 `image|pdf|doc|archive` → 扩展名 IN 查询（可选本轮）

## 成功标准

- 图片不新开窗口，Lightbox 可看
- PDF/文本在弹层内可读
- 下载带鉴权成功
- Office/zip 点预览有明确「请下载」

## 参考

日记配图已改为独立资源包（`getDiaryImagePreviewUrl`），与本文件模块预览分离。
