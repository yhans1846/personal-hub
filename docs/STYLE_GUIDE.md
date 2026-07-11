# Personal Hub - 编码规范

## 通用原则
可读性优先于性能 | 结构统一 | 禁止风格不统一 | 添加中文注释 | 不生成无用代码 | 不用过时 API

---

## 后端规范（Java / Spring Boot）

**所有 Java 代码必须遵守阿里巴巴开发手册（强制）：**

| 类别 | 要求 |
|------|------|
| 类注释 | `/** 说明 */` 所有 class/interface/enum 必须有 |
| 方法注释 | 所有 public 方法有 Javadoc，含 `@param`/`@return`/`@throws` |
| 字段注释 | `/** 说明 */` 放在字段上方，禁止行尾 `//` |
| 常量 | `UPPER_SNAKE_CASE` + `final` 修饰，禁止魔法值 |
| @Override | 不重复写 Javadoc，继承接口注释 |
| @Bean 方法 | 配置类中需写 Javadoc 说明用途 |

### Swagger 注解规范
- **Controller**: `@Tag`(类) + `@Operation`(方法) + `@Parameter(hidden=true)`(Authentication参数)
- **DTO**: `@Schema`(类+字段，含 example)
- **VO**: `@Schema`(类+字段)
- Result / PageResult 已添加无需重复

### 公共组件编码思想

以下模式应贯穿所有模块，新增代码时主动遵守：

#### 1. 分页参数复用 `PageParam`
所有分页查询 DTO **必须继承** `PageParam` 基类，禁止在各 DTO 中重复定义 `page` / `size`。
```java
// ✅ 正确
public class XxxQueryDTO extends PageParam {
    private String keyword;
    // 只需要业务字段，page/size 由父类提供
}

// ❌ 错误 — 重复造轮子
public class XxxQueryDTO {
    private Integer page = 1;   // 重复
    private Integer size = 10;  // 重复
    private String keyword;
}
```

#### 2. 统一返回状态码枚举 `ResultCode`
返回给前端的 `code` **必须**使用 `ResultCode` 枚举，禁止硬编码魔法数字。
```java
// ✅ 正确
return Result.badRequest("参数错误");
return Result.error(ResultCode.FORBIDDEN, "无权限");
throw new BusinessException(ResultCode.BAD_REQUEST, "库存不足");

// ❌ 错误 — 魔法数字
return Result.error(400, "参数错误");
throw new BusinessException("库存不足");  // 默认 400 可接受，但推荐显式
```

#### 3. 关键路径必须记录日志
- **Service 层**：增删改操作用 `log.info()`, 权限校验失败用 `log.warn()`
- **异常处理器**：业务异常 `log.warn()`, 未知异常 `log.error()`（含堆栈）
- **过滤器**：认证结果用 `log.debug()`

```java
@Slf4j
@Service
public class XxxServiceImpl implements XxxService {
    public void delete(Long id, Long userId) {
        // 校验失败 → log.warn
        if (notFound) {
            log.warn("资源不存在: id={}, userId={}", id, userId);
            throw new NotFoundException("资源不存在");
        }
        // 操作成功 → log.info
        mapper.deleteById(id);
        log.info("删除资源: id={}, userId={}", id, userId);
    }
}
```

### 命名
| 类型 | 规则 | 示例 |
|------|------|------|
| 包名 | 小写单数 | `com.personalhub.controller` |
| 类名 | PascalCase | `NoteController` |
| 方法/变量 | camelCase | `getNoteList` / `noteService` |
| 常量 | UPPER_SNAKE | `MAX_PAGE_SIZE` |
| 表/字段 | snake_case | `note_note` / `created_at` |
| DTO/VO/Entity | XxxDTO / XxxVO / Xxx | |

