# Personal Hub - 编码规范

## 通用原则

- 代码可读性优先于性能优化（在不影响体验的前提下）
- 保持项目结构统一，新增文件遵循已有目录模式
- 禁止出现风格不统一的代码
- 所有代码文件添加必要的中文注释
- 不生成未被使用的代码
- 不使用已废弃或过时的 API

---

## 后端规范（Java / Spring Boot）

### 命名规范

| 类型         | 规则                          | 示例                        |
| ------------ | ----------------------------- | --------------------------- |
| 包名         | 全部小写，单数                | `com.personalhub.controller` |
| 类名         | 大驼峰 `PascalCase`           | `NoteController`            |
| 方法名       | 小驼峰 `camelCase`            | `getNoteList`               |
| 变量名       | 小驼峰 `camelCase`            | `noteService`               |
| 常量名       | 全大写蛇形 `UPPER_SNAKE`     | `MAX_PAGE_SIZE`             |
| 数据库表名   | 小写蛇形 `snake_case`         | `note_note`                 |
| 数据库字段名 | 小写蛇形 `snake_case`         | `created_at`                |
| DTO          | `XxxDTO`                      | `NoteCreateDTO`             |
| VO           | `XxxVO`                       | `NoteVO`                    |
| Entity       | `Xxx` 或 `XxxEntity`          | `Note`                      |

### 目录规范

```
backend/src/main/java/com/personalhub/
├── common/                  # 公共类
│   ├── config/              # 配置类（Security、MyBatis-Plus 等）
│   ├── exception/           # 全局异常处理
│   ├── result/              # 统一返回（Result、PageResult）
│   └── util/                # 工具类
├── module/                  # 业务模块
│   ├── auth/                # 认证模块
│   │   ├── controller/      # AuthController
│   │   ├── service/         # AuthService
│   │   ├── dto/             # LoginDTO
│   │   └── vo/              # LoginVO
│   ├── note/                # 笔记模块
│   │   ├── controller/      # NoteController
│   │   ├── service/         # NoteService
│   │   ├── mapper/          # NoteMapper
│   │   ├── entity/          # Note
│   │   ├── dto/             # NoteCreateDTO、NoteQueryDTO
│   │   └── vo/              # NoteVO、NoteListVO
│   ├── study/               # 学习记录模块
│   ├── todo/                # 待办模块
│   └── file/                # 文件模块
└── PersonalHubApplication.java
```

### Controller 规范

- Controller **只负责参数校验和结果返回**，不写业务逻辑
- 使用 `@RestController` + `@RequestMapping`
- 注入 Service 使用构造器注入（`@RequiredArgsConstructor`）

```java
@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    @GetMapping
    public Result<PageResult<NoteVO>> list(NoteQueryDTO query) {
        return Result.success(noteService.listNotes(query));
    }
}
```

### Service 规范

- 接口 + 实现类：`NoteService` + `NoteServiceImpl`
- Service 负责所有业务逻辑
- 复杂查询使用 MyBatis-Plus 的 `LambdaQueryWrapper`

### Mapper 规范

- 继承 MyBatis-Plus `BaseMapper<T>`
- Mapper **只负责数据库操作**，不写业务逻辑
- 简单 CRUD 直接使用 MyBatis-Plus 内置方法

```java
@Mapper
public interface NoteMapper extends BaseMapper<Note> {
}
```

### DTO / VO 规范

- DTO 用于接收前端请求参数（`@Valid` 校验）
- VO 用于返回给前端的数据
- Entity 不直接暴露给前端

### 时间处理

- 统一使用 `LocalDateTime`（Java 8+）
- 不使用 `java.util.Date`
- MyBatis-Plus 自动处理 `LocalDateTime` 与 `DATETIME` 映射

### Lombok 使用

- Entity / DTO / VO 使用 `@Data`
- Controller 使用 `@RequiredArgsConstructor`（构造器注入）
- Service 使用 `@Service` + `@RequiredArgsConstructor`

---

## 前端规范（Vue 3 / TypeScript）

### 命名规范

