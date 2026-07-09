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
└── ph-boot/                         # Spring Boot 启动入口
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

## Git Commit

```
<类型>: <简要描述>
```
类型: feat(新功能) fix(修复) docs(文档) style(格式) refactor(重构) perf(优化) test(测试) chore(工具/依赖)

示例: `feat: 新增笔记分类管理接口`
