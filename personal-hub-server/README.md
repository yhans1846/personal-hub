# Personal Hub Server

Personal Hub 后端 — Spring Boot 3 + Java 21。

## 技术栈

- **框架**: Spring Boot 3.3.5 / Spring MVC
- **语言**: Java 21 LTS
- **ORM**: MyBatis-Plus
- **数据库**: MySQL 8.x / Redis 5.x
- **安全**: Spring Security + JWT
- **文档**: SpringDoc OpenAPI 2.6 (Swagger)
- **构建**: Maven 3.8+

## 领域模块

```
ph-boot          # 启动入口（spring-boot-maven-plugin）
ph-common        # 公共组件（Result / 异常处理 / JWT / 配置）
ph-system        # 用户认证(JWT) / 个人信息 / 通知系统 / 布局配置
ph-knowledge     # 笔记 / 日记 / 学习记录 / 阅读记录 / 标签
ph-planning      # Todo / 学习计划
ph-resource      # 收藏夹 / 文件管理
ph-dashboard     # Dashboard 聚合统计 + 全局搜索 + 趋势图
```

## 快速启动

### 前置

- JDK 21 LTS
- MySQL 8.0+（端口 3306，库 `personal_hub`）
- Redis 5.0+（端口 6379，DB 8）

### 启动

```bash
# 初始化数据库（首次）
mysql -u root -p < ../sql/init.sql

# 启动应用
mvn spring-boot:run -pl ph-boot -am
```

启动后：`http://localhost:8080` | API 文档：`http://localhost:8080/swagger-ui.html`

## 文档

项目文档见 `docs/` 目录。
