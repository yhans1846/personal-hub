# 文件存储与 Markdown 资源管理设计方案

## 设计目标

- 文件不存数据库，数据库只存元数据
- `StorageService` 统一存储接口，业务层不直接操作磁盘
- Markdown 作为核心格式，每篇笔记 = 一个独立自包含的资源包（Note Bundle）
- 笔记可完整迁移、导出、备份，不依赖 Personal Hub 即可阅读
- 后续可平滑切换 MinIO/OSS 等存储方案

---

# 一、整体目录结构

```
personal-hub.storage.location  (D:/PersonalHub/uploads 或 /opt/personal-hub/uploads)
│
├── uploads/                     ← 通用文件上传（按类型+年月归档）
│   ├── image/2026/07/           ← 图片归类
│   ├── document/2026/07/        ← PDF/Office/文本
│   ├── markdown/2026/07/        ← .md 文件附件
│   ├── diary/                   ← 日记配图（量少，不分年月）
│   ├── avatar/                  ← 用户头像（量少，不分年月）
│   └── other/2026/07/           ← 压缩包等其他
│
├── notes/                       ← 笔记内容 + 配套文件
│   └── {noteId}/
│       ├── note.md              ← Markdown 正文
│       ├── images/              ← 笔记配图
│       └── attachments/         ← 笔记附件
│
├── backup/
├── export/
└── logs/
```

---

# 二、笔记即资源包（Note Bundle）

## 2.1 设计理念

每篇笔记拥有独立资源目录，是一个**自包含（Self-contained）**的知识单元：

- 一篇笔记可以独立迁移
- 一篇笔记可以完整导出
- 图片、附件与笔记一起备份
- 不依赖网络图片、原电脑路径、Personal Hub 服务
- 可迁移到 Obsidian、Typora、VS Code 等 Markdown 工具

## 2.2 目录结构

```
notes/
└── {noteId}/
    ├── note.md
    ├── images/
    │      image-001.png
    │      architecture.png
    └── attachments/
           SpringBoot.pdf
           demo.zip
```

目录使用 noteId（而不是标题），原因：
- 标题可修改
- 避免非法字符
- 避免重复名称
- Windows/Linux 路径兼容

## 2.3 相对路径原则

Markdown 内统一采用相对路径引用资源：

```markdown
# Spring Boot 学习笔记

这是正文内容。

![](images/image-001.png)

架构图：

![](images/architecture.png)

下载资料：

[接口文档](attachments/SpringBoot.pdf)
```

禁止保存以下形式的引用：

```
http://localhost/...
/api/files/...
D:\Pictures\...
file:///D:/...
https://example.com/...
```

最终保存的 Markdown 必须只包含 `images/` 和 `attachments/` 相对路径。

## 2.4 DB 配合

`note_note` 表字段变更（正文存文件，`md_path` 替代 `content`）：

```sql
ALTER TABLE note_note
    CHANGE COLUMN content md_path VARCHAR(500) DEFAULT NULL COMMENT '笔记MD文件路径(notes/{id}/note.md)';
```

笔记正文读写策略：

| 操作 | 策略 |
|------|------|
| 读取笔记正文 | 读文件 `notes/{id}/note.md`，文件不存在返回空 |
| 保存笔记正文 | 只写文件，不同步 DB `content` |
| 全局搜索 | （待定——后续通过全文索引或读取文件实现） |
| 删除笔记 | 删除 DB 记录 + 删除整个 `notes/{id}/` 目录 |

## 2.5 笔记资源不上 file_resource 表

笔记的图片和附件**只存在于文件系统**，不写入 `file_resource` 表。
它们属于笔记内部资源，由笔记目录整体管理，不需要单独的文件元数据追踪。

---

# 三、通用文件存储

## 3.1 目录划分

通用上传的文件按扩展名自动归类：

| 扩展名 | 目标目录 |
|--------|---------|
| jpg/jpeg/png/gif/svg/webp/bmp | uploads/image/ |
| pdf/doc/docx/xls/xlsx/ppt/pptx/txt | uploads/document/ |
| md | uploads/markdown/ |
| 其他（zip/rar/7z等） | uploads/other/ |

按年月分目录：`uploads/image/2026/07/`

## 3.2 file_resource 表

通用上传的文件仍需写入 `file_resource` 表记录元数据，用于文件管理列表、搜索、下载等功能。

