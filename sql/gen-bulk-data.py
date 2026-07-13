#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""Personal Hub 批量趋势测试数据生成器"""
import sys
import os
import random
from datetime import datetime, timedelta

random.seed(20260713)

START = datetime(2026, 6, 13)
END = datetime(2026, 7, 13)
DAYS = (END - START).days
USER_ID = 1
OUTPUT = os.path.join(os.path.dirname(__file__) or '.', 'bulk-trend-data.sql')

# ─── 数据池 ───
STUDY_SUBJECTS = [
    "Spring Boot 自动配置", "LeetCode 二叉树", "MySQL 索引优化",
    "Vue3 响应式原理", "Docker Compose", "Redis 缓存策略",
    "Kubernetes Pod", "Java 并发编程", "Git 工作流",
    "React Hooks", "Python 数据分析", "Nginx 反向代理",
    "GraphQL 查询", "MongoDB 聚合", "消息队列 RabbitMQ",
    "Elasticsearch 搜索", "Go 语言基础",
    "设计模式-工厂", "DDD 领域驱动", "微服务拆分",
    "OAuth2 授权", "gRPC 通信", "WebSocket 推送",
    "CSS Grid 布局", "TypeScript 类型系统",
    "Playwright 测试", "Gradle 构建",
]

STUDY_CONTENT = [
    "学习了基础概念和核心原理，做了笔记。",
    "通过实战案例深入理解，写了示例代码。",
    "看完官方文档，整理了一份思维导图。",
    "做了几个练习题，巩固了知识点。",
    "看了一篇深度文章，收获很大。",
    "写了一个 Demo 项目，验证了方案的可行性。",
    "复习了之前的内容，发现了一些新的理解角度。",
    "和同事讨论了最佳实践，改进了实现方式。",
]

NOTE_TITLES = [
    "关于微服务架构的思考", "如何写好 API 文档",
    "团队代码规范讨论", "性能优化实战记录",
    "系统设计面试题解析", "Kubernetes 排障经验",
    "前端性能优化清单", "数据库分库分表方案",
    "日志监控体系建设", "技术选型对比分析",
    "CI/CD 流水线搭建", "代码审查清单整理",
    "敏捷开发实践总结", "技术债务清理计划",
    "单元测试最佳实践", "重构经验分享",
    "系统可用性设计", "容灾备份方案",
    "安全漏洞修复记录", "新技术调研报告",
    "项目管理工具对比", "周报模板优化",
    "团队知识库建设", "AIGC 辅助开发实践",
    "云原生架构设计", "Serverless 实践",
    "数据一致性方案", "契约测试入门",
    "前端构建性能优化", "API 网关设计",
]

TODO_TITLES = [
    "完成用户模块重构", "修复登录页样式 Bug",
    "更新依赖版本", "添加接口限流",
    "配置日志采集", "编写集成测试",
    "优化首页加载速度", "添加数据导出功能",
    "实现暗黑模式", "修复搜索分页问题",
    "升级 Spring Boot 版本", "添加操作审计",
    "优化数据库查询", "添加缓存预热",
    "实现文件预览", "修复上传超时",
    "添加灰度发布", "实现消息推送",
    "优化移动端适配", "添加数据导入",
    "实现批量操作", "修复通知重复问题",
    "优化大文件上传", "添加权限管理",
    "编写使用文档", "添加健康检查",
    "优化错误提示", "实现搜索建议",
    "添加数据备份", "优化登录体验",
    "实现记住密码", "添加加载动画",
    "优化表格渲染", "添加图表导出",
    "实现数据对比", "优化表单验证",
    "添加教程引导", "优化键盘导航",
]

BOOKS = [
    ("Clean Architecture", "Robert Martin"),
    ("DDD 实战", "Vaughn Vernon"),
    ("Head First 设计模式", "Eric Freeman"),
    ("Java 并发编程实战", "Brian Goetz"),
    ("高性能 MySQL", "Baron Schwartz"),
    ("TCP/IP 详解", "Kevin Fall"),
    ("算法导论", "Thomas Cormen"),
    ("程序员的自我修养", "俞甲子"),
    ("黑客与画家", "Paul Graham"),
    ("软件工程之美", "赵晓东"),
    ("数据密集型应用系统设计", "Martin Kleppmann"),
    ("凤凰项目", "Gene Kim"),
    ("SRE: Google 运维解密", "Betsy Beyer"),
    ("重构：改善既有代码的设计", "Martin Fowler"),
    ("测试驱动开发", "Kent Beck"),
]

# =====================================================

def line(s=""):
    return s + "\n"

