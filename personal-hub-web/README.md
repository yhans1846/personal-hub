# Personal Hub Web

Personal Hub 前端 — Vue 3 + TypeScript + Vite + Element Plus。

## 技术栈

- **框架**: Vue 3（Composition API, `<script setup>`）
- **构建**: Vite + TypeScript
- **UI**: Element Plus（通过 Ui-* 组件封装）
- **状态管理**: Pinia
- **路由**: Vue Router 4
- **HTTP**: Axios
- **图标**: lucide-vue-next
- **Markdown**: Vditor（IR 模式）
- **图片缩放**: medium-zoom
- **图表**: ECharts
- **拖拽**: sortablejs

## 目录结构

```
src/
├── api/              # API 接口封装（按模块聚合）
├── components/
│   ├── ui/           # Ui-* 基础组件
│   └── index.ts      # 共享组件导出（PageHeader / ListToolbar / EmptyState …）
├── composables/      # useMainContentFill / useFillPageSize / useProductViewMode / useDeepLinkDialog
├── modules/          # 领域模块
│   ├── category/     # 统一分类管理
│   ├── system/       # 登录 / 系统设置（工作台标签云）
│   ├── knowledge/    # 笔记 / 回收站 / 预览 / 日记 / 学习 / 阅读 / 标签
│   ├── planning/     # Todo / 学习计划
│   ├── resource/     # 收藏夹 / 文件
│   ├── dashboard/    # Dashboard（fill + Bento）
│   ├── search/       # 全局搜索
│   └── stats/        # 数据统计（折叠分组）
├── router/           # 路由（列表页 hideBreadcrumb）
├── store/            # Pinia
├── styles/           # global / product-list / markdown-prose
├── types/
└── utils/
```

## 启动

```bash
pnpm install
pnpm dev        # 开发模式 → http://localhost:3000
pnpm build      # 构建生产包
pnpm preview    # 预览生产包
```

## 文档

项目文档见 `docs/` 目录。
