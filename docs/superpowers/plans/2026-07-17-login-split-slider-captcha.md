# 登录页分屏 + 滑动拼图 实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 登录页改为分屏墨绿书房 UI，并强制滑动拼图验证后方可账号密码登录。

**架构：** 后端 `CaptchaService` 用 `BufferedImage` 生成底图/滑块，正确 `x` 存 Redis（TTL 120s）；`login` 先校验 captcha 再验密。前端独立色板 `Login.vue` + `SliderCaptcha.vue`。

**技术栈：** Spring Boot 3、Redis、`BufferedImage`；Vue 3、scoped CSS（登录独立变量）。

**规格：** `docs/superpowers/specs/2026-07-17-login-split-slider-captcha-design.md`

---

## 文件结构

| 文件 | 职责 |
|------|------|
| `ph-system/.../service/CaptchaService.java` | 生成/存/验/销毁 |
| `ph-system/.../vo/CaptchaVO.java` | 发码响应 |
| `ph-system/.../dto/LoginDTO.java` | 增 `captchaId`、`sliderX` |
| `ph-system/.../controller/AuthController.java` | `GET /captcha`；login 传 captcha |
| `ph-system/.../service/AuthService.java` + Impl | login 签名扩展 |
| `ph-common/.../config/SecurityConfig.java` | permit `/api/auth/captcha` |
| `web/.../login/SliderCaptcha.vue` | 拉码 + 拖拽 |
| `web/.../login/Login.vue` | 分屏 UI |
| `web/src/store/authStore.ts` + api | captcha + login 字段 |
| `docs/API.md` / `CHANGELOG.md` | 文档同步 |

---

### 任务 1：CaptchaService + 单测

**文件：**
- 创建：`CaptchaService.java`、`CaptchaVO.java`
- 测试：`CaptchaServiceTest.java`

- [x] 编写容差/一次性/过期失败测试
- [x] 实现生成（BufferedImage）+ Redis 存取 + `verifyAndConsume`
- [x] 跑通单测

### 任务 2：Auth API 接入

- [x] `LoginDTO` 增加必填字段
- [x] `AuthService.login(username, password, captchaId, sliderX)`
- [x] `AuthController`：`GET /captcha`、login 调用校验
- [x] `SecurityConfig` permit captcha

### 任务 3：前端 API + SliderCaptcha

- [x] `getCaptcha` / login body 扩展
- [x] `SliderCaptcha.vue`：展示、拖拽、emit verified / refresh

### 任务 4：Login.vue 分屏 UI

- [x] 按规格重做布局与色板
- [x] 未验证禁用按钮；失败刷新拼图
- [x] 窄屏堆叠

### 任务 5：文档

- [x] 更新 `API.md`、`CHANGELOG.md`
- [x] 提交前自检（用户未要求则不 commit）
