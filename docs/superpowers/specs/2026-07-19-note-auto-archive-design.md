# 笔记手动归档（AUTO_ARCHIVE）设计

> 日期：2026-07-19 · 方案 A · 已落地

## 行为

- 新增「归档」：软删进回收站，`delete_reason=AUTO_ARCHIVE`
- 现有「移入回收站」仍写 `USER_DELETE`
- 恢复 / 永久删除共用现有逻辑
- 回收站展示：`USER_DELETE`→用户删除，`AUTO_ARCHIVE`→归档

## API

`POST /api/notes/{id}/archive`（需登录）

## 代码

- 枚举 `NoteDeleteReason`：`USER_DELETE` / `AUTO_ARCHIVE`
- `NoteService.archive` + 内部统一 `softDelete(..., reason)`
- 前端：列表右键归档；`archiveNote`；回收站 `deleteReasonLabel`
