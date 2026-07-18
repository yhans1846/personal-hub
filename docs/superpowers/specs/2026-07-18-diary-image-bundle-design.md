# 日记配图资源包 + 文件模块边界（方案 A）

> 日期：2026-07-18  
> 状态：已实现

## 目标

- 日记配图对齐笔记：独立目录落盘，**不进** `file_resource`
- 文件管理页**只**管理本页 `/api/files/upload` 上传的文件
- 避免「在文件页删除 → 日记裂图」

## 范围

| 纳入 | 排除 |
|------|------|
| `diaries/{diaryId}/images/` 存储与读写 API | 历史 `image_file_ids`（数字 file id）迁移 |
| 字段改为存文件名列表 | 日记附件（非图片） |
| 前端日记弹窗/列表改走日记图片 API | 笔记存储改造 |
| 删日记时清理配图目录（与笔记删盘策略对齐） | 文件模块按「来源」分类筛选 |
| 文档：DATABASE / API / CHANGELOG | |

**历史数据：** 明确不做迁移；旧日记若仍存数字 id，新逻辑下配图视为无效/空，可接受。

## 存储

```
{storage.location}/
├── notes/{noteId}/images|attachments/   ← 已有，不变
├── diaries/{diaryId}/images/{uuid}.ext  ← 新增
└── uploads/...                          ← 仅文件模块
```

- 上传返回相对名：`{uuid}.ext`（或 `images/{uuid}.ext`，实现时与笔记返回约定对齐，库内统一只存文件名）
- 读：`GET /api/diaries/{id}/images/{filename}`（鉴权：日记归属当前用户）
- 删单图：`DELETE /api/diaries/{id}/images/{filename}`，并同步更新日记记录中的文件名列表
- 路径穿越：禁止 `..`、禁止斜杠；仅允许已登记文件名

## 数据模型

- 列：`diary_entry.image_file_ids` **改名为** `image_files`（TEXT，JSON 字符串数组）
  - 例：`["a1b2c3.png","d4e5f6.jpg"]`
  - 顺序即展示顺序；首张作封面（与现行为一致）
- Entity / DTO / VO：`List<String> imageFiles`（替换 `List<Long> imageFileIds`）
- 迁移脚本：`ALTER ... CHANGE image_file_ids image_files ...`（不做内容转换）
- `init.sql` 同步

## API

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/diaries/{id}/images` | multipart 上传；须已有 diaryId |
| GET | `/api/diaries/{id}/images/{filename}` | 预览/缩略图（可 blob） |
| DELETE | `/api/diaries/{id}/images/{filename}` | 删文件 + 更新 `image_files` |
| CRUD | `/api/diaries` | create/update 的 body 带 `imageFiles: string[]` |

日记 CRUD 不再接收 / 返回数字 file id。

## 前端

- `DiaryDialog`：有 `id` 后才允许上传；新建流程为「先保存拿到 id → 再传图 → 可再保存顺序」或保存成功后自动进入可传图状态（实现计划二选一并写死）
- 列表缩略图 / Lightbox：改请求日记图片 URL，不再 `getFilePreviewUrl(fileId)`
- 拖拽排序：仍只改 `imageFiles` 顺序并 PATCH/PUT 日记
- 文件模块：无改行为目标（本就只列 `file_resource`）；确认日记不再调用 `uploadFile`

## 删除与一致性

- 删日记（逻辑删）：删除 `diaries/{id}/` 目录（推荐本轮做，避免孤儿文件）；若笔记删除目前不删盘，则日记与笔记对齐——**实现前对照 `NoteServiceImpl` 删盘行为，日记与之保持一致**
- 文件模块 `DELETE /api/files/{id}`：不影响日记目录

## 成功标准

1. 日记新上传图片不出现在文件管理列表
2. 文件管理删除任意文件，不影响新日记配图显示
3. 日记弹窗可上传、排序、删除配图；列表封面/缩略图正常
4. 无 diaryId 时不能上传（新建须先落库）
5. DATABASE / API / CHANGELOG 已更新

## 参考

- 笔记：`NoteFileServiceImpl` · `notes/{id}/images`
- 现行日记：`image_file_ids` + `/api/files`
