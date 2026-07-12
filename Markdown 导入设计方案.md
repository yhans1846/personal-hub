# Markdown 导入设计方案

> 更新时间：2026-07
>
> 本文档用于规范 Personal Hub 的 Markdown 导入能力，支持本地 Markdown、网络 Markdown、Obsidian、Typora 等来源，并保证图片、附件等资源能够完整导入、迁移及导出。

---

# 一、设计目标

Markdown 导入不仅仅是导入一段文本，而是导入**完整的笔记资源**。

目标：

- 支持本地 Markdown 文件导入
- 自动导入 Markdown 引用的图片
- 自动导入附件（后续）
- 自动修正 Markdown 内资源引用
- 保证导入后的笔记可以独立存在
- 后续支持一键导出整个笔记目录
- 后续支持 Obsidian / Typora 等工具迁移

导入完成后，不依赖原始文件目录即可正常显示所有资源。

---

# 二、为什么不能只有"粘贴 Markdown"

例如：

```markdown
# Python 学习

![](img/demo.png)

![](../assets/logo.png)
```

如果用户只是复制 Markdown 内容：

```
# Python 学习

![](img/demo.png)
```

服务器只能拿到：

```
Markdown 字符串
```

此时：

```
img/demo.png
```

到底指的是：

```
D:\note\img\demo.png
```

还是：

```
E:\WorkSpace\Python\learn\img\demo.png
```

服务器无法知道。

因为：

> **相对路径必须依赖 Markdown 文件所在目录才能解析。**

因此：

**仅靠 Markdown 内容无法恢复所有资源。**

---

# 三、导入方式设计

建议支持两种导入方式。

---

## 方式一：从 Markdown 文件导入（推荐）

入口：

```
导入

↓

Markdown 文件
```

用户选择：

```
E:\WorkSpace\Python\learn\python_base_learn\note.md
```

此时系统能够获得：

```
Markdown 文件

↓

完整路径

↓

E:\WorkSpace\Python\learn\python_base_learn\
```

因此：

Markdown：

```markdown
![](img/demo.png)
```

能够解析为：

```
E:\WorkSpace\Python\learn\python_base_learn\img\demo.png
```

如果：

```markdown
![](../assets/logo.png)
```

解析：

```
E:\WorkSpace\Python\learn\assets\logo.png
```

所有资源都可以正确找到。

---

## 方式二：粘贴 Markdown 内容

入口：

```
导入

↓

Markdown 内容
```

适用于：

- ChatGPT
- Claude
- DeepSeek
- 网页复制
- GitHub README
- 博客文章

支持：

✅ 网络图片

```
https://xxx/image.png
```

✅ Base64

```
data:image/png;base64,...
```

支持自动下载。

但是：

对于：

```
img/demo.png
```

```
../assets/logo.png
```

```
../../images/test.png
```

由于缺少 Markdown 文件位置：

**无法解析。**

系统应提示：

> 检测到 Markdown 中存在相对路径资源，请使用"从 Markdown 文件导入"以自动导入图片及附件。

---

# 四、导入流程

## Markdown 文件导入

```
用户选择 note.md

        │

        ▼

读取 Markdown

        │

        ▼

解析 Markdown AST

        │

        ▼

提取所有资源

        │

        ▼

根据 Markdown 所在目录

解析相对路径

        │

        ▼

复制资源到当前笔记目录

        │

        ▼

修改 Markdown 引用

        │

        ▼

保存数据库
```

---

# 五、资源解析规则

统一建立：

```
MarkdownResourceResolver
```

负责：

> 将 Markdown 中所有资源解析为真实路径。

支持：

## 1、HTTP

```
https://example.com/a.png
```

直接下载。

---

## 2、HTTPS

```
https://example.com/logo.jpg
```

直接下载。

---

## 3、网站根路径

```
/images/logo.png
```

需要：

```
baseUrl

+

/images/logo.png
```

得到：

```
https://example.com/images/logo.png
```

---

