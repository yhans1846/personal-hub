# Personal Hub - 笔记回收站与审计日志改造

请按照 Personal Hub 项目的代码规范、数据库规范和 UI 规范完成以下功能。

## 一、笔记增加删除时间

### 数据库

为 `note` 表新增字段：

```sql
deleted_at DATETIME NULL COMMENT '删除时间'
```

规则：

- 新建笔记：`deleted_at = NULL`
- 正常编辑：不修改该字段
- 删除笔记：
  - `deleted = 1`
  - `deleted_at = 当前时间`
- 恢复笔记：
  - `deleted = 0`
  - `deleted_at = NULL`

> 删除时间用于回收站排序、展示以及后续自动清理。

---

## 二、增加统一审计日志（Audit Log）

为了后续支持所有业务日志，不要新增 `note_log`。

请设计一个通用审计日志表：

例如：

```sql
audit_log
```

建议字段：

| 字段        | 说明                        |
| ----------- | --------------------------- |
| id          | 主键                        |
| module      | 模块（NOTE、TODO、FILE...） |
| business_id | 业务ID                      |
| action      | 操作类型                    |
| content     | 操作描述                    |
| operator_id | 操作用户                    |
| created_at  | 操作时间                    |

### 本次实现记录以下日志

#### 删除笔记

```
module:
NOTE

action:
DELETE

content:
删除笔记：《Spring Boot 学习》
```

#### 恢复笔记

```
module:
NOTE

action:
RESTORE

content:
恢复笔记：《Spring Boot 学习》
```

要求：

后续 Todo、学习记录、文件管理等模块都可以复用该表。

不要设计成仅服务于笔记。

---

## 三、回收站页面改造

优化回收站列表。

新增显示字段：

- 标题
- 分类
- 标签
- 创建时间
- 最后更新时间
- 删除时间

时间格式统一：

```
YYYY-MM-DD HH:mm
```

删除时间建议使用强调色显示（例如红色文字或 Danger Tag）。

---

## 四、新增"只读查看"

回收站中的笔记支持：

```
查看
```

点击后：

不要恢复笔记。

不要进入编辑器。

而是：

**新开浏览器标签页（New Tab）**

打开：

```
/notes/{id}/preview
```

要求：

- 只读模式
- 禁止编辑
- 禁止保存
- 保留 Markdown 渲染
- 图片正常显示
- 可以复制内容
- 可以查看附件

页面顶部增加：

```
该笔记位于回收站，仅支持查看。
```

提供：

```
恢复按钮（可选）

关闭页面
```

---

## 五、回收站排序

默认按照：

```
删除时间 DESC
```

最新删除排最前。

---

## 六、接口调整

新增：

### 查询回收站

```
GET /api/notes/recycle
```

返回：

- 创建时间
- 更新时间
- 删除时间

### 预览

```
GET /api/notes/{id}/preview
```

说明：

返回笔记详情。

即使：

```
deleted = 1
```

也允许查看。

但：

不能修改。

---

## 七、后端要求

恢复、删除统一放在 Service。

删除流程：

```
更新 deleted

↓

更新 deleted_at

↓

写入 Audit Log
```

恢复流程：

```
更新 deleted

↓

清空 deleted_at

↓

写入 Audit Log
```

保证事务一致性。

---

## 八、前端要求

回收站页面：

新增：

```
查看
```

按钮。

点击：

```
window.open(
    `/notes/${id}/preview`,
    '_blank'
)
```

不要在当前页面跳转。

---

## 九、设计要求

保持 Personal Hub 的设计风格：

- Notion
- Linear
- Raycast
- Apple

回收站更偏向"历史记录"。

避免传统后台风格。

所有时间列保持统一格式。

删除时间作为最重要信息进行突出显示。

---

## 十、扩展性要求

本次新增的 Audit Log 必须设计为整个系统通用能力。

未来以下模块均可直接使用：

- Note
- Todo
- Study
- Reading
- Diary
- File
- Bookmark
- Tag
- User

后续增加任何操作日志时，不需要重新设计数据库结构，只需新增对应的 `module` 和 `action` 即可。



我再建议你补充两个功能（很适合 Personal Hub）

恢复位置记录

恢复后保留原分类、原标签，不需要重新选择。

回收站详情

增加一列 删除原因（目前固定为"用户删除"即可）。

后续如果增加"自动归档"、"自动清理"等功能，这个字段就能复用。



另外，audit_log 我建议不要只叫日志，而是直接作为整个项目的审计模块（Audit），以后登录日志、导出记录、数据恢复、AI 操作记录都可以统一记录进去，长期维护会更加清晰。 

审计代码放入系统模块