新增字段：

```sql
ALTER TABLE file_resource
    ADD COLUMN source VARCHAR(20) DEFAULT NULL COMMENT '上传来源: upload/diary/avatar',
    ADD COLUMN ref_id BIGINT DEFAULT NULL COMMENT '关联实体ID(diary_id等)';
```

## 3.3 删除策略

删除 `file_resource` 记录时，同时删除物理文件。

---

# 四、接口设计

## 4.1 StorageService 接口

```java
public interface StorageService {
    /** 上传文件，保存到 relativePath，返回完整相对路径 */
    String store(MultipartFile file, String relativePath);

    /** 上传文件字节 */
    String store(byte[] data, String relativePath);

    /** 按路径加载文件 */
    Resource load(String relativePath);

    /** 删除文件或目录 */
    void delete(String relativePath);

    /** 判断文件是否存在 */
    boolean exists(String relativePath);

    /** 写入文本内容到文件 */
    void write(String relativePath, String content);

    /** 读取文本内容 */
    String read(String relativePath);

    /** 移动/重命名文件 */
    void move(String sourcePath, String targetPath);

    /** 复制文件 */
    void copy(String sourcePath, String targetPath);
}
```

## 4.2 StorageProperties

```java
@ConfigurationProperties(prefix = "personal-hub.storage")
public class StorageProperties {
    /** 文件存储根目录，默认 ./data/uploads */
    private Path location = Paths.get("data", "uploads");

    /** 单文件最大字节，默认 50MB */
    private long maxSize = 50 * 1024 * 1024;
}
```

## 4.3 API 接口

### 通用文件 API（控制器：FileController）

路径前缀：`/api/files`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/files | 文件列表（分页） |
| GET | /api/files/{id} | 详情 |
| POST | /api/files/upload | 上传（multipart） |
| GET | /api/files/{id}/download | 下载 |
| GET | /api/files/{id}/preview | 预览（inline，图片/PDF） |
| DELETE | /api/files/{id} | 删除（逻辑+物理） |

### 笔记图片 API（控制器：NoteController 或独立控制器）

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /api/notes/{noteId}/images | 上传笔记配图 |
| POST | /api/notes/{noteId}/attachments | 上传笔记附件 |
| GET | /api/notes/{noteId}/images/{filename} | 获取笔记图片 |
| GET | /api/notes/{noteId}/attachments/{filename} | 获取笔记附件 |

笔记资源上传返回：

```json
{
    "url": "images/image-003.png",
    "name": "image-003.png"
}
```

前端直接在 Markdown 中插入 `![](images/image-003.png)`。

---

# 五、Markdown 导入

## 5.1 概述

导入 Markdown 时，自动解析并本地化所有资源引用，使导入后的笔记完全不依赖外部资源。

支持导入的资源类型：

| 类型 | 示例 |
|------|------|
| 网络图片 | `![](https://example.com/logo.png)` |
| Windows 绝对路径 | `![](D:\Pictures\logo.png)` |
| Linux 绝对路径 | `![](/home/user/logo.png)` |
| file:// 协议 | `![](file:///D:/Pictures/logo.png)` |
| 相对路径 | `![](./images/logo.png)` |
| 附件引用 | `[文档](./docs/api.pdf)` |

## 5.2 导入流程

```
选择 Markdown 文件
        │
        ▼
解析 Markdown 内容
        │
        ▼
扫描所有资源引用（正则匹配 ![]() / []()）
        │
        ▼
逐个处理资源：
   ├── http/https  → 下载到 images/ 或 attachments/
   ├── file://     → 复制到 images/ 或 attachments/
   ├── Windows 路径 → 复制到 images/ 或 attachments/
   ├── Linux 路径   → 复制到 images/ 或 attachments/
   └── 相对路径     → 定位文件并复制到 images/ 或 attachments/
        │
        ▼
生成唯一文件名，避免覆盖
        │
        ▼
修改 Markdown 中所有引用为相对路径
        │
        ▼
保存 note.md 到 notes/{noteId}/note.md
        │
        ▼
保存数据库记录
```

## 5.3 命名策略

导入的资源按导入顺序命名，避免与已有文件冲突：

