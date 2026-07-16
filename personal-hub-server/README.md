# Personal Hub Server

Personal Hub 后端 — Spring Boot 3 + Java 21 + MyBatis-Plus。

## 技术栈

- **框架**: Spring Boot 3.3.5 / Spring MVC
- **语言**: Java 21 LTS
- **ORM**: MyBatis-Plus
- **数据库**: MySQL 8.x / Redis 5.x
- **安全**: Spring Security + JWT
- **文档**: SpringDoc OpenAPI 2.6（仅 dev 环境开启）
- **构建**: Maven 3.8+

## 领域模块（4）

```
ph-boot          # 启动入口（spring-boot-maven-plugin）
ph-common        # 公共组件 + 存储 SPI（Result / 异常 / JWT / Security / StorageService）
ph-system        # 用户认证(JWT) / 个人信息 / 通知 / 布局 / 审计
ph-biz           # 业务合集
                 #   knowledge  笔记 / 回收站 / 日记 / 学习 / 阅读 / 标签 / 分类
                 #   planning   Todo / 学习计划
                 #   resource   收藏夹 / 文件 / 笔记资源
                 #   dashboard  聚合统计 / 全局搜索 / 趋势
```

## 环境配置

```
application.yml            # 公共配置
application-dev.yml        # 开发环境（激活：spring.profiles.active=dev）
application-prod.yml       # 生产环境（激活：spring.profiles.active=prod）
```

主要差异：SQL 日志（dev 打印 / prod 关闭）、Swagger（dev 开启 / prod 关闭）、连接池大小、文件存储路径。

## 快速启动

### 前置

- JDK 21 LTS
- MySQL 8.0+（端口 3306，库 `personal_hub`）
- Redis 5.0+（端口 6379，DB 8）

### 启动

```bash
# 初始化数据库（首次）
mysql -u root -p < ../sql/init.sql

# 启动应用（默认 dev 环境）
mvn spring-boot:run -pl ph-boot -am
```

启动后：`http://localhost:8080` | API 文档（dev）：`http://localhost:8080/swagger-ui.html`

## 文档

项目文档见 `docs/` 目录。
