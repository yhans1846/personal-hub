# Personal Hub Web

Personal Hub 前端 — Vue 3 + TypeScript + Vite。

## 技术栈

- **框架**: Vue 3 (Composition API, `<script setup>`)
- **构建**: Vite + TypeScript
- **UI**: Element Plus
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
├── api/              # API 接口封装
├── components/       # 共享组件（PageHeader / EmptyState / StatCard 等）
├── modules/          # 领域模块
│   ├── system/       # 登录
│   ├── knowledge/    # 笔记 / 日记 / 学习记录 / 阅读记录 / 标签
│   ├── planning/     # Todo / 学习计划
│   ├── resource/     # 收藏夹 / 文件管理
│   ├── dashboard/    # Dashboard 首页
│   └── search/       # 全局搜索
├── router/           # 路由配置
├── store/            # Pinia 状态
├── styles/           # 全局样式 / 主题变量
├── types/            # TypeScript 类型定义
└── utils/            # 工具函数
```

## 启动

```bash
pnpm install
pnpm dev        # 开发模式 → http://localhost:5173
pnpm build      # 构建生产包
pnpm preview    # 预览生产包
```

## 文档

项目文档见 `docs/` 目录。
