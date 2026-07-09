# Personal Hub

个人知识管理系统，Spring Boot 3 + Vue 3 前后端分离。

## 技术栈

| 端 | 技术 |
|------|------|
| 后端 | Spring Boot 3.3.5 / Java 21 / MyBatis-Plus / MySQL / Redis |
| 前端 | Vue 3 / Vite / TypeScript / Element Plus / Pinia |
| 认证 | JWT + Spring Security |
| 文档 | SpringDoc OpenAPI (Swagger) |

## 模块

```
backend/                  # 后端
├── ph-common/           # 公共组件（Result、异常处理、JWT、配置）
├── ph-auth/             # 用户认证
├── ph-note/             # Markdown 笔记
├── ph-study/            # 学习记录
├── ph-boot/             # 启动入口
└── pom.xml              # 父 POM

frontend/                 # 前端
├── src/views/           # 页面
├── src/api/             # 接口封装
├── src/store/           # 状态管理（Pinia）
└── ...
```

## 功能

- 用户登录 / JWT 认证
- Markdown 笔记（CRUD / 分类 / 标签 / 收藏 / 回收站 / 搜索）
- 学习记录（CRUD / 日期筛选 / 关键词搜索）

## 快速启动

```bash
# 后端
cd backend
mvn spring-boot:run -pl ph-boot -am

# 前端
cd frontend
npm install
npm run dev
```

访问地址：`http://localhost:8080`
API 文档：`http://localhost:8080/swagger-ui.html`

## 文档

项目设计文档位于 `docs/` 目录：

| 文档 | 内容 |
|------|------|
| PROJECT.md | 功能规划、模块依赖 |
| TECH_STACK.md | 技术栈版本 |
| DATABASE.md | 表结构 |
| API.md | 接口规范 |
| STYLE_GUIDE.md | 编码规范 |
| ROADMAP.md | 开发路线 |
| CHANGELOG.md | 版本记录 |
