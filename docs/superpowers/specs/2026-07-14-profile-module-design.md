# Personal Hub V4 - 个人资料（Profile）模块

> Date: 2026-07-14
> Status: Draft

## 1. 设计目标

在 Settings 页面新增"个人资料"Tab，用于维护用户基本信息。与系统设置（工作台/阅读/外观/高级）并列。

**仅负责：** 身份信息、联系方式、个人介绍、社交链接  
**不包含：** 修改密码、通知设置、系统偏好、Dashboard 配置

## 2. 页面布局

Settings 下新增 Tab，单列 Card 布局：

```
┌─ 个人资料 | 工作台 | 阅读 | 外观 | 高级 ─┐
│                                            │
│  ┌─────────────────────────────────────┐   │
│  │  👤 头像                            │   │
│  │  上传 / 更换头像                    │   │
│  └─────────────────────────────────────┘   │
│  ┌─────────────────────────────────────┐   │
│  │  基本信息                            │   │
│  │  用户名（只读） 昵称 性别 出生日期   │   │
│  └─────────────────────────────────────┘   │
│  ┌─────────────────────────────────────┐   │
│  │  联系方式                            │   │
│  │  邮箱 手机号 所在地区（级联选择器）    │   │
│  └─────────────────────────────────────┘   │
│  ┌─────────────────────────────────────┐   │
│  │  个人简介                            │   │
│  │  GitHub 个人网站 个人简介（200字）    │   │
│  └─────────────────────────────────────┘   │
│                                            │
│           [重置]  [保存修改]                 │
└────────────────────────────────────────────┘
```

## 3. 模块划分

### 3.1 头像
- 页面顶部展示当前头像
- Hover 显示"更换头像"
- 点击上传：JPG/PNG，最大 5MB
- 上传成功立即预览
- 使用独立 avatar 存储目录

### 3.2 基本信息
| 字段 | 是否可修改 | 校验 |
|------|-----------|------|
| 用户名 | ❌ 只读 | - |
| 昵称 | ✅ | 必填，2-20 字符 |
| 性别 | ✅ | 枚举：男/女/保密 |
| 出生日期 | ✅ | 日期选择器 |

### 3.3 联系方式
| 字段 | 必填 | 校验 |
|------|------|------|
| 邮箱 | ✅ | 邮箱格式 |
| 手机号 | ❌ | 手机号格式 |
| 所在地区 | ❌ | 级联选择器：国家 → 省份 → 城市 |

### 3.4 个人简介
| 字段 | 必填 | 校验 |
|------|------|------|
| GitHub | ❌ | `https://github.com/...` |
| 个人网站 | ❌ | `https://...` |
| 个人简介 | ❌ | 最多 200 字，带字数统计 |

## 4. 数据库变更

`sys_user` 表新增列：

```sql
ALTER TABLE sys_user ADD COLUMN gender     tinyint      DEFAULT 0  COMMENT '性别 0-保密 1-男 2-女' AFTER email;
ALTER TABLE sys_user ADD COLUMN birthday   date         DEFAULT NULL COMMENT '出生日期' AFTER gender;
ALTER TABLE sys_user ADD COLUMN phone      varchar(20)  DEFAULT NULL COMMENT '手机号' AFTER birthday;
ALTER TABLE sys_user ADD COLUMN country    varchar(50)  DEFAULT NULL COMMENT '国家' AFTER phone;
ALTER TABLE sys_user ADD COLUMN province   varchar(50)  DEFAULT NULL COMMENT '省份' AFTER country;
ALTER TABLE sys_user ADD COLUMN city       varchar(50)  DEFAULT NULL COMMENT '城市' AFTER province;
ALTER TABLE sys_user ADD COLUMN website    varchar(200) DEFAULT NULL COMMENT '个人网站' AFTER city;
ALTER TABLE sys_user ADD COLUMN github     varchar(200) DEFAULT NULL COMMENT 'GitHub 地址' AFTER website;
ALTER TABLE sys_user ADD COLUMN bio        varchar(500) DEFAULT NULL COMMENT '个人简介' AFTER github;
```

## 5. API 设计

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/user/profile` | 获取当前用户资料 |
| PUT | `/api/user/profile` | 更新资料 |
| POST | `/api/user/avatar` | 上传头像（multipart） |

### GET /api/user/profile 响应

```json
{
  "id": 1,
  "username": "admin",
  "nickname": "管理员",
  "avatar": "https://...",
  "email": "admin@example.com",
  "gender": 1,
  "birthday": "1990-01-01",
  "phone": "13800138000",
  "country": "中国",
  "province": "广东省",
  "city": "深圳市",
  "website": "https://example.com",
  "github": "https://github.com/username",
  "bio": "热爱编程"
}
```

### PUT /api/user/profile 请求体

```json
{
  "nickname": "管理员",
  "email": "admin@example.com",
  "gender": 1,
  "birthday": "1990-01-01",
  "phone": "13800138000",
  "country": "中国",
  "province": "广东省",
  "city": "深圳市",
  "website": "https://example.com",
  "github": "https://github.com/username",
  "bio": "热爱编程"
}
```

## 6. 前端组件结构

```
SettingsView.vue
  └── ProfileSettings.vue    (新建)
        ├── AvatarUpload.vue  (内联)
        ├── 基本信息表单
        ├── 联系方式表单（级联选择器）
        └── 个人简介表单（字数统计）
```

## 7. 交互细节
- 点击"保存修改" → 请求成功 → `✓ 保存成功`
- 点击"重置" → 恢复到上次保存的值
- 表单校验失败时对应字段标红
- 头像 hover 时显示遮罩"更换头像"

## 8. UI 规范
- Card：border-radius 16px, padding 24px
- 输入框统一宽度 420px
- TextArea 高度 120px，带字数统计 `0/200`
- 按钮底部右对齐：重置（次要）+ 保存修改（品牌色）
