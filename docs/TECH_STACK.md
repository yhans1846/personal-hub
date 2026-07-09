# Personal Hub - 技术栈

## 后端技术

| 技术            | 版本          | 说明                   |
| --------------- | ------------- | ---------------------- |
| JDK             | 21 LTS        | Java 开发环境          |
| Spring Boot     | 3.x           | 后端开发框架           |
| Spring MVC      | 随 Spring Boot | Web 请求处理           |
| Spring Security | 最新稳定版    | 身份认证与授权         |
| Spring Data Redis | 随 Spring Boot | Redis 集成            |
| MyBatis-Plus    | 最新稳定版    | ORM 持久层框架         |
| MySQL           | 8.x           | 关系型数据库           |
| Redis           | 5.x           | 缓存（预留）           |
| JWT             | 最新稳定版    | Token 登录认证         |
| Lombok          | 最新稳定版    | 简化 Java 代码         |
| Maven           | 3.8.5         | 项目依赖管理           |

> **版本说明**："最新稳定版" 表示在开发时使用当前可用的最新正式版本。Spring Boot 3.x 要求 JDK 17+，本项目统一使用 JDK 21 LTS。

### Maven 依赖管理

- 使用 `${revision}` 统一管理版本号
- 通过 `spring-boot-starter-parent` 统一管理 Spring 生态依赖版本
- 新增第三方依赖前需评估必要性，优先使用 Spring Boot 官方 starter

## 前端技术

| 技术         | 版本          | 说明               |
| ------------ | ------------- | ------------------ |
| Vue          | 3.x           | 前端框架           |
| Vite         | 最新稳定版    | 构建工具           |
| TypeScript   | 最新稳定版    | JavaScript 超集    |
| Vue Router   | 4.x           | 路由管理           |
| Pinia        | 最新稳定版    | 状态管理           |
| Axios        | 最新稳定版    | HTTP 请求          |
| Element Plus | 最新稳定版    | UI 组件库          |

### 前端约定

- 全部组件使用 Vue 3 **Composition API**（`<script setup lang="ts">`）
- 全部使用 TypeScript（`.ts` / `.vue` 中 `<script setup lang="ts">`）
- 组件优先复用，避免重复代码
- 使用 pnpm 作为包管理器

## 开发工具

| 工具          | 版本        | 用途           |
| ------------- | ----------- | -------------- |
| IntelliJ IDEA | 2025/2026   | 后端开发 IDE   |
| VS Code       | 最新版      | 前端开发编辑器 |
| Git           | 最新版      | 版本控制       |
| Node.js       | 22 LTS      | 前端运行时     |
| pnpm          | 最新版      | 前端包管理器   |
| Maven         | 3.8.5       | 后端依赖管理   |
| MySQL         | 8.x         | 数据库         |
| Redis         | 5.x         | 缓存（预留）   |

## 开发环境要求

| 环境     | 最低版本 |
| -------- | -------- |
| JDK      | 21 LTS   |
| Node.js  | 22 LTS   |
| MySQL    | 8.0+     |
| Redis    | 5.0+     |
| Maven    | 3.8.5    |
| pnpm     | 最新稳定版 |
