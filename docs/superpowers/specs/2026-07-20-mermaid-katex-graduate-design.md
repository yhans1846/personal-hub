# Mermaid / KaTeX 毕业为正式能力

> 日期：2026-07-20 · 方案 A（始终开启）  
> 范围：产品定位与前端一致性。后端、存储格式、vditor CDN 策略不变。

## 目标

将已接入的 Mermaid 图表与 KaTeX 公式从「实验功能」毕业为正式笔记能力：插入常显、渲染常开、设置页不再提供开关，文档与路线图同步。

## 背景

- Vditor IR / 笔记预览已支持 ` ```mermaid ` 与 `$` / `$$`（`mathBlockPreview` + `codeBlockPreview` + 本地 `/vditor` CDN）。
- 实验开关仅隐藏右键菜单项，不关闭渲染；且 Vditor 无法单独关闭 Mermaid 而不影响代码块预览。
- 文件模块 `FilePreviewDialog` 未走共享预览配置，md 中图/公式可能渲染不完整。

## 设计

| 项 | 约定 |
|----|------|
| 产品定位 | 正式能力；默认始终可用，无用户开关 |
| 实验功能 | 仅保留 AI 助手、双向链接（即将推出） |
| FeatureFlags | 移除 `mermaid` / `katex`；加载旧 localStorage 时静默丢弃这两键 |
| 右键菜单 | 无选区：常显 Mermaid / 块级公式 / 行内公式；有选区：可「包成行内公式」 |
| 渲染 | 笔记编辑 IR、笔记预览、侧栏预览保持现状（常开） |
| 文件预览 | `FilePreviewDialog` 对 markdown 使用 `buildPreviewOptions`（含 `cdn` + math/code preview） |
| 重置文案 | 实验区「关闭全部」改为「恢复默认」（仅影响仍留在实验区的项） |
| 不做 | 开关同步 `user_layout`；独立 Mermaid/KaTeX 配置页；拆掉 knip ignore 的直连依赖清理（可选后续） |

## 文档

- `PROJECT.md`：低优路线图去掉 Mermaid/KaTeX
- `PAGE_SPEC.md`：笔记编辑补 Mermaid / 公式插入与预览说明；高级实验功能仅 AI / 双向链接
- `CHANGELOG.md`：当日条目

## 验收

1. 设置 → 高级 → 实验功能：无 Mermaid/KaTeX 开关  
2. 笔记右键可插入图与公式；IR 与预览页正确渲染  
3. 选中文本 → 行内公式包裹生效  
4. 文件模块预览含 mermaid/公式的 `.md` 与笔记预览一致  
5. 旧用户 localStorage 含 `mermaid`/`katex` 不报错、不影响实验区剩余开关  
