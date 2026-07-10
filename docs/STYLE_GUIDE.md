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

```
backend/
├── pom.xml                          # 父 POM（Spring Boot parent）
├── ph-common/                       # 公共组件
│   └── src/main/java/com/personalhub/common/
│       ├── config/                  # MyBatis-Plus/Jackson/Redis/Security/Swagger
│       ├── exception/               # 全局异常处理 + 自定义异常
│       ├── filter/                  # JWT 认证过滤器
│       ├── result/                  # Result / PageResult
│       └── util/                    # JwtUtil
├── ph-auth/                         # 认证 + 用户模块
│   └── src/main/java/com/personalhub/
│       ├── module/auth/             # 登录/退出
│       └── module/user/             # 个人信息/密码
├── ph-note/                         # 笔记模块
│   └── src/main/java/com/personalhub/module/note/
├── ph-study/                        # 学习记录模块
│   └── src/main/java/com/personalhub/module/study/
├── ph-todo/                         # 待办任务模块
│   └── src/main/java/com/personalhub/module/todo/
├── ph-file/                          # 文件管理模块
│   └── src/main/java/com/personalhub/module/file/
└── ph-boot/                          # Spring Boot 启动入口
    └── src/main/
        ├── java/com/personalhub/PersonalHubApplication.java
        └── resources/               # application.yml + application-dev.yml
```

每模块内部按分层组织：`controller/ dto/ entity/ mapper/ service/ vo/`

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
```
frontend/src/
├── api/           # Axios实例 + 各模块接口
├── router/        # 路由配置
├── store/         # Pinia（按模块拆分）
├── types/         # TS 类型定义
├── views/         # 页面组件
├── components/    # 公共组件
├── composables/   # 组合式函数
└── utils/         # 工具函数
```

### 组件 / API / Store
- 全部 `<script setup lang="ts">` + TypeScript，Props/Emits 类型推断
- Pinia 按模块拆分 Store，简单状态组件内管理
- API 请求统一封装在 `api/` 目录

---

## UI/UX 设计原则

本项目不是企业后台，而是个人知识管理系统。设计参考 Notion、Linear、Obsidian、GitHub、Raycast。

### 核心调性
- **简洁现代**、大量留白，适合长时间阅读和写作
- **禁止**传统 Admin 模板视觉风格（灰底白卡、密集表格、深色侧栏大菜单）
- **内容优先**：笔记、学习、阅读体验是核心，功能性控件退后

### 布局
- **Dashboard 优先**：首页是知识管理概览，不是数据管理页
- **减少表格**：优先使用卡片网格（Card Grid）、列表（List）、时间线（Timeline）
- 响应式设计，桌面为主，兼顾平板

### 视觉 Token
| 属性 | 值 |
|------|-----|
| 圆角（容器级） | 12px (`--radius-lg`) |
| 圆角（组件级） | 6px (`--radius-sm`) |
| 阴影 | 柔和浅阴影 `0 1px 3px rgba(0,0,0,0.06)` |
| 间距体系 | 4 / 8 / 12 / 16 / 24 / 32 / 40 / 48 px |
| 字体 | 系统字体栈 |
| 阅读区最大宽度 | 800px (`--reading-max-width`) |

### 色彩体系（CSS 变量定义在 `styles/global.css`）
- **浅色**：背景 `#f8f9fa` → 卡片 `#ffffff`，文字 `#1a1a2e`
- **深色**：背景 `#0d1117`（GitHub Dark）→ 卡片 `#161b22`，文字 `#e6edf3`
- **强调色**：`#3b82f6`（浅色）/ `#58a6ff`（深色）

### 组件使用规范（Element Plus）
- 使用 Element Plus 但通过 `styles/global.css` 全局覆盖提升质感
- 按钮：flat/text 风格，无渐变/强阴影
- 输入框：大圆角、淡边框
- 表格：仅数据密集型场景保留，边框最小化
- 卡片：纯白（浅色）/ 深灰（深色），悬浮时阴影提升

### Vue 组件编写检查清单
1. 优先卡片网格（card grid）/ 列表（list）/ 时间线（timeline），不用 table
2. 卡片需悬浮反馈（hover shadow）
3. 操作按钮 hover 才显示（减少视觉噪音）
4. 标题/搜索/筛选在顶部统一区域
5. 表单使用 `label-position="top"` 而非左侧
6. 深色模式通过 `data-theme="dark"` 切换
7. 统一骨架屏加载动画

### 相关文件
- 设计 Token 变量：`frontend/src/styles/global.css`
- Element Plus 覆盖：同上文件
- 深色模式检测：`frontend/src/main.ts` 中自动跟随系统偏好

---

## Git Commit

```
<类型>: <简要描述>
```
类型: feat(新功能) fix(修复) docs(文档) style(格式) refactor(重构) perf(优化) test(测试) chore(工具/依赖)

示例: `feat: 新增笔记分类管理接口`
