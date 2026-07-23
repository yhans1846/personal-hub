# 全站 UI 字体切换（思源黑体默认）

> 日期：2026-07-23 · 已确认（对话）  
> 范围：全站 UI `--font-sans`；外观设置可选预置字体。  
> 非目标：阅读/Markdown 正文字体联动、自定义上传字体、改代码等宽栈。

## 1. 目标

- 默认界面字体为**思源黑体**（Source Han Sans / Noto Sans SC）。
- 系统设置 → 外观提供若干热门字体切换，即时全站生效并持久化。
- **阅读体验**（字号/主题/Markdown）保持独立，不跟界面字体。

成功标准：

1. 新用户 / 恢复默认 → 思源黑体。
2. 外观切换字体后全站 UI（侧栏、列表、设置、Overlay）立即变；刷新仍保留。
3. 阅读预览/笔记 Markdown 正文字体不受 `uiFont` 影响。
4. 未选中的 Web 字体不打进首包关键路径（按需加载）。

## 2. 预置

| key | 显示名 | 栈 / 资源 |
|-----|--------|-----------|
| `source-sans` | 思源黑体 | **默认**；Noto Sans SC / Source Han Sans SC woff2 |
| `source-serif` | 思源宋体 | Noto Serif SC woff2 |
| `lxgw-wenkai` | 霞鹜文楷 | LXGW WenKai woff2 |
| `inter` | Inter | Inter + 系统中文回退 |
| `system` | 系统默认 | 现有系统栈，不加载 Web 字体 |

等宽：`--font-mono` 不变。

## 3. 数据与应用

- `AppearanceConfig` 增 `uiFont: string`（上表 key；非法 → `source-sans`）。
- `themeStore.saveAppearanceConfig` / `resetAppearanceConfig` / 启动 hydrate 同步。
- 应用：`document.documentElement` 设 `--font-sans: <stack>` + `data-ui-font`；选中 Web 字体时按需注入 `<link rel="stylesheet">`（已加载则跳过）。
- 字体来源（实现）：CDN 样式表（Bunny Fonts / jsDelivr `@fontsource`），避免首包打入大体积 CJK woff2；`system` 不注入。后续若需离线可改为自托管 `public/fonts/`。

## 4. UI

外观 Tab「界面字体」区块：选项按钮或小卡片，样例文案「个人知识库 Personal Hub」；当前项高亮。

## 5. 文档与验收

- `PAGE_SPEC` / `STYLE_GUIDE` / `CHANGELOG` 各补一句。
- 手测：五种切换；恢复默认；阅读页字体不变；弱网下 system 仍可用。

## 6. 实现顺序

1. 类型 + store + CSS 变量应用  
2. 字体文件与按需加载  
3. AppearanceSettings UI  
4. 文档  

实现前可用 `writing-plans` 拆任务。