| 类型       | 规则                       | 示例                  |
| ---------- | -------------------------- | --------------------- |
| 组件文件   | 大驼峰 `PascalCase.vue`    | `NoteEditor.vue`      |
| 页面文件   | 大驼峰 `PascalCase.vue`    | `NoteList.vue`        |
| 组合式函数 | `useXxx`                   | `useNoteList`         |
| 状态 Store | `useXxxStore`              | `useAuthStore`        |
| TypeScript 文件 | 小驼峰 `camelCase.ts` | `noteApi.ts`        |
| 类型定义   | `I` 前缀或直接大写         | `INoteItem` / `Note`  |
| 变量名     | 小驼峰 `camelCase`         | `noteList`            |
| CSS class   | kebab-case / BEM           | `.note-card__title`   |

### 组件规范

- 全部使用 `<script setup lang="ts">`（Composition API）
- 全部使用 TypeScript
- Props 和 Emits 使用 TypeScript 类型推断

```vue
<script setup lang="ts">
import type { NoteVO } from '@/types/note'

const props = defineProps<{
  note: NoteVO
  loading?: boolean
}>()

const emit = defineEmits<{
  (e: 'update', id: number): void
  (e: 'delete', id: number): void
}>()
</script>
```

### 目录规范

```
frontend/src/
├── api/                     # API 请求层
│   ├── request.ts           # Axios 实例（拦截器、Token 注入）
│   ├── authApi.ts           # 认证接口
│   ├── noteApi.ts           # 笔记接口
│   └── ...
├── router/                  # 路由配置
│   └── index.ts
├── store/                   # Pinia 状态管理
│   ├── authStore.ts
│   └── ...
├── types/                   # TypeScript 类型定义
│   ├── common.ts            # Result<T>、PageResult<T>
│   └── note.ts              # NoteVO、NoteCreateDTO
├── views/                   # 页面组件
│   ├── login/
│   ├── notes/
│   └── ...
├── components/              # 公共组件
│   ├── AppLayout.vue
│   ├── MarkdownEditor.vue
│   └── ...
├── composables/             # 组合式函数
│   ├── usePagination.ts
│   └── ...
├── utils/                   # 工具函数
└── App.vue
```

### API 调用规范

```typescript
// api/noteApi.ts
import request from './request'
import type { PageResult, NoteVO, NoteCreateDTO } from '@/types'

export function getNoteList(params: Record<string, any>) {
  return request.get<PageResult<NoteVO>>('/notes', { params })
}

export function createNote(data: NoteCreateDTO) {
  return request.post<NoteVO>('/notes', data)
}
```

### 状态管理规范

- 使用 Pinia 进行状态管理
- 按模块拆分 Store
- 简单组件状态直接在组件内管理，不滥用全局 Store

---

## Git Commit 规范

### Commit Message 格式

```
<类型>: <简要描述>
```

### 类型说明

| 类型     | 说明                         |
| -------- | ---------------------------- |
| feat     | 新功能                       |
| fix      | 修复 Bug                     |
| docs     | 文档更新                     |
| style    | 代码格式调整（不影响逻辑）   |
| refactor | 重构                         |
| perf     | 性能优化                     |
| test     | 测试相关                     |
| chore    | 构建/工具/依赖变更           |

### 示例

```
feat: 新增笔记分类管理接口
fix: 修复 JWT Token 过期判断逻辑
docs: 更新 API 接口文档
refactor: 抽取文件上传公共逻辑
```

## 注释规范

### Java 注释

- 类注释：说明模块职责
- 方法注释：说明方法用途、参数、返回值
- 复杂逻辑：行内注释说明
- 使用 `/** */` JavaDoc 格式

```java
/**
 * 笔记业务服务
 */
@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {

    /**
     * 分页查询笔记列表
     *
     * @param query 查询条件
     * @return 笔记分页数据
     */
    @Override
    public PageResult<NoteVO> listNotes(NoteQueryDTO query) {
        // ...
    }
}
```

### Vue 注释

- 组件注释：在 `<script setup>` 顶部说明组件用途
- 复杂逻辑：行内注释
- API 文件：每个函数上方注释用途
