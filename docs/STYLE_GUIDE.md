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
