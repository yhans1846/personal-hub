# Personal Hub - 技术栈

## 后端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| JDK | 21 LTS | |
| Spring Boot | 3.x | + MVC / Security / Data Redis |
| MyBatis-Plus | 最新 | ORM |
| MySQL | 8.x | localhost:3306 · `personal_hub` |
| Redis | 5.x | localhost:6379 · DB 8 |
| JWT / Lombok / Maven 3.8.5 | | |
| SpringDoc OpenAPI | 2.6.0 | Swagger UI |
| Apache POI | 5.3.0 | 学习计划等 xlsx 导出 |

> 「最新」= 开发时正式版。Boot 3 需 JDK 17+，本项目 JDK 21。

### Maven 多模块
父 POM 管版本；模块为 `ph-common` / `ph-system` / `ph-biz` / `ph-boot`；`dependencyManagement` 管内部版本；仅 `ph-boot` 打可执行 JAR；优先官方 starter。

## 前端技术

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue 3 · Vite · TS · Router 4 · Pinia · Axios | | |
| Element Plus · lucide / @lucide/vue | | 唯一图标源 |
| vditor | | Markdown WYSIWYG（IR） |
| sortablejs · ECharts 6 | | 拖拽 / 图表 |

### 前端约定
`<script setup lang="ts">` · pnpm · 主题用 `global.css` 变量，不改 EP 源码。

## 开发工具

IDEA · VS Code · Git · Node 22 LTS · pnpm · Maven 3.8.5 · MySQL 8 · Redis 5

## 环境要求

| 环境 | 最低 |
|------|------|
| JDK / Node | 21 LTS / 22 LTS |
| MySQL / Redis | 8.0+ / 5.0+ |
| Maven / pnpm | 3.8.5 / 最新稳定 |