### 分层职责
- **Controller**: 参数校验+结果返回。构造器注入
- **Service**: 接口+实现类，全部业务逻辑
- **Mapper**: 继承 `BaseMapper<T>`，仅数据库操作
- **DTO**: 接收请求（`@Valid`） | **VO**: 返回前端 | **Entity**: 不直接暴露

### 时间 & Lombok
- 统一 `LocalDateTime`，不用 `java.util.Date`
- `@Data`(Entity/DTO/VO) / `@RequiredArgsConstructor`(Controller/Service)

### Maven 多模块结构

#### 当前结构（14 模块）

```
personal-hub-server/
├── pom.xml                          # 父 POM（Spring Boot parent）
├── ph-common/                       # 公共组件
│   └── src/main/java/com/personalhub/common/
│       ├── config/                  # MyBatis-Plus/Jackson/Redis/Security/Swagger
│       ├── exception/               # 全局异常处理 + 自定义异常
│       ├── filter/                  # JWT 认证过滤器
│       ├── result/                  # Result / PageResult
│       └── util/                    # JwtUtil
├── ph-auth/                         # 认证 + 用户模块
├── ph-note/                         # 笔记模块
├── ph-study/                        # 学习记录模块
├── ph-todo/                         # 待办任务模块
├── ph-file/                         # 文件管理模块
├── ph-diary/                        # 日记模块
├── ph-bookmark/                     # 收藏夹模块
├── ph-studyplan/                    # 学习计划模块
├── ph-reading/                      # 阅读记录模块
├── ph-tag/                          # 统一标签模块
├── ph-dashboard/                    # Dashboard + 数据统计 + 全局搜索模块
└── ph-boot/                         # Spring Boot 启动入口
    └── src/main/
        ├── java/com/personalhub/PersonalHubApplication.java
        └── resources/               # application.yml + application-dev.yml
```

#### 目标结构（领域聚合，7 模块）

后续重构逐步合并为领域模块：

```
personal-hub-server/
├── ph-common        # 公共能力
├── ph-system        # 用户 + 认证 + 通知       <- ph-auth + ph-notification
├── ph-knowledge     # 笔记 + 日记 + 学习记录    <- ph-note + ph-diary + ph-study
│                    # + 阅读记录 + 标签         <- + ph-reading + ph-tag
├── ph-planning      # Todo + 学习计划           <- ph-todo + ph-studyplan
├── ph-resource      # 收藏夹 + 文件管理         <- ph-bookmark + ph-file
├── ph-dashboard     # 聚合查询 + 全局搜索       <- ph-dashboard
└── ph-boot          # 启动入口
```

每模块内部按分层组织：`controller/ dto/ entity/ mapper/ service/ vo/`

> 领域划分原则：按业务领域聚合，不按数据库表划分模块，避免"每张表一个 Module"的数据库思维。

---

## 前端规范（Vue 3 / TypeScript）

### 命名
| 类型 | 规则 | 示例 |
|------|------|------|
| 组件/页面 | PascalCase.vue | `NoteEditor.vue` |
| 组合函数 | useXxx | `useNoteList` |
| Store | useXxxStore | `useAuthStore` |
| TS 文件 | camelCase.ts | `noteApi.ts` |
| 类型 | I 前缀或直接大写 | `INoteItem` / `NoteVO` |
| CSS class | kebab-case/BEM | `.note-card__title` |

### 目录结构

#### 当前：按页面类型组织

```
personal-hub-web/src/
├── api/           # Axios实例 + 各模块接口
├── router/        # 路由配置
├── stores/        # Pinia（按模块拆分）
├── types/         # TS 类型定义
├── views/         # 页面组件（按页面一级目录）
├── components/    # 公共组件
│   ├── common/    # 通用组件（EmptyState / PageHeader 等）
│   ├── business/  # 业务组件
│   └── charts/    # ECharts 图表
├── composables/   # 组合式函数
├── layouts/       # 布局组件
├── styles/        # 全局样式
└── utils/         # 工具函数
```

