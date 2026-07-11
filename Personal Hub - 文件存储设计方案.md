# Personal Hub - 文件存储设计方案

## 设计目标

Personal Hub 采用**本地文件系统 + 数据库存储元数据**的方式管理文件。

设计原则：

- 文件不存数据库
- 数据库仅保存文件元数据
- 文件目录与项目代码分离
- 保存相对路径，不保存绝对路径
- 后续可平滑迁移到 Docker、WebDAV、MinIO、OSS 等存储方案

---

# 整体架构

```
Browser
    │
    ▼
REST API
    │
    ▼
StorageService
    │
    ├── LocalStorageService（当前）
    │
    ├── MinioStorageService（后续）
    │
    ├── WebDavStorageService（后续）
    │
    └── OssStorageService（后续）
```

业务模块仅依赖 `StorageService` 接口，不直接操作磁盘。

---

# 存储目录

推荐使用独立的数据目录，而不是放在 Spring Boot 项目内部。

Windows：

```
D:/
└── PersonalHub/
    ├── uploads/
    ├── backup/
    ├── export/
    └── logs/
```

Linux：

```
/opt/personal-hub/
    uploads/
    backup/
    export/
    logs/
```

这样项目升级、重新部署不会影响用户数据。

---

# uploads 目录设计

```
uploads/
├── image/
│   ├── 2026/
│   │   ├── 07/
│   │   └── 08/
│
├── document/
│   ├── 2026/
│   └── ...
│
├── markdown/
│
├── diary/
│
├── avatar/
│
└── other/
```

按照：

> 文件类型 → 年 → 月

进行分类。

例如：

```
uploads/image/2026/07/
uploads/document/2026/07/
uploads/avatar/
```

避免所有文件放在同一个目录。

---

# 文件命名

真实文件名采用 UUID。

例如：

```
93c2c695-acde-4ab2-acde-acde34ef.png
```

数据库保存：

| 字段        | 内容                               |
| ----------- | ---------------------------------- |
| name        | Java学习路线.pdf                   |
| stored_name | 93c2c695-acde-4ab2-acde.pdf        |
| path        | document/2026/07/93c2c695-acde.pdf |

下载时仍返回原始名称：

```
Java学习路线.pdf
```

避免重名覆盖。

---

# 数据库存储

数据库只保存文件元数据。

示例：

| 字段        | 示例                               |
| ----------- | ---------------------------------- |
| id          | 1                                  |
| name        | Java学习路线.pdf                   |
| stored_name | 93c2c695-acde.pdf                  |
| path        | document/2026/07/93c2c695-acde.pdf |
| size        | 125648                             |
| type        | pdf                                |
| mime_type   | application/pdf                    |
| category_id | 1                                  |
| created_at  | ...                                |

**path 保存相对路径。**

不要保存：

```
D:\PersonalHub\uploads\document\...
```

也不要保存：

```
http://localhost/uploads/...
```

---

# Spring Boot 配置

application.yml

```yaml
personal-hub:
  storage:
    location: D:/PersonalHub/uploads
```

对应配置类：

```java
@ConfigurationProperties(prefix = "personal-hub.storage")
public class StorageProperties {

    /**
     * 文件存储根目录
     */
    private String location;

}
```

业务统一读取：

```
storage.location
```

不允许硬编码磁盘路径。

---

# StorageService 设计

定义统一存储接口：

```java
public interface StorageService {

    StorageFile upload(MultipartFile file);

    Resource load(String path);

    void delete(String path);

    boolean exists(String path);

}
```

当前实现：

```
LocalStorageService
```

未来可扩展：

```
MinioStorageService
WebDavStorageService
OssStorageService
```

业务层无需修改。

---

# 上传流程

```
客户端上传文件
        │
        ▼
FileController
        │
        ▼
StorageService.upload()
        │
        ▼
生成 UUID
        │
        ▼
创建目录
        │
        ▼
保存文件
        │
        ▼
返回 StorageFile
        │
        ▼
保存 file_resource 数据
```

---

# 下载流程

```
GET /api/files/{id}/download
        │
        ▼
查询数据库
        │
        ▼
获取 path
        │
        ▼
StorageService.load(path)
        │
        ▼
读取文件
        │
        ▼
ResponseEntity<Resource>
```

下载时设置：

```
Content-Disposition

attachment;
filename="Java学习路线.pdf"
```

用户始终看到原始文件名。

---

# 文件访问

统一通过 API 访问。

上传：

```
POST /api/files/upload
```

下载：

```
GET /api/files/{id}/download
```

预览：

```
GET /api/files/{id}/preview
```

删除：

```
DELETE /api/files/{id}
```

不直接暴露：

```
/uploads/**
```

这样方便：

- 登录校验
- 权限控制
- 下载统计
- 文件预览
- 图片压缩
- 防盗链

---

# Docker 部署

容器内：

```
/data/uploads
```

宿主机：

```
/opt/personal-hub/uploads
```

挂载：

```bash
-v /opt/personal-hub/uploads:/data/uploads
```

application.yml：

```yaml
personal-hub:
  storage:
    location: /data/uploads
```

无需修改业务代码。

---

# 后续扩展

StorageService 可以逐步支持：

- Local File
- Docker Volume
- NAS
- WebDAV
- MinIO
- 阿里云 OSS
- 腾讯 COS
- Amazon S3

业务代码保持不变，仅替换 StorageService 实现。

---

# 推荐方案

```
PersonalHub/
├── uploads/
│   ├── image/
│   │   └── 2026/07/
│   ├── document/
│   │   └── 2026/07/
│   ├── avatar/
│   ├── diary/
│   ├── markdown/
│   └── other/
├── backup/
├── export/
└── logs/
```

数据库：

- 保存文件元数据
- path 保存相对路径

Spring Boot：

- application.yml 配置存储根目录
- StorageService 统一管理文件操作

该方案满足 Personal Hub 当前本地存储需求，同时具备良好的可维护性和后续扩展能力。