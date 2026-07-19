# 后端架构收口实现计划

> **面向 AI 代理的工作者：** 使用 executing-plans 逐任务实现。步骤用复选框跟踪。

**目标：** Storage 去 MultipartFile、文件门面、通知 Scanner（A）、状态枚举统一

**架构：** 见 `docs/superpowers/specs/2026-07-19-backend-architecture-cleanup-design.md`

**技术栈：** Spring Boot 3.3 / Java 21 / MyBatis-Plus

---

### 任务 1：Storage 去掉 MultipartFile

**文件：**
- 修改：`ph-common/.../StorageService.java`、`LocalStorageServiceImpl.java`
- 修改：Note/Diary/File 上传实现、`UserController`
- 测试：更新 `LocalStorageServiceImplTest`

- [ ] 删除 `store(MultipartFile)`，调用方改 `store(file.getBytes(), path)`
- [ ] 运行测试并 commit

### 任务 2：文件门面

**文件：**
- 创建：`StoragePaths`、`FileUploadValidator`、`FileAssetService`(+Impl)
- 修改：Note/Diary/File/Avatar 委托门面；FileController 下载走 Service

- [ ] 实现并接入
- [ ] commit

### 任务 3：PlanningNotificationSource（方案 A）

**文件：**
- 创建：`ph-system/.../PlanningNotificationSource.java`、Candidate
- 创建：`ph-biz/.../PlanningNotificationSourceImpl.java`
- 修改：`NotificationServiceImpl`、`NotificationMapper`(+xml)

- [ ] 实现 Scanner，删跨域 SQL
- [ ] commit

### 任务 4：枚举替换魔法数

**文件：**
- 修改：`StudyPlanServiceImpl`、`TodoTaskServiceImpl`、Scanner 内枚举

- [ ] 替换并 commit
- [ ] 更新 CHANGELOG / STYLE_GUIDE / PROJECT（若需）
