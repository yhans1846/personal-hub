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
- **Markdown**: md-editor-v3
- **图表**: ECharts
- **拖拽**: sortablejs

## 目录结构

```
src/
├── api/              # API 接口封装（按模块聚合）
├── components/
│   ├── ui/           # Ui-* 基础组件（UiDialog/UiInput/UiButton 等 8 个）
│   └── index.ts      # 共享组件导出（PageHeader/EmptyState/StatCard 等）
├── modules/          # 领域模块
│   ├── category/     # 统一分类管理（Tab 切换）
│   ├── system/       # 登录 / 系统设置（布局自定义）
│   ├── knowledge/    # 笔记 / 日记 / 学习记录 / 阅读记录 / 标签
│   ├── planning/     # Todo / 学习计划
│   ├── resource/     # 收藏夹 / 文件管理
│   ├── dashboard/    # Dashboard 首页
│   ├── search/       # 全局搜索
│   └── stats/        # 数据统计趋势图
├── router/           # 路由配置
├── stores/           # Pinia（auth / theme / layout / notification）
├── styles/           # 全局样式 / CSS 设计令牌 / 深色模式 / 5 种强调色
├── types/            # TypeScript 类型定义
└── utils/            # 工具函数
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
