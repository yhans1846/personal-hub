# 跨模块检索升级

> 日期：2026-07-20 · 已批准  
> 范围：P0-B。不扫磁盘 `note.md`、不上 ES/向量。

## 目标

- 笔记列表/回收站 keyword 与全局一致：`title OR excerpt`
- 全局搜索每模块上限 10→20；标题命中优先于正文/摘要命中

## 变更

| 位置 | 变更 |
|------|------|
| `NoteMapper.xml` selectNotePage / selectRecyclePage | keyword → title OR excerpt |
| `DashboardMapper.xml` 全部 search* | LIMIT 20；`ORDER BY` 标题命中优先 |
| 笔记列表 placeholder | 「搜索笔记标题或摘要…」 |
| API / CHANGELOG | 同步说明 |

## 不做

全文索引、语义检索、附件/文件内容搜索、读盘搜正文。