#### 目标：按业务领域组织（推荐）

随着模块增多，`views/` 一级目录膨胀，推荐重组为 `modules/` 结构，与后端领域对齐：

```
personal-hub-web/src/
├── api/               # 按领域聚合的 API 文件
├── components/        # 公共组件（common / business / charts）
├── composables/
├── layouts/
├── router/
├── stores/            # 仅全局状态（user / theme / dashboard）
├── styles/
├── types/
├── utils/
└── modules/           # 按业务领域组织页面
    ├── dashboard/
    │   ├── Dashboard.vue
    │   ├── components/     # Greeting / StatCards / QuickActions 等
    │   └── api.ts
    ├── knowledge/
    │   ├── note/           # List / Detail / Edit + components/
    │   ├── diary/
    │   ├── study/
    │   ├── reading/
    │   └── tag/
    ├── planning/
    │   ├── todo/
    │   └── studyplan/
    ├── resource/
    │   ├── bookmark/
    │   └── file/
    ├── search/
    └── system/
        └── login/
```

> 每个模块内部按功能分层：页面级组件（List / Detail / Edit）+ 私有 `components/` + `api.ts`。
> Dashboard.vue 只负责布局和数据组装，子模块拆分到 `dashboard/components/`。

### 组件 / API / Store
- 全部 `<script setup lang="ts">` + TypeScript，Props/Emits 类型推断
- Pinia 按模块拆分 Store，简单状态组件内管理
- API 请求统一封装在 `api/` 目录
- **API 文件按领域聚合**，与后端模块命名保持一致：

  ```
  api/
  ├── request.ts         # Axios 实例 + 拦截器
  ├── auth.ts            # 认证相关
  ├── knowledge.ts       # 笔记/日记/学习/阅读/标签
  ├── planning.ts        # Todo / 学习计划
  ├── resource.ts        # 收藏夹 / 文件
  ├── dashboard.ts       # 首页统计 + 趋势
  ├── notification.ts    # 通知
  └── search.ts          # 全局搜索
  ```

  禁止每张表一个 api 文件（如 `note.ts` / `study.ts` / `reading.ts` 各写一个），同一领域放一起。

### Pinia 使用原则
- **全局状态**：`user`（登录信息）、`theme`（主题/强调色）、`dashboard`（首页聚合数据）用 Pinia Store
- **业务列表数据**：直接在页面组件内通过 API 获取和管理，不建议所有业务都建 Store
- 简单数据传递用 props + emits，跨多层传递用 provide/inject，**不滥用 Pinia 做全局状态管理**

### 依赖引入原则
- 可用社区包简化实现时优先使用，不强行手写轮子
- 但也不随意引入大而全的库，评估体积和必要性
- 例：拖拽用 sortablejs，Markdown 编辑用 md-editor-v3
- 禁止引入功能重复的依赖（如同时用两个图标库）

---

## UI/UX 设计原则

本项目不是企业后台，而是**个人知识管理系统**。设计参考 Notion、Linear、Raycast、Apple HIG。

### 核心调性
- **简洁、专注、温暖、高级** — 不追求炫酷动画和复杂视觉
- **长时间使用不疲劳** — 大量留白、舒适阅读体验、极低学习成本
- **内容优先** — 笔记、学习、阅读体验是核心，功能性控件退后
- **禁止** 传统 Admin 模板风格（灰底白卡、密集表格、深色大菜单）

### 整体布局
```
┌──────────────────────────────────────┐
│ Logo        Search          User     │ ← 56px
├────────────┬─────────────────────────┤
│            │                         │
│ Sidebar    │     Main Content        │
│  240px     │    max-width: 1600px    │
│            │     padding: 32px       │
│            │                         │
└────────────┴─────────────────────────┘
```

