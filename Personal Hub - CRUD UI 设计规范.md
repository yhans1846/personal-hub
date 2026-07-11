# Personal Hub - CRUD UI 设计规范

> **适用于 Personal Hub 所有业务模块**
>
> 本规范用于统一整个系统的新增、编辑、查看等交互方式，避免传统 Admin 后台风格，打造符合 **Notion + Linear + Raycast + Apple** 设计理念的个人知识管理系统。

---

# 一、设计理念

Personal Hub **不是企业后台（Admin Dashboard）**。

它是一个长期陪伴个人学习和知识管理的产品，因此所有界面应遵循以下原则：

- 简洁（Simple）
- 专注（Focused）
- 克制（Minimal）
- 温暖（Comfortable）
- 高级（Premium）

避免：

- 企业 ERP/OA 风格
- 表单堆砌
- 信息密度过高
- 花哨动画
- 复杂渐变
- 五颜六色的卡片

目标风格：

- Notion
- Linear
- Raycast
- Apple Human Interface Guidelines

---

# 二、统一交互模式

整个系统只保留三种编辑方式。

## ① Dialog（推荐）

适用于：

- Todo
- 标签
- 分类
- 收藏夹
- 文件分类
- 通知规则
- 用户资料
- 其它简单对象

特点：

- 不跳转页面
- 保留当前列表状态
- 操作完成立即关闭
- 用户上下文不会丢失

交互流程：

```
点击新增

↓

弹出 Dialog

↓

填写内容

↓

保存

↓

关闭 Dialog

↓

刷新列表
```

**禁止：**

```
列表

↓

跳转新页面

↓

填写

↓

返回列表
```

---

## ② Drawer（侧边抽屉）

适用于：

- 学习记录
- 阅读记录
- 学习计划
- 文件详情
- 通知详情
- 查看详情

特点：

- 保留列表
- 支持快速切换
- 阅读体验更好

交互：

```
列表

↓

打开 Drawer

↓

编辑

↓

关闭
```

类似：

- Linear Issue
- GitHub Issue
- Notion Page Peek

---

## ③ Full Page（独立页面）

仅适用于：

- Markdown 编辑器
- 日记编辑
- Dashboard 自定义
- 大型配置页面

特点：

需要专注编辑。

不要使用 Dialog。

---

# 三、Dialog 设计规范

所有 Dialog 保持统一。

## 尺寸

推荐：

```
宽度：

520px
```

复杂一点：

```
600px
```

最大：

```
720px
```

不要超过：

```
800px
```

---

## 圆角

统一：

```
16px
```

---

## Padding

统一：

```
24px
```

---

## Header

Header 只保留：

- 标题
- 关闭按钮

例如：

```
新建待办
编辑标签
新增分类
```

不要：

```
图标

彩色背景

渐变
```

---

## Footer

Footer 永远：

```
取消        保存
```

按钮右对齐。

保存按钮：

```
Primary
```

取消：

```
Default
```

不要超过两个按钮。

---

# 四、表单设计规范

不要使用传统后台表单。

错误示例：

```
Label

Input

Label

Input

Label

Input
```

推荐：

```
标题

输入框

说明（可选）


优先级

单选


截止日期

日期选择


备注

多行输入
```

每一个字段都是一个独立内容块。

阅读体验更舒适。

---

## Label

字体：

```
14px

500
```

颜色：

```
--text-primary
```

不要太粗。

---

## 输入框

高度统一：

```
40px
```

Textarea：

```
自动高度

或

4~6 行
```

不要出现：

```
几十个输入框连续排列
```

---

## 字段间距

字段之间：

```
20px
```

Label 与 Input：

```
6px
```

Section：

```
24px
```

整个 Dialog：

```
24px
```

保持统一节奏。

---

# 五、组件封装规范

业务页面禁止直接大量使用 Element Plus。

不要：

