# Personal Hub - 编码规范

## 通用原则
可读性优先于性能（不影响体验） | 结构统一 | 禁止风格不统一 | 添加中文注释 | 不生成无用代码 | 不用过时 API

---

## 后端规范（Java / Spring Boot）

### 命名
| 类型 | 规则 | 示例 |
|------|------|------|
| 包名 | 小写单数 | `com.personalhub.controller` |
| 类名 | PascalCase | `NoteController` |
| 方法/变量 | camelCase | `getNoteList` / `noteService` |
| 常量 | UPPER_SNAKE | `MAX_PAGE_SIZE` |
| 表/字段 | snake_case | `note_note` / `created_at` |
| DTO/VO/Entity | XxxDTO / XxxVO / Xxx | |

### 目录结构
```
backend/src/main/java/com/personalhub/
├── common/                   # config/ exception/ result/ util/
├── module/{auth,note,study,todo,file}/
│   └── {controller/ service/ mapper/ entity/ dto/ vo/}
└── PersonalHubApplication.java
```

### 分层职责
- **Controller**: 仅参数校验和结果返回。`@RestController` + `@RequestMapping` + 构造器注入
- **Service**: 接口 + 实现类，负责全部业务逻辑
- **Mapper**: 继承 `BaseMapper<T>`，仅数据库操作，简单 CRUD 用内置方法
- **DTO**: 接收请求（`@Valid`）| **VO**: 返回前端 | **Entity**: 不直接暴露

### 时间 & Lombok
- 统一 `LocalDateTime`，不用 `java.util.Date`
- Entity/DTO/VO 用 `@Data`，Controller/Service 用 `@RequiredArgsConstructor`

### Swagger 注解规范
- 所有 Controller 类添加 `@Tag(name = "模块名", description = "说明")`
- 所有接口方法添加 `@Operation(summary = "简要说明", description = "详细说明")`
- 所有 DTO 类添加 `@Schema(description = "说明")`，字段添加 `@Schema(description = "说明", example = "示例值")`
- 所有 VO 类添加 `@Schema(description = "说明")`，字段添加 `@Schema(description = "说明")`
- 统一返回 Result 和 PageResult 已添加 `@Schema`，无需重复

---

## 前端规范（Vue 3 / TypeScript）

### 命名
| 类型 | 规则 | 示例 |
|------|------|------|
| 组件/页面 | PascalCase.vue | `NoteEditor.vue` |
| 组合函数 | useXxx | `useNoteList` |
| Store | useXxxStore | `useAuthStore` |
| TS 文件 | camelCase.ts | `noteApi.ts` |
| 类型 | I 前缀或直接大写 | `INoteItem` |
| CSS class | kebab-case/BEM | `.note-card__title` |

### 目录结构
```
frontend/src/
├── api/           # Axios 实例 + 各模块接口
├── router/        # 路由配置
├── store/         # Pinia（按模块拆分）
├── types/         # TS 类型定义
├── views/         # 页面组件
├── components/    # 公共组件
├── composables/   # 组合式函数
└── utils/         # 工具函数
```

### 组件规范
全部 `<script setup lang="ts">` + TypeScript，Props/Emits 使用类型推断。

### API 调用
```typescript
export function getNoteList(params: Record<string, any>) {
  return request.get<PageResult<NoteVO>>('/notes', { params })
}
```

### 状态管理
Pinia 按模块拆分 Store，简单状态组件内管理。

---

## Git Commit

```
<类型>: <简要描述>
```

| 类型 | 说明 |
|------|------|
| feat | 新功能 |
| fix | 修复 |
| docs | 文档 |
| style | 格式 |
| refactor | 重构 |
| perf | 优化 |
| test | 测试 |
| chore | 工具/依赖 |

示例: `feat: 新增笔记分类管理接口`

---

## 注释
- **Java**: 类注释说明模块职责，方法注释说明用途/参数/返回，复杂逻辑行内注释，用 `/** */` JavaDoc
- **Vue**: 组件顶部注释用途，复杂逻辑行内注释
