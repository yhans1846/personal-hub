-- ========================================
-- Personal Hub 测试数据（userId=1 管理员）
-- ========================================

-- 1. 笔记分类
INSERT INTO `note_category` (`user_id`, `name`, `sort_order`) VALUES
(1, '技术笔记', 1),
(1, '生活随笔', 2),
(1, '读书笔记', 3);

-- 2. 笔记
INSERT INTO `note_note` (`user_id`, `title`, `content`, `is_favorite`) VALUES
(1, 'Spring Boot 入门指南', '## 什么是 Spring Boot\n\nSpring Boot 是 Spring 框架的一个子项目，用于快速构建生产级别的 Spring 应用。\n\n### 核心特性\n- 自动配置\n- 嵌入式服务器\n- 起步依赖\n- 健康检查\n\n### 快速开始\n\n```java\n@SpringBootApplication\npublic class Application {\n    public static void main(String[] args) {\n        SpringApplication.run(Application.class, args);\n    }\n}\n```', 1),
(1, 'Vue3 组合式 API 学习', '## Composition API\n\nVue3 引入了组合式 API，让逻辑复用更加灵活。\n\n### ref 和 reactive\n\n```javascript\nimport { ref, reactive } from ''vue''\n\nconst count = ref(0)\nconst state = reactive({ name: ''test'' })\n```\n\n### 生命周期\n\n- `onMounted` - 挂载后\n- `onUnmounted` - 卸载前\n- `watch` - 侦听变化', 1),
(1, 'MySQL 索引优化笔记', '## 索引类型\n\n- B+Tree 索引：最常用\n- 哈希索引：等值查询\n- 全文索引：文本搜索\n\n## 优化建议\n\n1. 选择性高的列适合建索引\n2. 不要对频繁更新的列建索引\n3. 联合索引遵循最左前缀原则', 0),
(1, '周末爬山记', '今天和朋友一起去爬了梧桐山，天气很好，山顶的风景很美。\n\n### 路线\n- 起点：梧桐山北门\n- 耗时：约3小时\n- 距离：约8公里\n\n虽然很累，但是很有成就感！', 0),
(1, '重构：改善既有代码的设计 读书笔记', '## 核心原则\n\n### 何时重构\n- 添加功能时\n- 修复bug时\n- Code Review 时\n\n### 坏代码的味道\n1. 过长函数\n2. 过大的类\n3. 基本类型偏执\n4. 重复代码\n\n### 常用重构手法\n- 提取方法（Extract Method）\n- 移动字段（Move Field）\n- 以多态取代条件表达式', 1);

-- 3. 笔记-分类关联
INSERT INTO `note_category_rel` (`note_id`, `category_id`) VALUES
(1, 1), (2, 1), (3, 1), (4, 2), (5, 3);

-- 4. 标签
INSERT INTO `tag` (`user_id`, `name`, `color`) VALUES
(1, '前端', '#409eff'),
(1, '后端', '#67c23a'),
(1, '数据库', '#e6a23c'),
(1, '日常', '#f56c6c'),
(1, '阅读', '#909399'),
(1, 'Vue', '#409eff'),
(1, 'Spring', '#67c23a'),
(1, '性能优化', '#e6a23c');

-- 5. 标签关联（笔记）
INSERT INTO `tag_rel` (`tag_id`, `entity_type`, `entity_id`) VALUES
(1, 'note', 2), (2, 'note', 1), (3, 'note', 3),
(4, 'note', 4), (5, 'note', 5), (6, 'note', 2), (7, 'note', 1), (8, 'note', 3);

-- 6. 收藏夹分类
INSERT INTO `bookmark_category` (`user_id`, `name`, `sort_order`) VALUES
(1, '开发工具', 1),
(1, '学习资源', 2),
(1, '技术社区', 3),
(1, '设计灵感', 4);

-- 7. 收藏夹
INSERT INTO `bookmark_url` (`user_id`, `title`, `url`, `description`, `favicon`, `category_id`) VALUES
(1, 'GitHub', 'https://github.com', '全球最大的代码托管平台', NULL, 1),
(1, 'Vue.js 官方文档', 'https://vuejs.org', 'Vue.js 渐进式 JavaScript 框架官方文档', NULL, 2),
(1, 'Spring 官方文档', 'https://spring.io', 'Spring 框架官方文档和指南', NULL, 2),
(1, 'MDN Web 文档', 'https://developer.mozilla.org', 'Web 开发权威文档资源', NULL, 2),
(1, '掘金社区', 'https://juejin.cn', '中文技术社区，前端技术文章丰富', NULL, 3),
(1, 'Stack Overflow', 'https://stackoverflow.com', '全球最大的程序设计问答社区', NULL, 3),
(1, 'Dribbble', 'https://dribbble.com', '设计师作品展示平台', NULL, 4),
(1, 'Codepen', 'https://codepen.io', '在线代码编辑器，前端代码片段分享', NULL, 1),
(1, 'InfoQ', 'https://www.infoq.cn', '中文技术资讯网站', NULL, 3),
(1, '阿里巴巴图标库', 'https://www.iconfont.cn', '阿里巴巴矢量图标库', NULL, 4);