```vue
<el-input />

<el-select />

<el-button />

<el-dialog />

<el-form />
```

统一封装：

```
UiDialog

UiInput

UiTextarea

UiSelect

UiDatePicker

UiButton

UiCard

UiSection
```

业务页面只负责：

```
组合业务
```

不要负责：

```
样式
```

这样以后修改 UI：

只改一处。

整个项目统一。

---

# 六、页面布局规范

页面层级：

```
页面

↓

Card

↓

Section

↓

Field
```

而不是：

```
页面

↓

Form

↓

几十个 FormItem
```

推荐：

```
Card

    基本信息

        标题

        描述

    时间

        截止日期

    其它

        标签
```

阅读更自然。

---

# 七、按钮规范

主要按钮：

```
Primary
```

取消：

```
Default
```

危险：

```
Danger
```

不要：

- Success
- Warning
- Info

作为主要按钮。

---

# 八、颜色规范

保持低饱和。

边框：

```
浅灰
```

背景：

```
白色

或

主题背景
```

Hover：

```
轻微变化
```

不要：

- 彩虹色
- 大面积蓝色
- 重阴影
- 渐变背景

---

# 九、阴影规范

默认：

```
无阴影
```

需要层级：

```
极轻阴影
```

不要：

```
box-shadow:

0 12px 40px ...
```

整体保持克制。

---

# 十、CRUD 页面规范

以后所有模块统一：

| 功能           | UI        |
| -------------- | --------- |
| Todo           | Dialog    |
| Tag            | Dialog    |
| Category       | Dialog    |
| Bookmark       | Dialog    |
| File Category  | Dialog    |
| Reading Record | Drawer    |
| Study Record   | Drawer    |
| Study Plan     | Drawer    |
| Markdown       | Full Page |
| Diary          | Full Page |

禁止：

```
TodoCreate.vue

TodoEdit.vue
```

推荐：

```
TodoList.vue

↓

TodoDialog.vue
```

编辑、新增共用一个 Dialog。

---

# 十一、用户体验原则

所有 CRUD 页面必须遵循：

- 不打断用户操作
- 保留上下文
- 尽量不跳转
- 操作路径最短
- 操作反馈明确
- 保持页面一致性

用户应该始终感觉：

> **"我一直停留在当前页面，只是在完成一个操作。"`**

而不是：

```
进入

跳转

返回

刷新

重新定位
```

---

# 十二、Claude Code 修改要求

请根据本规范，对整个项目进行 UI 重构。

要求：

## 1. 修改所有简单 CRUD

改为 Dialog。

例如：

- Todo
- 标签
- 分类
- 收藏夹
- 文件分类

取消独立 Create/Edit 页面。

---

## 2. 修改中等复杂页面

改为 Drawer。

例如：

- 学习记录
- 阅读记录
- 学习计划

---

## 3. 保留大型编辑页面

例如：

- Markdown 编辑器
- 日记编辑
- Dashboard 自定义

继续使用独立页面。

---

## 4. 封装统一 UI 组件

新增：

```
src/components/ui/

UiDialog.vue

UiInput.vue

UiTextarea.vue

UiSelect.vue

UiDatePicker.vue

UiButton.vue

UiSection.vue

UiCard.vue
```

以后业务页面禁止直接使用 Element Plus 原生组件进行页面布局。

---

## 5. 统一设计语言

所有页面统一：

- 留白
- 字体
- 间距
- 圆角
- 按钮
- Header
- Footer
- Dialog
- Drawer

保证整个 Personal Hub 拥有统一的设计语言。

---

# 最终目标

重构后的 Personal Hub 应具备以下特征：

- 不像传统 Admin 后台
- 不像 ERP/OA 系统
- 更接近 Notion、Linear、Raycast 的交互体验
- 所有 CRUD 保持统一
- 页面风格一致
- 操作路径简洁
- 长时间使用不疲劳
- 具有现代、高级、克制的视觉体验