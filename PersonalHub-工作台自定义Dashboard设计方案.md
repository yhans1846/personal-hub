# Personal Hub 工作台自定义（Dashboard Customization）设计方案

# 一、设计目标

打造一个支持用户个性化配置的工作台，而不仅仅是隐藏菜单。

支持：

-   ✅ 隐藏/显示菜单
-   ✅ 菜单拖拽排序
-   ✅ 首页卡片显示/隐藏
-   ✅ Dashboard 卡片拖拽排序
-   ✅ 每个用户拥有自己的布局
-   ✅ 一键恢复默认布局

------------------------------------------------------------------------

# 二、整体架构

建议新增 **布局配置(Layout)** 模块。

    Dashboard
        │
        ├── Menu Layout
        ├── Card Layout
        ├── Widget Layout
        └── User Preference

所有配置统一保存。

------------------------------------------------------------------------

# 三、数据库设计

## 用户布局配置

``` sql
CREATE TABLE user_layout(
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    layout_type VARCHAR(30) NOT NULL,
    layout_json JSON NOT NULL,
    update_time DATETIME,
    UNIQUE(user_id,layout_type)
);
```

layout_type：

-   menu
-   dashboard
-   widget

layout_json 示例：

``` json
{
  "menus":[
    {"code":"note","visible":true,"order":2},
    {"code":"study","visible":false,"order":8}
  ]
}
```

------------------------------------------------------------------------

# 四、菜单管理

支持：

-   显示/隐藏
-   拖拽排序
-   恢复默认

首页建议固定不可隐藏。

------------------------------------------------------------------------

# 五、Dashboard 卡片

每张卡片拥有：

-   visible
-   order

例如：

-   今日计划
-   最近阅读
-   最近更新
-   待办任务
-   数据统计

支持拖拽排序。

------------------------------------------------------------------------

# 六、设置中心

新增：

    系统设置
     ├── 菜单管理
     ├── Dashboard布局
     └── 恢复默认

------------------------------------------------------------------------

# 七、接口设计

    GET  /api/layout
    PUT  /api/layout
    POST /api/layout/import

------------------------------------------------------------------------

# 八、前端设计

菜单增加：

``` ts
{
  code:"note",
  title:"笔记",
  order:2,
  visible:true
}
```

Dashboard 卡片：

``` ts
{
  code:"todo",
  title:"待办任务",
  order:1,
  visible:true
}
```

渲染前统一排序并过滤 visible。

------------------------------------------------------------------------

# 九、推荐技术

-   VueUse draggable 或 SortableJS 实现拖拽
-   Pinia 保存当前布局
-   后端数据库持久化
-   登录后自动同步布局

------------------------------------------------------------------------

# 十、长期演进

后续可继续扩展：

-   多套布局模板
-   深色模式独立布局
-   快捷入口自定义
-   常用功能收藏
-   Widget 插件机制
-   首页自由组合（类似 Notion）
