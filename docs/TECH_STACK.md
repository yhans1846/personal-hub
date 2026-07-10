# Personal Hub - 技术栈

## 后端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| JDK | 21 LTS | Java 开发环境 |
| Spring Boot | 3.x | 后端框架 |
| Spring MVC | 随 Boot | Web 请求处理 |
| Spring Security | 最新 | 身份认证与授权 |
| Spring Data Redis | 随 Boot | Redis 集成 |
| MyBatis-Plus | 最新 | ORM 持久层 |
| MySQL | 8.x | 关系型（localhost:3306，库 personal_hub） |
| Redis | 5.x | 缓存（localhost:6379，DB 8）|
| JWT | 最新 | Token 登录认证 |
| Lombok | 最新 | 简化代码 |
| Maven | 3.8.5 | 依赖管理 |
| SpringDoc OpenAPI | 2.6.0 | API 文档（Swagger UI）|

> 版本说明："最新"指开发时当前可用最新正式版。Spring Boot 3.x 需 JDK 17+，本项目用 JDK 21 LTS。

### Maven 多模块依赖管理
- 父 POM `personal-hub` (packaging=pom) 统一管理依赖版本，子模块继承父 POM
- `${mybatis-plus.version}` / `${jjwt.version}` / `${springdoc.version}` 集中管理版本
- 内部模块间依赖通过 `dependencyManagement` 统一声明版本
- `spring-boot-starter-parent` 作为父 POM 的 parent，由 Spring Boot 管理 Spring 生态版本
- 仅 `web` 模块使用 `spring-boot-maven-plugin`（可执行 JAR）
- 新增依赖前评估必要性，优先 Spring Boot 官方 starter

## 前端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.x | 前端框架 |
| Vite | 最新 | 构建工具 |
| TypeScript | 最新 | JS 超集 |
| Vue Router | 4.x | 路由管理 |
| Pinia | 最新 | 状态管理 |
| Axios | 最新 | HTTP 请求 |
| Element Plus | 最新 | UI 组件库 |
| lucide-vue-next / @lucide/vue | 最新 | 图标库（统一图标源） |
| md-editor-v3 | 最新 | Markdown 编辑器组件 |
| ECharts | 6.x | 数据可视化图表 |

### 前端约定
- 全部 Vue 3 Composition API（`<script setup lang="ts">`）
- 全部 TypeScript
- 包管理器用 pnpm
- 图标统一使用 lucide-vue-next（禁止混用 Element Icon / Heroicons 等）
- UI 主题通过 `styles/global.css` CSS 变量覆盖，不修改 Element Plus 源代码

## 开发工具

| 工具 | 版本 | 用途 |
|------|------|------|
| IntelliJ IDEA | 2025/2026 | 后端 IDE |
| VS Code | 最新 | 前端编辑器 |
| Git | 最新 | 版本控制 |
| Node.js | 22 LTS | 前端运行时 |
| pnpm | 最新 | 前端包管理 |
| Maven | 3.8.5 | 后端依赖管理 |
| MySQL | 8.x | 数据库（localhost:3306）|
| Redis | 5.x | 缓存（localhost:6379，DB 8）|

## 环境要求

| 环境 | 最低版本 |
|------|---------|
| JDK | 21 LTS |
| Node.js | 22 LTS |
| MySQL | 8.0+ |
| Redis | 5.0+ |
| Maven | 3.8.5 |
| pnpm | 最新稳定版 |