def gen_data():
    study_rows = []
    note_rows = []
    todo_rows = []
    reading_rows = []

    note_id = 11
    todo_id = 15
    reading_id = 9

    for day_offset in range(DAYS):
        d = START + timedelta(days=day_offset)

        # ── 学习记录 ──
        study_count = random.choices([0, 1, 2, 3, 4], weights=[5, 20, 35, 30, 10])[0]
        for _ in range(study_count):
            subject = random.choice(STUDY_SUBJECTS)
            duration = random.choices([30, 45, 60, 75, 90, 120, 150], weights=[10, 20, 30, 20, 10, 7, 3])[0]
            content = random.choice(STUDY_CONTENT)
            reflection = random.choice(["理解了核心概念", "需要多加练习", "收获很大", "还有疑问", "结合实际项目应用"])
            study_rows.append(
                f"({len(study_rows)+16}, {USER_ID}, '{subject}', '{d.date()}', {duration}, '{content}', '{reflection}', NULL, "
                f"'{d.strftime('%Y-%m-%d %H:%M:%S')}')"
            )

        # ── 笔记 ──
        note_count = random.choices([0, 1, 2, 3], weights=[10, 40, 40, 10])[0]
        for _ in range(note_count):
            title = random.choice(NOTE_TITLES)
            h, m = random.randint(8, 23), random.randint(0, 59)
            ct = d.replace(hour=h, minute=m)
            fav = random.choice([0, 0, 0, 1])
            note_rows.append(
                f"({note_id}, {USER_ID}, '{title}', 'notes/{note_id}/note.md', {fav}, NULL, "
                f"'{ct.strftime('%Y-%m-%d %H:%M:%S')}', '{ct.strftime('%Y-%m-%d %H:%M:%S')}')"
            )
            note_id += 1

        # ── 待办 ──
        todo_count = random.choices([0, 1, 2, 3], weights=[10, 30, 40, 20])[0]
        for _ in range(todo_count):
            title = random.choice(TODO_TITLES)
            done = 1 if random.random() < 0.4 else 0
            priority = random.choices([1, 2, 3], weights=[20, 50, 30])[0]
            due = d + timedelta(days=random.randint(1, 14)) if not done else None
            h, m = random.randint(9, 20), random.randint(0, 59)
            ct = d.replace(hour=h, minute=m)
            due_str = f"'{due.date()}'" if due else "NULL"
            todo_rows.append(
                f"({todo_id}, {USER_ID}, '{title}', NULL, {done}, {priority}, {due_str}, "
                f"'{ct.strftime('%Y-%m-%d %H:%M:%S')}', '{ct.strftime('%Y-%m-%d %H:%M:%S')}')"
            )
            todo_id += 1

        # ── 阅读记录 ──
        if random.random() < 0.65:
            book = random.choice(BOOKS)
            title, author = book
            chapters = random.randint(8, 40)
            current = random.randint(0, chapters)
            progress = int(current / chapters * 100)
            status = 2 if progress == 100 else (1 if progress > 0 else 0)
            rating = random.randint(3, 5) if status == 2 else "NULL"
            duration = random.randint(30, 600) if progress > 0 else 0
            h = random.randint(10, 22)
            ct = d.replace(hour=h)
            end = f"'{d.date()}'" if status == 2 else "NULL"
            start = f"'{(d - timedelta(days=random.randint(1, 30))).date()}'" if progress > 0 else "NULL"
            reading_rows.append(
                f"({reading_id}, {USER_ID}, '{title}', '{author}', NULL, {chapters}, {current}, {progress}, "
                f"{status}, {rating}, {duration}, "
                f"'{ct.strftime('%Y-%m-%d %H:%M:%S')}', '{ct.strftime('%Y-%m-%d %H:%M:%S')}', "
                f"{start}, {end})"
            )
            reading_id += 1

    # ── 写出文件 ──
    with open(OUTPUT, 'w', encoding='utf-8') as f:
        f.write("-- ================================================\n")
        f.write("-- Personal Hub 批量趋势数据\n")
        f.write(f"-- 生成日期: {START.date()} ~ {END.date()}\n")
        f.write(f"-- 用户 ID: {USER_ID}\n")
        f.write("-- ================================================\n")
        f.write("SET NAMES utf8mb4;\n\n")

        f.write(f"-- 学习记录 x {len(study_rows)}\n")
        f.write("INSERT INTO `study_record` (`id`, `user_id`, `subject`, `date`, `duration`, `content`, `reflection`, `plan_id`, `created_at`) VALUES\n")
        for i, r in enumerate(study_rows):
            f.write(f"  {r}{',' if i < len(study_rows)-1 else ';'}\n")
        f.write("\n")

        f.write(f"-- 笔记 x {len(note_rows)}\n")
        f.write("INSERT INTO `note_note` (`id`, `user_id`, `title`, `md_path`, `is_favorite`, `delete_reason`, `created_at`, `updated_at`) VALUES\n")
        for i, r in enumerate(note_rows):
            f.write(f"  {r}{',' if i < len(note_rows)-1 else ';'}\n")
        f.write("\n")

        f.write(f"-- 待办 x {len(todo_rows)}\n")
        f.write("INSERT INTO `todo_task` (`id`, `user_id`, `title`, `content`, `is_done`, `priority`, `due_date`, `created_at`, `updated_at`) VALUES\n")
        for i, r in enumerate(todo_rows):
            f.write(f"  {r}{',' if i < len(todo_rows)-1 else ';'}\n")
        f.write("\n")

        f.write(f"-- 阅读记录 x {len(reading_rows)}\n")
        f.write("INSERT INTO `reading_record` (`id`, `user_id`, `book_title`, `author`, `cover_url`, `total_chapters`, `current_chapter`, `progress`, `status`, `rating`, `total_duration`, `created_at`, `updated_at`, `start_date`, `end_date`) VALUES\n")
        for i, r in enumerate(reading_rows):
            f.write(f"  {r}{',' if i < len(reading_rows)-1 else ';'}\n")

    print(f"OK: {OUTPUT}")
    print(f"  study_record: {len(study_rows)}")
    print(f"  note_note:    {len(note_rows)}")
    print(f"  todo_task:    {len(todo_rows)}")
    print(f"  reading_record: {len(reading_rows)}")

if __name__ == '__main__':
    gen_data()
