# 登录页重设计 + 滑动拼图验证码

日期：2026-07-17  
状态：已确认（方案 1 · 分屏墨绿 + 滑动拼图）

## 目标

将登录页从居中白卡片升级为**分屏书房气质**，并强制**滑动拼图验证**后方可账号密码登录，提升辨识度与基础防爆破能力。

## 决策摘要

| 项 | 选择 |
|----|------|
| 布局 | 左品牌叙事 / 右表单（约 52% / 48%） |
| 色调 | 墨绿墨水 + 米黄纸感（独立登录色板） |
| 验证 | 自研滑动拼图（无第三方） |
| 注册/找回 | 不做 |

## 视觉与布局

### 桌面

- **左侧**：墨绿渐变（`#1a3329` → `#12241d`）+ 淡网格；Logo「Personal Hub」；Hero「你的知识，安静生长。」；副文案「笔记 · 计划 · 阅读 · 收藏 — 一处私人工作台。」；底标 `PERSONAL KNOWLEDGE OS`
- **右侧**：米黄纸感背景；标题「欢迎回来」；用户名、密码；滑动拼图；主按钮「进入 Personal Hub」（未通过验证禁用）
- **字体**：展示标题可用衬线（如 Cormorant Garamond / 系统衬线回退）；UI 用现有无衬线栈
- **动效**：页入淡入；拼图成功缺口合拢；按钮启用过渡

### 窄屏（≤760px）

上下堆叠：品牌区在上（压缩高度），表单在下。

### 非目标（视觉）

- 不接插画资源包；不做紫渐变 / 奶油衬线模板套路
- 不污染站内 `--accent` 主题体系（登录页 scoped CSS 变量）

## 滑动拼图 · 安全与 API

### 流程

1. 打开登录页 → `GET /api/auth/captcha`
2. 返回：`captchaId`、`background`（底图 base64）、`slider`（滑块图 base64）、`y`（缺口纵坐标，前端渲染用）
3. **正确偏移 `x` 仅存服务端**（优先 Redis，与登录限流一致；无 Redis 时回退内存 Map），TTL **120s**，一次性消费
4. 用户拖动滑块松手后，前端暂存 `sliderX`；通过本地容差提示 UX，**最终以服务端为准**
5. `POST /api/auth/login` Body：`{ username, password, captchaId, sliderX }`
6. 服务端校验：`|sliderX - storedX| ≤ 8`（像素，按生成图坐标系）；通过后删除 captcha，再校验账号密码
7. 松手时先调 `POST /api/auth/captcha/check`：未对齐则前端回弹左侧；对齐后才允许登录

### 接口

| 方法 | 路径 | 鉴权 | 说明 |
|------|------|------|------|
| GET | `/api/auth/captcha` | 否 | 发码 |
| POST | `/api/auth/login` | 否 | 增字段 `captchaId`、`sliderX`（必填） |

`SecurityConfig` 需放行 `/api/auth/captcha`。现有 `LoginRateLimitFilter` 继续保护 `/api/auth/login`；可选对 captcha 做轻度限流（同 IP）。

### 生成策略

- 服务端用 `BufferedImage` 生成底图（噪点/渐变/简单形状）与缺口滑块图，避免依赖外部图片 CDN
- `captchaId`：UUID；存储键：`captcha:{id}` → `{x, expireAt}`

## 前端改动

| 文件 | 职责 |
|------|------|
| `Login.vue` | 分屏布局 + 表单 + 独立色板 |
| `SliderCaptcha.vue`（新建） | 拉码、拖拽、emit `{ captchaId, sliderX }` / refresh |
| `authStore` / login API | login 请求携带 captcha 字段；新增 `getCaptcha()` |

未验证通过：主按钮 `disabled`。验证过期或登录失败：自动 `refresh` 拼图并清空 slider 状态。

默认预填本地常用账号 `admin` / `123456`（个人自用，不改密码场景）。

## 后端改动

| 位置 | 职责 |
|------|------|
| `CaptchaService`（新建于 `ph-system`） | 生成、存储、校验、销毁 |
| `AuthController` | `GET /captcha`；login DTO 扩展 |
| `LoginDTO` | `captchaId`、`sliderX` 必填校验 |
| `AuthServiceImpl` | 先 captcha 后密码 |
| `SecurityConfig` | permit captcha |

## 错误处理

| 情况 | 行为 |
|------|------|
| captcha 缺失/过期/已用 | 400，文案「验证已失效，请重试」+ 前端刷新 |
| 偏移不准 | 400，「请完成拼图验证」+ 刷新 |
| 用户名密码错误 | 保持现有文案；仍刷新 captcha（防枚举重试） |
| 限流 | 保持现有限流响应 |

## 测试

- 单元：`CaptchaService` 容差内外、一次性消费、过期
- 接口：无 captcha 登录失败；正确 captcha + 错误密码失败；双正确成功
- 前端：窄屏堆叠；禁用按钮；刷新拼图

## 文档同步（实现时）

- `docs/API.md`：captcha + login 字段
- `docs/CHANGELOG.md`
- `docs/STYLE_GUIDE.md` 或 PROJECT：登录页独立气质一句

## 非目标

- 注册、忘记密码、OAuth、短信/邮箱 OTP、Cloudflare Turnstile
- 改 JWT / 登录成功后路由逻辑
