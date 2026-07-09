# Personal Hub - 分步开发计划

> 每完成一步提交一次代码。开发过程中新生成的计划写入此文件以免遗忘。

## 进度总览

| Step | 内容 | 状态 |
|------|------|------|
| 1 | 后端脚手架 | ✅ 已完成 |
| 2 | 前端脚手架 | ✅ 已完成 |
| 3 | 数据库初始化 + 用户认证 | ✅ 已完成 |
| 4 | Markdown 笔记模块 | 🔲 待开发 |
| 5 | 学习记录模块 | 🔲 待开发 |
| 6 | Todo 模块 | 🔲 待开发 |
| 7 | 文件管理模块 | 🔲 待开发 |
| 8 | 后续：日记/收藏夹/学习计划/阅读记录 | 🔲 |

---

## Step 1: 后端脚手架 ✅

**文件清单：**
- `backend/pom.xml` — Spring Boot 3.3.5 + MyBatis-Plus 3.5.7 + JWT + Security + Redis
- `backend/src/main/resources/application.yml` — 公共配置（端口8080、MyBatis-Plus、Jackson）
- `backend/src/main/resources/application-dev.yml` — 开发环境（MySQL/Redis连接，已gitignore）
- `backend/src/main/java/com/personalhub/PersonalHubApplication.java` — 启动类
- `backend/src/main/java/com/personalhub/common/result/Result.java` — 统一返回
- `backend/src/main/java/com/personalhub/common/result/PageResult.java` — 分页返回
- `backend/src/main/java/com/personalhub/common/exception/` — 全局异常处理（Global、Business、Unauthorized、NotFound）
- `backend/src/main/java/com/personalhub/common/config/MyBatisPlusConfig.java` — 分页插件 + 自动填充
- `backend/src/main/java/com/personalhub/common/config/JacksonConfig.java` — LocalDateTime序列化
- `backend/src/main/java/com/personalhub/common/config/RedisConfig.java` — Redis序列化配置
- `backend/src/test/.../PersonalHubApplicationTests.java` — 启动测试

**提交:** `chore: 搭建Spring Boot 3后端脚手架`

---

## Step 2: 前端脚手架 ✅

**文件清单：**
- `frontend/` — pnpm create vite（vue-ts模板）+ vue-router + pinia + axios + element-plus
- `frontend/vite.config.ts` — @别名 + API代理到8080
- `frontend/src/main.ts` — Pinia + Router + ElementPlus
- `frontend/src/App.vue` — router-view
- `frontend/src/api/request.ts` — Axios实例（拦截器 + Token注入）
- `frontend/src/router/index.ts` — 路由（/login, /）
- `frontend/src/store/authStore.ts` — Pinia认证状态
- `frontend/src/types/common.ts` — Result<T>, PageResult<T>
- `frontend/src/views/Login.vue` — 登录页占位
- `frontend/src/views/Dashboard.vue` — 首页占位
- `frontend/src/components/AppLayout.vue` — 后台布局

**提交:** `chore: 搭建Vue 3 + Vite前端脚手架`

---

## Step 3: 数据库初始化 + 用户认证 ✅

**数据库：**
- `sql/init.sql` — 10张第一阶段表

**后端（auth模块）：**
- `module/auth/controller/AuthController.java` — POST /api/auth/login, /api/auth/logout
- `module/auth/service/AuthService(Impl).java`
- `module/auth/dto/LoginDTO.java`
- `module/auth/vo/LoginVO.java`

**后端（user模块）：**
- `module/user/entity/User.java` — MyBatis-Plus Entity
- `module/user/mapper/UserMapper.java`
- `module/user/service/UserService(Impl).java`
- `module/user/controller/UserController.java` — GET/PUT /api/user/profile, PUT /api/user/password
- `module/user/dto/UserProfileUpdateDTO.java`, `PasswordUpdateDTO.java`
- `module/user/vo/UserProfileVO.java`

**后端（安全）：**
- `common/util/JwtUtil.java` — JWT生成/解析
- `common/filter/JwtAuthenticationFilter.java` — JWT过滤器
- `common/config/SecurityConfig.java` — Spring Security + BCrypt

