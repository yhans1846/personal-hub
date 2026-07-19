# 后端架构收口设计（2026-07-19）

> 已批准：Storage 解耦 · 文件门面 · 通知 Scanner（方案 A）· 状态枚举

## 1. Storage 与 Web 解耦

- `StorageService` 删除 `store(MultipartFile, …)`
- 新增/保留：`store(byte[] data, String relativePath)`；可选 `store(InputStream, long, String)`
- 调用方读取 Multipart 字节后调用
- **不**新建 Maven `ph-storage` 模块

## 2. 文件统一门面

- `StoragePaths`：路径常量与 builder
- `FileUploadValidator`：空文件 / maxSize / 可选 MIME
- `FileAssetService`：storeBytes / load / delete（委托 Storage）
- Note/Diary/File/Avatar 服务委托门面；Controller 不直调 Storage

## 3. 通知（方案 A）

- 在 **ph-system** 定义 `PlanningNotificationSource` 接口 + Candidate DTO
- **ph-biz** 实现：用 Todo/StudyPlan Mapper 查询，枚举状态码
- `NotificationServiceImpl.generateSystemNotifications` 调 Source，再 `create`
- 删除 `NotificationMapper.xml` 中 `selectOverdueTodos` / `selectDeadlinePlans` / `selectCompletedPlans`
- Job / `POST /check` 行为不变

## 4. 魔法状态码

- `StudyPlanServiceImpl.stats` → `StudyPlanStatus.*.getCode()`
- `TodoTaskServiceImpl` 默认 → `TodoPriority.MEDIUM.getCode()`
- Scanner SQL/Wrapper 使用枚举 code + `Flags`

## 验收

- `mvn -pl ph-common,ph-system,ph-biz -am test` 通过
- 通知生成仍可由 Job/`/check` 触发
- 上传路径与对外 API 不变
