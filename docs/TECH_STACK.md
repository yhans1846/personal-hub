# Personal Hub - 技术栈

> 职责：版本与开发/运行环境。模块划分见 `PROJECT.md`，部署见 `DEPLOYMENT.md`。

## 后端

| 技术 | 版本 |
|------|------|
| JDK | 21 LTS |
| Spring Boot | 3.x（MVC / Security / Data Redis） |
| MyBatis-Plus | 项目锁定正式版 |
| MySQL | 8.x · `personal_hub` |
| Redis | 5.x+（验证码等） |
| JWT / Lombok / Maven | 3.8.5+ |
| SpringDoc OpenAPI | 2.6.0 |
| Apache POI | 5.3.0（xlsx 导出） |

可执行包仅 `ph-boot`；内部模块版本由父 POM `dependencyManagement` 管理。

## 前端

| 技术 | 版本 |
|------|------|
| Vue 3 · Vite · TS · Router 4 · Pinia · Axios | 项目锁定 |
| Element Plus · lucide / `@lucide/vue` | 唯一图标源 |
| vditor | Markdown WYSIWYG（IR） |
| sortablejs · ECharts 6 | 拖拽 / 图表 |
| ESLint 9+ · typescript-eslint · eslint-plugin-vue · unused-imports | `pnpm lint`（扫 `src`） |
| vue-tsc | `pnpm build` 前置类型检查已通过 |

Node **22 LTS** · **pnpm**。编码与 UI 见 `STYLE_GUIDE.md`；页面形态见 `PAGE_SPEC.md`。

## 环境最低要求

| 项 | 要求 |
|----|------|
| JDK / Node | 21 / 22 LTS |
| MySQL / Redis | 8.0+ / 5.0+ |
| Maven / pnpm | 3.8.5+ / 最新稳定 |