**前端：**
- `frontend/src/api/authApi.ts` — 登录API
- `frontend/src/store/authStore.ts` — +login/logout逻辑
- `frontend/src/router/index.ts` — +路由守卫（未登录跳转）
- `frontend/src/views/Login.vue` — Element Plus登录表单
- `frontend/src/components/AppLayout.vue` — +用户名+退出按钮

**测试用户:** admin / 123456（BCrypt加密）

**验证:** `POST /api/auth/login` → 200 + JWT Token, `GET /api/user/profile` → 200

**提交:** `feat: 数据库初始化与用户认证模块（JWT登录）`

---

## Step 4: Markdown 笔记模块

### 后端
- `module/note/entity/Note.java`, `NoteCategory.java`, `NoteTag.java` — Entity
- `module/note/entity/NoteCategoryRel.java`, `NoteTagRel.java` — 关联表Entity
- `module/note/mapper/NoteMapper.java`, `NoteCategoryMapper.java`, `NoteTagMapper.java`
- `module/note/mapper/NoteCategoryRelMapper.java`, `NoteTagRelMapper.java`
- `module/note/service/NoteService.java` + `NoteServiceImpl.java`
- `module/note/service/NoteCategoryService.java` + impl
- `module/note/service/NoteTagService.java` + impl
- `module/note/controller/NoteController.java` — CRUD + 搜索 + 收藏 + 回收站
- `module/note/controller/NoteCategoryController.java` — CRUD
- `module/note/controller/NoteTagController.java` — CRUD
- `module/note/dto/NoteCreateDTO.java`, `NoteQueryDTO.java`
- `module/note/vo/NoteVO.java`

### 前端
- `frontend/src/api/noteApi.ts` — 笔记/分类/标签 API
- `frontend/src/types/note.ts` — 笔记类型
- `frontend/src/views/notes/NoteList.vue` — 笔记列表（分页/搜索/筛选）
- `frontend/src/views/notes/NoteEditor.vue` — Markdown编辑器
- `frontend/src/views/notes/NoteCategoryManage.vue` — 分类管理
- `frontend/src/views/notes/NoteTagManage.vue` — 标签管理
- `frontend/src/views/notes/RecycleBin.vue` — 回收站

**验证:** 新建/编辑/删除笔记 + 分类标签管理 + 收藏 + 回收站恢复

---

## Step 5: 学习记录模块

### 后端
- `module/study/entity/StudyRecord.java`
- `module/study/mapper/StudyRecordMapper.java`
- `module/study/service/StudyRecordService.java` + impl
- `module/study/controller/StudyRecordController.java`
- `module/study/dto/StudyRecordCreateDTO.java`, `StudyRecordQueryDTO.java`
- `module/study/vo/StudyRecordVO.java`

### 前端
- `frontend/src/api/studyApi.ts`
- `frontend/src/types/study.ts`
- `frontend/src/views/study/StudyRecordList.vue`
- `frontend/src/views/study/StudyRecordForm.vue`

---

## Step 6: Todo 模块

### 后端
- `module/todo/entity/TodoTask.java`
- `module/todo/mapper/TodoTaskMapper.java`
- `module/todo/service/TodoTaskService.java` + impl
- `module/todo/controller/TodoTaskController.java`
- `module/todo/dto/TodoCreateDTO.java`, `TodoQueryDTO.java`
- `module/todo/vo/TodoVO.java`

### 前端
- `frontend/src/api/todoApi.ts`
- `frontend/src/types/todo.ts`
- `frontend/src/views/todo/TodoList.vue`

---

## Step 7: 文件管理模块

### 后端
- `module/file/entity/FileResource.java`, `FileCategory.java`
- `module/file/mapper/FileResourceMapper.java`, `FileCategoryMapper.java`
- `module/file/service/FileService.java` + impl
- `module/file/controller/FileController.java`, `FileCategoryController.java`
- 配置 `spring.servlet.multipart.*`

### 前端
- `frontend/src/api/fileApi.ts`
- `frontend/src/types/file.ts`
- `frontend/src/views/files/FileList.vue`
- `frontend/src/views/files/FileCategoryManage.vue`

---

## 后续步骤（第二阶段）
- 日记模块
- 收藏夹模块
- 学习计划模块
- 阅读记录模块
- Dashboard & 数据统计
- 全局搜索
- 统一标签系统