-- 8. 待办任务
INSERT INTO `todo_task` (`user_id`, `title`, `content`, `priority`, `due_date`, `is_done`) VALUES
(1, '完成项目文档编写', '包括 README 和接口文档', 1, '2026-07-12', 0),
(1, 'Review 代码提交', '审核本周合并请求的代码质量', 1, '2026-07-11', 0),
(1, '学习 Docker 容器化部署', 'docker-compose 编排 Spring Boot + MySQL + Redis', 2, '2026-07-15', 0),
(1, '购买办公用品', '键盘、显示器支架等', 3, '2026-07-13', 0),
(1, '更新个人博客主题', '更换前端 UI 框架为 Element Plus', 2, '2026-07-20', 0),
(1, '完成周报', '本周工作总结和下周计划', 2, '2026-07-10', 1),
(1, '数据库备份检查', '检查自动备份策略是否正常运行', 1, '2026-07-10', 1),
(1, '配置 GitHub Actions CI', '自动化构建和测试流程', 2, '2026-07-18', 0);

-- 9. 日记
INSERT INTO `diary_entry` (`user_id`, `date`, `title`, `content`, `mood`, `weather`) VALUES
(1, '2026-07-08', '忙碌的周三', '今天完成了搜索功能的开发，包括全局搜索和按模块搜索。前端使用了模糊搜索，后端接入了 Elasticsearch 类似的 LIKE 查询。\n\n明天开始做 Dashboard 统计图表。', 2, '晴'),
(1, '2026-07-09', 'Dashboard 开发', '用 ECharts 完成了趋势图表，包括笔记数量、学习时长、任务完成率的趋势展示。\n\n统计模块用 MyBatis 写了一些聚合查询，性能还不错。下午修复了一些前端样式问题。', 1, '多云'),
(1, '2026-07-10', '数据库维护日', '今天检查了数据库表结构，优化了几个慢查询。发现 study_record 表缺少 plan_id 字段，已经补充。\n\n下午花了些时间看技术文章，学习了 MySQL 索引优化的新技巧。', 2, '晴');

-- 10. 学习记录
INSERT INTO `study_record` (`user_id`, `subject`, `date`, `duration`, `content`, `reflection`) VALUES
(1, 'Spring Security 认证流程', '2026-07-08', 120, '学习了 Spring Security 的认证过滤器链、UserDetailsService 和 JWT 集成', '对认证流程有了更深入的理解，特别是过滤器链的执行顺序'),
(1, 'Vue3 响应式原理', '2026-07-08', 90, '学习了 Proxy 和 Reflect 在 Vue3 响应式系统中的应用', '相比 Vue2 的 defineProperty，Proxy 更加强大和灵活'),
(1, 'MySQL 索引优化', '2026-07-09', 60, '学习了 explain 执行计划分析、索引下推、覆盖索引等优化技术', '需要多实践，EXPLAIN 是优化 SQL 的好工具'),
(1, 'Docker 基础', '2026-07-09', 150, 'Dockerfile 编写、docker-compose 编排、常用命令', '容器化部署对开发和运维都很有帮助，需要继续深入学习'),
(1, 'Redis 缓存策略', '2026-07-10', 100, '缓存穿透、缓存击穿、缓存雪崩的解决方案', '布隆过滤器解决缓存穿透，互斥锁解决缓存击穿');

-- 11. 学习计划
INSERT INTO `study_plan` (`user_id`, `name`, `goal`, `progress`, `start_date`, `end_date`, `status`) VALUES
(1, 'Spring Boot 深入学习', '系统学习 Spring Boot 高级特性，包括安全、缓存、消息队列', 60, '2026-07-01', '2026-08-30', 1),
(1, '前端工程化', '掌握 Vite、Webpack、ESLint、Prettier 等前端工具链', 30, '2026-07-10', '2026-09-10', 1),
(1, 'MySQL 高级', '学习 MySQL 性能优化、主从复制、分库分表', 20, '2026-07-15', '2026-09-15', 1);

-- 12. 阅读记录
INSERT INTO `reading_record` (`user_id`, `book_title`, `author`, `cover_url`, `total_chapters`, `current_chapter`, `progress`, `status`, `notes`, `start_date`, `end_date`) VALUES
(1, '深入理解 Java 虚拟机', '周志明', NULL, 13, 8, 60, 1, '第三章垃圾回收看了两遍，讲得很透彻', '2026-06-01', NULL),
(1, '重构：改善既有代码的设计', 'Martin Fowler', NULL, 20, 20, 100, 2, '实践性很强，推荐每个开发者阅读', '2026-05-01', '2026-06-30'),
(1, 'JavaScript 高级程序设计', 'Nicholas Zakas', NULL, 28, 15, 50, 1, '经典红宝书，内容很全面', '2026-06-15', NULL),
(1, 'Docker 实战', 'Jeff Nickoloff', NULL, 12, 0, 0, 0, NULL, NULL, NULL);

-- 13. 标签关联（收藏夹）
INSERT INTO `tag_rel` (`tag_id`, `entity_type`, `entity_id`) VALUES
(2, 'bookmark', 1), (2, 'bookmark', 3),
(1, 'bookmark', 2), (1, 'bookmark', 8),
(3, 'bookmark', 4),
(5, 'bookmark', 5), (5, 'bookmark', 6);

-- 14. 标签关联（日记）
INSERT INTO `tag_rel` (`tag_id`, `entity_type`, `entity_id`) VALUES
(2, 'diary', 1), (8, 'diary', 1),
(2, 'diary', 2), (1, 'diary', 2),
(3, 'diary', 3), (8, 'diary', 3);