## 4、Markdown 相对路径

例如：

```
img/demo.png
```

转换：

```
Markdown目录

+

img/demo.png
```

得到：

```
E:\WorkSpace\Python\learn\python_base_learn\img\demo.png
```

---

## 5、父目录

例如：

```
../assets/logo.png
```

解析：

```
normalize()

↓

E:\WorkSpace\Python\learn\assets\logo.png
```

---

## 6、本地绝对路径（可选）

例如：

```
C:\image\a.png
```

Linux：

```
/home/user/image.png
```

macOS：

```
/Users/admin/image.png
```

如果安全策略允许：

直接复制。

---

## 7、Base64

例如：

```
data:image/png;base64,...
```

解码后保存。

---

# 六、导入后的目录结构

导入前：

```
python_base_learn

├── note.md
├── img
│   ├── 1.png
│   ├── 2.png
│
├── assets
│   └── logo.png
```

导入后：

```
storage

notes

└── 1001

    ├── note.md

    ├── images

    │   ├── uuid1.png
    │   ├── uuid2.png

    └── attachments
```

原始目录：

不再依赖。

---

# 七、Markdown 自动修正

例如：

原始：

```markdown
![](img/demo.png)
```

导入后：

```markdown
![](images/uuid-demo.png)
```

或者：

```markdown
![](images/demo.png)
```

以后：

无论：

- 导出
- 迁移
- 备份

图片始终位于当前笔记目录。

---

# 八、导出

导出：

```
note.md

+

images

+

attachments
```

生成：

```
MyNote.zip

├── note.md

├── images

│   ├── demo.png

└── attachments
```

导出的 Markdown：

```markdown
![](images/demo.png)
```

无需再次修改。

---

# 九、未来扩展

后续导入能力：

## Obsidian

```
Vault

↓

扫描 Markdown

↓

导入
```

---

## Typora

直接导入。

---

## GitHub

输入：

```
README.md URL
```

自动下载：

- Markdown
- 图片

---

## ZIP 导入

例如：

```
note.zip

├── note.md

├── images
```

自动恢复。

---

## WebDAV

支持直接同步。

---

# 十、模块设计

建议新增：

```
ph-import
```

负责：

```
Markdown 导入

HTML 导入（后续）

ZIP 导入（后续）

Obsidian 导入（后续）
```

内部职责：

```
ImportService
│
├── MarkdownImporter
├── ResourceScanner
├── MarkdownResourceResolver
├── ResourceDownloader
├── MarkdownRewriter
└── ImportReport
```

职责说明：

| 组件                     | 职责                                               |
| ------------------------ | -------------------------------------------------- |
| MarkdownImporter         | 导入入口，负责整体流程                             |
| ResourceScanner          | 扫描 Markdown 中所有图片、附件等资源               |
| MarkdownResourceResolver | 将相对路径、绝对路径、URL 等统一解析为真实资源地址 |
| ResourceDownloader       | 下载网络资源或复制本地资源                         |
| MarkdownRewriter         | 重写 Markdown 中的资源引用                         |
| ImportReport             | 输出导入结果、警告及失败信息                       |

---

# 十一、设计原则

1. **优先使用"从 Markdown 文件导入"**，因为能够获取 Markdown 所在目录，完整解析所有相对路径资源。
2. **"粘贴 Markdown 内容"定位为快捷导入**，仅保证文本和可解析的网络资源导入成功。
3. **所有资源解析统一经过 `MarkdownResourceResolver`**，业务代码不得直接拼接路径或下载资源。
4. **导入后资源全部复制到当前笔记目录**，保证笔记完全独立，不依赖原始文件位置。
5. **导入时统一重写 Markdown 中的资源引用**，导出时无需再次调整路径。
6. **所有导入流程生成导入报告**，记录成功导入、跳过、失败的资源，便于用户排查问题。
7. **后续所有导入能力（Obsidian、Typora、GitHub、ZIP、WebDAV）均复用同一套资源解析与重写机制，避免重复实现。**