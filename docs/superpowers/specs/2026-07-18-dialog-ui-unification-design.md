# 对话框 UI 统一（方案 B）

> 日期：2026-07-18  
> 范围：**除笔记外**全部业务 Dialog；笔记 Full Page / 导入弹窗不动  
> ProfileDrawer 仅对齐 Token，不改成 Dialog

## 目标

业务弹窗同一壳、同一分区节奏、同一主次按钮；字段内容可不同。

## 组件

| 组件 | 职责 |
|------|------|
| `UiDialog` | 壳：`size` sm/md/lg · title/subtitle · Body 滚 · Footer 槽（无内容不渲染边框区） |
| `DialogSection` | 可选 label + 默认槽 |
| `DialogDivider` | 分割线 |
| `DialogFooterActions` | 取消 + 保存（`UiButton`） |
| `ImageLightbox` | 居中预览（已有） |

## 约定

1. 业务页禁止裸 `el-dialog`（笔记除外）
2. Footer 禁止 `el-button`，用 `UiButton` / `DialogFooterActions`
3. 宽：`sm=520` · `md=720`(默认) · `lg=860`
4. 表单分区用 `DialogSection` / `DialogDivider`，消灭各页复制的 `.section-label` / `.section-divider`（可渐进替换）
5. 确认框本轮可暂留 `ElMessageBox`，后续再换

## 迁移清单

待办 · 日记 · 学习计划 · 阅读 · 学习记录 · 收藏 · 标签 · 分类

## 不做

笔记编辑页 · 笔记导入 · Form Schema 生成器
