# 登录页产品化 UI（v2 · Glass + 呼吸感）

日期：2026-07-17  
状态：已实现  
关联：滑块/书架验证 `2026-07-17-login-split-slider-captcha-design.md`

## 布局

- **55% / 45%** 双栏，`100vh`，大量留白
- `<1200px` 隐藏左侧与知识水印

## 左侧

- **工作台 meta**：当前时间（HH:mm）+ 时段问候（Good Morning / Afternoon / Evening / Night）
- Logo（Personal Hub + Knowledge OS）→ 主副标题 → Features 两列
- **Quote**：每日一句（按一年中的日序轮换），固定在左下角
- 背景：浅网格呼吸 + 双超大 blur circle（36s / 40s 漂浮）
- 知识水印：Markdown / Book / Code / SQL / Vue / Spring，`opacity: 0.03` 缓慢漂浮
- 不做 Dashboard Preview / 今日概览卡片

## 右侧 · 三步登录流

Glass Card：`rgba(255,255,255,.72)` + blur ~20px + 内高光，宽 ~440px，圆角 20px  
输入 Focus 绿 glow；按钮 Hover Lift + Active `scale(0.98)`

| step | 内容 |
|------|------|
| `form` | 欢迎回来 👋 / 用户名 · 密码 /「进入 Personal Hub →」/ 记住我 · 忘记密码 |
| `loading` | 「正在验证身份…」+ spinner |
| `verify` | Drawer Slide 220ms：「安全验证」+ 书架归位；通过后自动登录；可「← 返回」 |

## 微动画规范

| 动画 | 时长 | 场景 |
|------|------|------|
| Fade + Up | 200~250ms | 页面进入、卡片加载 |
| Hover Lift | 150ms | 按钮、Card、功能入口 |
| Scale 0.98 | 100ms | 点击反馈 |
| Drawer Slide | 220ms | 验证步骤切换 |
| Background Float | 30~40s | 柔光背景 / 网格呼吸 |

原则：少而精；交互动画 ≤300ms；禁止 Bounce、Elastic、复杂粒子和过度缩放。尊重 `prefers-reduced-motion`。

## Token

品牌 `#16A34A` · 背景 `#F7F7F5` · 文字 `#111827` · 辅助 `#6B7280` · 边框 `#E5E7EB`