### 侧边栏规范
- 三段分组：**工作区**（首页/笔记/学习记录/待办/日记/收藏夹/计划/阅读/文件）、**管理**（分类/标签/回收站）、**统计**
- 一级菜单：14px/500w/40px高/圆角12px，**激活态：实心蓝色背景+白色文字图标**
- 二级菜单：13px/400w/灰色/缩进24px
- 分组标题：11px/600w/灰色/大写

### 颜色规范（CSS 变量定义在 `styles/global.css`）

| Token | 浅色 | 深色 |
|-------|------|------|
| `--accent` | `#4F7BFF` | `#60A5FA` |
| `--accent-hover` | `#5B86FF` | `#93C5FD` |
| `--bg-body` | `#F8FAFC` | `#0B1120` |
| `--bg-card` | `#FFFFFF` | `#1E293B` |
| `--border-color` | `#EAEAEA` | `#334155` |
| `--text-primary` | `#0F172A` | `#F1F5F9` |
| `--text-secondary` | `#64748B` | `#94A3B8` |
| `--success` | `#22C55E` | `#34D399` |
| `--warning` | `#F59E0B` | `#FBBF24` |
| `--danger` | `#EF4444` | `#F87171` |

**强调色支持 5 种切换**：蓝色 `#4F7BFF` / 紫色 `#8B5CF6` / 青色 `#06B6D4` / 橙色 `#F97316` / 绿色 `#10B981`，通过 `data-accent` 属性切换。

### 字体与字号

| 类型 | 大小 | 字重 |
|------|------|------|
| 页面标题 | 24px (text-2xl) | 600 |
| 卡片标题 | 16px (text-base) | 600 |
| 正文 | 14px (text-sm) | 400 |
| 辅助文字 | 13px | 400 |
| 标签 | 12px (text-xs) | 500 |

字体栈：系统字体（SF Pro / Noto Sans SC / Segoe UI），等宽字体用 SF Mono / Fira Code。

### 间距体系（8px 基准）
`4 / 8 / 12 / 16 / 20 / 24 / 32 / 40 / 48px`，对应 `--sp-1` 至 `--sp-12`。

### 卡片设计（统一，禁止各页面不同）
- 圆角：12px（`--radius-lg`，容器级）/ 8px（`--radius-sm`，组件级）
- 边框：`1px solid var(--border-color)`
- 阴影：`--shadow-md`（`0 2px 8px rgba(0,0,0,0.06)`）
- Hover：`transform: translateY(-2px)` + `--shadow-lg`

### 按钮规范
- 主按钮：Filled，主色 `var(--accent)`
- 次按钮：Outlined（透明背景，hover 时变灰）
- 危险按钮：红色
- 禁止使用 Element Plus 默认蓝色

### 共享组件（`src/components/`）
| 组件 | 用途 | Props |
|------|------|-------|
| `EmptyState` | 列表空状态 | `icon`, `text`, `actionLabel?`, `illustration?` |
| `StatCard` | Dashboard 统计卡片 | `icon`, `value`, `label`, `color?` |
| `PageHeader` | 页面标题 | `title`, `subtitle?`, 插槽 |
| `ListToolbar` | 列表搜索栏 | `search`, `search-placeholder`, `create-label` + `#filters` 插槽 |
| `ListPagination` | 分页 | `total`, `page`, `size` |
| `CommandPalette` | Ctrl+K 全局搜索 | 全局快捷键 |
| `NotificationBell` | 通知下拉 | el-badge 红标 + el-popover 面板，自动检测生成 |
| `SearchBar` | 通用搜索框 | `placeholder`, `modelValue`, `debounce?` |
| `ConfirmDialog` | 确认弹窗 | `title`, `content`, `confirmText?`, `cancelText?` |
| `MarkdownEditor` | Markdown 编辑器 | `modelValue`, `previewOnly?` |
| `Tag` | 标签展示 | `name`, `color`, `closable?` |
| `PriorityTag` | 优先级标签 | `priority` (高/中/低) |
| `ProgressCard` | 进度卡片 | `value`, `max`, `label` |
| `FileUploader` | 文件上传 | `accept`, `multiple?`, `maxSize?` |