- `image-001.png`, `image-002.png`, ...
- 保留原始文件名（如果唯一）：`logo.png`, `architecture.png`
- 重名自动加后缀：`logo-1.png`, `logo-2.png`

## 5.4 导入后目录示例

```
notes/10001/
    note.md
    images/
        image-001.png
        architecture.png
        logo.png
    attachments/
        api.pdf
```

Markdown 内容：

```markdown
# Spring Boot 学习笔记

![](images/image-001.png)

架构图：

![](images/architecture.png)

[接口文档](attachments/api.pdf)
```

导入完成后，整篇笔记不再依赖任何外部资源。

---

# 六、Markdown 导入方式

## 6.1 单文件导入

用户选择一个 .md 文件，系统解析并导入。

## 6.2 目录导入（ZIP / 文件夹）

支持导入 Obsidian/Typora 等导出的完整笔记目录：

```
Java/
    Java.md
    images/
    attachments/
```

处理流程：

```
上传 ZIP 或选择目录
        │
        ▼
解压/扫描
        │
        ▼
创建新的 noteId
        │
        ▼
复制整个目录到 notes/{noteId}/
        │
        ▼
解析 note.md，验证所有资源引用
        │
        ▼
保存数据库
```

几乎不需要修改 Markdown，因为源目录已经使用相对路径。

---

# 七、导出

## 7.1 单篇笔记导出

```
notes/10001/
    note.md
    images/
    attachments/
```

打包为：

```
Spring Boot 学习笔记.zip
    Spring Boot 学习笔记/
        note.md
        images/
        attachments/
```

Markdown 无需任何路径转换，相对路径保持不变。

## 7.2 批量导出

选择多篇笔记，每篇独立目录，打包为一个 ZIP。

---

# 八、资源清理

## 8.1 保存时清理

每次保存 Markdown 时，扫描当前引用的资源，删除 `images/` 和 `attachments/` 中未被引用的文件。

## 8.2 定时任务清理（推荐）

后台定时扫描 notes/ 目录，比对 DB 中存在的笔记，删除已删除笔记的资源目录。

更安全，避免误删正在编辑中的资源。

## 8.3 删除笔记

删除笔记时，同时删除整个 `notes/{id}/` 目录，不会产生孤立垃圾。

```
删除 DB 记录
    │
    ▼
删除 notes/{noteId}/ 目录
```

---

# 九、数据备份与恢复

## 备份

```
D:/PersonalHub/uploads/
    notes/          ← 所有 Markdown 和笔记资源
    uploads/        ← 通用上传文件
```

备份 `uploads/` 目录即可备份所有用户数据。

## 恢复

```
恢复 uploads/ 目录
    │
    ▼
恢复数据库
    │
    ▼
完成
```

Markdown 和资源文件无需额外处理。

## 数据迁移

```
直接复制 notes/ 目录
    │
    ▼
数据库同步 note 表（title/category/tag...）
    │
    ▼
完成
```

无需重新处理 Markdown 和资源文件。

---

# 十、配置

application.yml / application-dev.yml：

```yaml
personal-hub:
  storage:
    location: D:/PersonalHub/uploads
    max-size: 52428800   # 50MB
```

删除旧配置 `app.upload.dir`、`app.upload.max-size`。

---

# 十一、不变的部分

以下功能不受本次重构影响，保持不动：

- `FileCategoryService` / `FileCategoryController` — 文件分类
- 前端 `List.vue` / `CategoryManage.vue` — 文件管理页面
- `FileQueryDTO` — 查询参数
- `FileVO` — 返回结构
- `FileController` 现有接口 URL 和参数不变
- 文件分类 CRUD 流程不变

---

# 十二、核心设计原则

## Resource Localization（资源本地化）
所有资源统一下载或复制到笔记目录，不依赖网络、原路径、外部服务。

## Relative Path（相对路径）
Markdown 永远使用 `images/`、`attachments/` 相对路径，保证独立阅读能力。

## Self-contained（自包含）
每篇笔记都是一个完整资源包，任何时候复制整个目录即可完整迁移。

## Portable（可迁移）
导出的 Markdown 可直接用于 Obsidian、Typora、VS Code、GitHub、Hugo、Docsify 等生态，无需转换。

## Open Format（开放格式）
Personal Hub 不锁定数据。Markdown 保持标准格式，即使未来不再使用 Personal Hub，用户仍可直接使用所有数据。