### 工具函数（`src/utils/`）
| 文件 | 函数 | 用途 |
|------|------|------|
| `readingTime.ts` | `estimateReadingTime()` | Markdown 阅读时长估算 |
| | `formatRelativeTime()` | 相对时间格式化（刚刚/X分钟前） |
| | `isRecentlyEdited()` | 24 小时内编辑检测 |

### 新页面模板
`PageHeader` → `ListToolbar`（`#filters` 插槽）→ loading skeleton → `EmptyState` → 内容 → `ListPagination`

### 图标规范
- 统一使用 **lucide-vue-next**（已迁移为 `@lucide/vue`）
- 禁止混用 Element Icon / Heroicons / Tabler / Remix
- 图标尺寸：导航 18px、操作 14px、空状态 48px

### 交互规范
- 所有可点击元素：`cursor: pointer` + `transition: 200ms ease`
- 输入框 Focus：蓝色边框（`2px var(--accent)` inset）
- 卡片 Hover：`translateY(-2px)` + 阴影提升
- 按钮 Hover：颜色渐变（浅色加深）
- 操作按钮 hover 才显示（减少视觉噪音）

### 空状态规范
- **禁止** 纯文字 "暂无数据"
- 必须包含：SVG 插画 + 引导文案 + 操作按钮
- 使用 `<EmptyState>` 组件，传入 `illustration` 参数选择插画主题

### 动画标准
- 路由过渡：`page-fade` 淡入淡出（200ms）
- 收藏按钮：`fav-pop` 弹跳动效（350ms cubic-bezier）
- 骨架屏：`pulse` 闪烁（1.5s）
- 侧边栏展开（移动端）：slide 250ms ease

### 响应式断点
| 断点 | 行为 |
|------|------|
| < 768px | 侧边栏折叠为 overlay + 汉堡菜单，Dashboard 单列，Topbar 精简 |
| < 1024px | Topbar 文字隐藏，统计卡片 2 列 |

### Element Plus 使用规范
- 全局安装 `element-plus`，通过 `styles/global.css` 覆盖主题变量
- 不使用 SCSS 主题构建（CSS 变量覆盖更简单）
- 按钮：flat/text 风格，无渐变强阴影
- 输入框：圆角 8px、淡边框、focus 蓝色描边
- 表格：仅数据密集型场景保留，边框最小化
- 表单：`label-position="top"` 而非左侧标签
- 对话框：圆角 16px、大阴影
- 分页：简约风格，激活态 accent 色
- 菜单：borderless，自定义 item hover/active

### 深色模式
- 通过 `data-theme="dark"` 属性切换，CSS 变量自动适配
- 初始跟随系统 `prefers-color-scheme: dark`
- 手动切换后持久化到 `localStorage`（`theme-preference` 键）
- 支持用户手动覆盖系统偏好

### Checklist（新增页面时对照）
1. ✅ 使用 `<PageHeader>` + `<EmptyState>` 共享组件
2. ✅ 卡片网格/列表优先，不用 table
3. ✅ 卡片 hover 反馈（translateY + shadow）
4. ✅ 深色模式验证
5. ✅ 响应式适配（768px 断点）
6. ✅ 骨架屏加载状态
7. ✅ 统一的页面结构（header → toolbar → list → pagination）

### 相关文件
- 设计 Token / Element Plus 覆盖：`personal-hub-web/src/styles/global.css`
- 共享组件：`personal-hub-web/src/components/`
- 深色/强调色切换：`personal-hub-web/src/main.ts`

---

## Git Commit

```
<类型>: <简要描述>
```
类型: feat(新功能) fix(修复) docs(文档) style(格式) refactor(重构) perf(优化) test(测试) chore(工具/依赖)

示例: `feat: 新增笔记分类管理接口`
