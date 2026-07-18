# -*- coding: utf-8 -*-
"""
Personal Hub 演示数据种子：写入 DB + 真实文件到 D:/PersonalHub/uploads
用法: python scripts/seed_demo_data.py
账号: admin / 123456
"""
from __future__ import annotations

import io
import json
import random
import uuid
import zipfile
from datetime import date, datetime, timedelta
from pathlib import Path

import pymysql
from PIL import Image, ImageDraw, ImageFont

# ── 配置 ──────────────────────────────────────────────
DB = dict(host="127.0.0.1", user="al_user", password="123456", database="personal_hub", charset="utf8mb4")
STORAGE = Path("D:/PersonalHub/uploads")
USER_ID = 1
TODAY = date.today()
RNG = random.Random(20260718)

# ── 文件工具 ──────────────────────────────────────────
def ensure_parent(p: Path) -> Path:
    p.parent.mkdir(parents=True, exist_ok=True)
    return p


def write_bytes(rel: str, data: bytes) -> tuple[str, int]:
    path = ensure_parent(STORAGE / rel)
    path.write_bytes(data)
    return rel.replace("\\", "/"), len(data)


def make_png(label: str, size=(640, 400), color=None) -> bytes:
    color = color or (
        RNG.randint(40, 200),
        RNG.randint(40, 200),
        RNG.randint(40, 200),
    )
    img = Image.new("RGB", size, color)
    draw = ImageDraw.Draw(img)
    draw.rectangle([20, 20, size[0] - 20, size[1] - 20], outline=(255, 255, 255), width=3)
    try:
        font = ImageFont.truetype("arial.ttf", 28)
    except Exception:
        font = ImageFont.load_default()
    draw.text((40, size[1] // 2 - 20), label[:40], fill=(255, 255, 255), font=font)
    buf = io.BytesIO()
    img.save(buf, format="PNG")
    return buf.getvalue()


def make_jpeg(label: str) -> bytes:
    img = Image.new("RGB", (480, 320), (RNG.randint(60, 180), RNG.randint(60, 180), RNG.randint(60, 180)))
    draw = ImageDraw.Draw(img)
    draw.text((30, 140), label[:36], fill=(255, 255, 255))
    buf = io.BytesIO()
    img.save(buf, format="JPEG", quality=85)
    return buf.getvalue()


def make_pdf(title: str) -> bytes:
    # 最小可打开 PDF
    content = f"BT /F1 18 Tf 50 700 Td ({title[:40]}) Tj ET"
    stream = content.encode("latin-1", errors="replace")
    objs = []
    objs.append(b"1 0 obj<< /Type /Catalog /Pages 2 0 R >>endobj\n")
    objs.append(b"2 0 obj<< /Type /Pages /Kids [3 0 R] /Count 1 >>endobj\n")
    objs.append(
        b"3 0 obj<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] "
        b"/Contents 4 0 R /Resources<< /Font<< /F1 5 0 R >> >> >>endobj\n"
    )
    objs.append(f"4 0 obj<< /Length {len(stream)} >>stream\n".encode() + stream + b"\nendstream\nendobj\n")
    objs.append(b"5 0 obj<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>endobj\n")
    body = b"".join(objs)
    xref_pos = len(b"%PDF-1.4\n") + len(body)
    # rebuild with offsets
    out = io.BytesIO()
    out.write(b"%PDF-1.4\n")
    offsets = [0]
    for o in objs:
        offsets.append(out.tell())
        out.write(o)
    xref_start = out.tell()
    out.write(f"xref\n0 {len(offsets)}\n".encode())
    out.write(b"0000000000 65535 f \n")
    for off in offsets[1:]:
        out.write(f"{off:010d} 00000 n \n".encode())
    out.write(b"trailer<< /Size %d /Root 1 0 R >>\n" % len(offsets))
    out.write(b"startxref\n%d\n%%%%EOF\n" % xref_start)
    return out.getvalue()


def make_txt(text: str) -> bytes:
    return text.encode("utf-8")


def make_md(title: str, body: str, image_refs: list[str] | None = None) -> bytes:
    parts = [f"# {title}\n", f"> seeded at {datetime.now():%Y-%m-%d %H:%M}\n\n", body, "\n"]
    if image_refs:
        parts.append("\n## 配图\n\n")
        for ref in image_refs:
            parts.append(f"![img]({ref})\n\n")
    return "".join(parts).encode("utf-8")


def make_zip(name: str, inner: str) -> bytes:
    buf = io.BytesIO()
    with zipfile.ZipFile(buf, "w", zipfile.ZIP_DEFLATED) as zf:
        zf.writestr(inner, f"demo archive for {name}\n".encode("utf-8"))
    return buf.getvalue()


def make_docx(title: str) -> bytes:
    """最小 OOXML docx。"""
    buf = io.BytesIO()
    with zipfile.ZipFile(buf, "w", zipfile.ZIP_DEFLATED) as zf:
        zf.writestr(
            "[Content_Types].xml",
            """<?xml version="1.0" encoding="UTF-8"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
</Types>""",
        )
        zf.writestr(
            "_rels/.rels",
            """<?xml version="1.0" encoding="UTF-8"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
</Relationships>""",
        )
        safe = title.replace("<", "").replace(">", "")[:80]
        zf.writestr(
            "word/document.xml",
            f"""<?xml version="1.0" encoding="UTF-8"?>
<w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main">
  <w:body><w:p><w:r><w:t>{safe}</w:t></w:r></w:p></w:body>
</w:document>""",
        )
    return buf.getvalue()


def uid_hex() -> str:
    return uuid.uuid4().hex


def dt_ago(days: int, hours: int = 0) -> datetime:
    return datetime.now() - timedelta(days=days, hours=hours)


# ── DB ────────────────────────────────────────────────
def connect():
    return pymysql.connect(**DB, autocommit=False, cursorclass=pymysql.cursors.DictCursor)


def clear_business(cur):
    tables = [
        "tag_rel",
        "note_category_rel",
        "audit_log",
        "sys_notification",
        "todo_task",
        "study_record",
        "study_plan",
        "reading_record",
        "file_resource",
        "bookmark_url",
        "diary_entry",
        "note_note",
        "tag",
        "category",
    ]
    cur.execute("SET FOREIGN_KEY_CHECKS=0")
    for t in tables:
        cur.execute(f"TRUNCATE TABLE `{t}`")
    cur.execute("SET FOREIGN_KEY_CHECKS=1")


def insert(cur, table: str, row: dict) -> int:
    cols = ", ".join(f"`{k}`" for k in row)
    ph = ", ".join(["%s"] * len(row))
    cur.execute(f"INSERT INTO `{table}` ({cols}) VALUES ({ph})", list(row.values()))
    return cur.lastrowid


# ── 种子内容 ──────────────────────────────────────────
NOTE_CATS = ["技术笔记", "读书摘录", "工作纪要", "灵感草稿"]
BM_CATS = ["开发文档", "工具站点", "学习资源", "常用导航"]
FILE_CATS = ["截图存档", "工作文档", "压缩包", "参考资料"]
TAGS = [
    ("Java", "#e6a23c"),
    ("Vue", "#67c23a"),
    ("数据库", "#409eff"),
    ("算法", "#f56c6c"),
    ("生活", "#909399"),
    ("重要", "#c45656"),
    ("待整理", "#b88230"),
    ("前端", "#529b2e"),
    ("后端", "#337ecc"),
    ("运维", "#73767a"),
    ("英语", "#9b59b6"),
    ("设计", "#e67e22"),
]
WEATHERS = ["☀️", "🌤️", "☁️", "🌧️", "⛈️", "❄️"]
LOCATIONS = [
    ("杭州西湖", 30.2420, 120.1485),
    ("上海外滩", 31.2400, 121.4900),
    ("北京故宫", 39.9163, 116.3972),
    ("成都宽窄巷", 30.6636, 104.0535),
    ("深圳湾", 22.5200, 113.9900),
    (None, None, None),
]


def seed_categories(cur) -> dict[str, list[int]]:
    out = {"note": [], "bookmark": [], "file": []}
    for i, name in enumerate(NOTE_CATS):
        out["note"].append(insert(cur, "category", {
            "user_id": USER_ID, "name": name, "type": "note", "sort_order": i,
            "created_at": dt_ago(90 - i), "updated_at": dt_ago(90 - i),
        }))
    for i, name in enumerate(BM_CATS):
        out["bookmark"].append(insert(cur, "category", {
            "user_id": USER_ID, "name": name, "type": "bookmark", "sort_order": i,
            "created_at": dt_ago(80 - i), "updated_at": dt_ago(80 - i),
        }))
    for i, name in enumerate(FILE_CATS):
        out["file"].append(insert(cur, "category", {
            "user_id": USER_ID, "name": name, "type": "file", "sort_order": i,
            "created_at": dt_ago(70 - i), "updated_at": dt_ago(70 - i),
        }))
    return out


def seed_tags(cur) -> list[int]:
    ids = []
    for i, (name, color) in enumerate(TAGS):
        ids.append(insert(cur, "tag", {
            "user_id": USER_ID, "name": name, "color": color,
            "created_at": dt_ago(60 - i), "updated_at": dt_ago(60 - i),
        }))
    return ids


def bind_tag(cur, tag_id: int, entity_type: str, entity_id: int):
    insert(cur, "tag_rel", {
        "tag_id": tag_id, "entity_type": entity_type, "entity_id": entity_id,
        "created_at": datetime.now(),
    })


def seed_notes(cur, cat_ids: list[int], tag_ids: list[int]) -> list[int]:
    topics = [
        "Spring Boot 事务传播", "Vue3 Composition API 笔记", "MySQL 索引优化清单",
        "Docker Compose 部署备忘", "Redis 缓存击穿防护", "JWT 鉴权设计",
        "日记配图资源包方案", "产品列表一屏铺满约定", "Vditor IR 右键菜单",
        "学习计划 XLSX 导出", "待办 dueScope 语义", "文件预览改造",
        "Markdown 表格技巧", "Git rebase 与 squash", "Nginx 反向代理",
        "Python 种子脚本约定", "日志 Logback 按天归档", "验证码书架交互",
        "首页六宫格布局", "通知软清除机制", "阅读进度算法", "标签关联表设计",
        "分类树与 UK 约束", "本地存储路径规范", "审计日志 LOGIN",
        "CAPTCHA 限流配置", "Profile 资料字段", "面包屑路由统一",
        "Element Plus 主题 Token", "深链 useDeepLinkDialog",
        "回收站恢复流程", "AUTO_ARCHIVE 预留", "笔记摘要截断",
        "附件 originalName 保留", "图片鉴权代理", "Mermaid 本地扩展",
        "KaTeX 公式备忘", "CSS 变量密度", "内容区宽度滑块",
        "统计趋势 days 参数", "全局搜索分组", "外部快捷 show_on_dashboard",
        "天气 emoji 约定", "心情枚举 1-5", "GPS 经纬度精度",
    ]
    note_ids = []
    # 45 条：0–36 正常（含收藏），37–44 进回收站
    for i in range(45):
        title = topics[i] if i < len(topics) else f"补充笔记 {i + 1}: 覆盖分页与关键词"
        if i >= 37:
            title = f"[已删] {title}"
        is_fav = 1 if i in (0, 5, 10, 15, 20, 25, 30) else 0
        is_del = 1 if i >= 37 else 0
        created = dt_ago(50 - i, i % 12)
        excerpt = f"{title} — 演示摘要，用于列表展示与关键词检索。第 {i+1} 条。"[:200]
        nid = insert(cur, "note_note", {
            "user_id": USER_ID,
            "title": title[:200],
            "md_path": None,
            "excerpt": excerpt,
            "is_favorite": is_fav,
            "is_deleted": is_del,
            "deleted_at": created if is_del else None,
            "delete_reason": "USER_DELETE" if is_del else None,
            "created_at": created,
            "updated_at": created + timedelta(hours=2),
        })
        md_path = f"notes/{nid}/note.md"
        img_refs = []
        # 约一半笔记带配图 + 附件
        if i % 2 == 0:
            fn = f"{uid_hex()}.png"
            rel = f"notes/{nid}/images/{fn}"
            write_bytes(rel, make_png(f"Note#{nid}"))
            img_refs.append(f"images/{fn}")
            if i % 4 == 0:
                fn2 = f"{uid_hex()}.jpg"
                write_bytes(f"notes/{nid}/images/{fn2}", make_jpeg(f"NoteJPG#{nid}"))
                img_refs.append(f"images/{fn2}")
            att_name = f"附件-{nid}.txt"
            write_bytes(f"notes/{nid}/attachments/{att_name}", make_txt(f"attachment for note {nid}\n{title}\n"))
        body = (
            f"## 概述\n\n本笔记用于演示 Personal Hub 列表、收藏、回收站与关键词「{title.split()[0]}」。\n\n"
            f"## 要点\n\n1. 真实 Markdown 落盘\n2. 配图相对路径 `images/...`\n3. 分类与标签关联\n\n"
            f"```java\npublic class Demo{nid} {{}}\n```\n"
        )
        write_bytes(md_path, make_md(title, body, img_refs))
        cur.execute("UPDATE note_note SET md_path=%s WHERE id=%s", (md_path, nid))

        # 分类（可多选）
        if cat_ids:
            cur.execute(
                "INSERT INTO note_category_rel (note_id, category_id) VALUES (%s, %s)",
                (nid, cat_ids[i % len(cat_ids)]),
            )
            if i % 3 == 0:
                cur.execute(
                    "INSERT IGNORE INTO note_category_rel (note_id, category_id) VALUES (%s, %s)",
                    (nid, cat_ids[(i + 1) % len(cat_ids)]),
                )
        # 标签
        if tag_ids and not is_del:
            bind_tag(cur, tag_ids[i % len(tag_ids)], "note", nid)
            if i % 2 == 0:
                bind_tag(cur, tag_ids[(i + 3) % len(tag_ids)], "note", nid)
        note_ids.append(nid)
    return note_ids


def seed_diaries(cur) -> list[int]:
    titles = [
        "晨跑感受", "周末书店", "加班小记", "旅行碎片", "下雨天宅家",
        "代码调通了", "和朋友聚餐", "学习英语", "健身房打卡", "公园散步",
        "项目复盘", "灵感突现", "感冒休息", "看展日记", "海边日落",
    ]
    ids = []
    # 跨近 5 个月，覆盖 mood 1-5、有无图、有无地点
    for i in range(60):
        d = TODAY - timedelta(days=i * 2 + RNG.randint(0, 1))
        mood = (i % 5) + 1
        title = f"{titles[i % len(titles)]} · {d:%m/%d}"
        loc = LOCATIONS[i % len(LOCATIONS)]
        created = datetime.combine(d, datetime.min.time()) + timedelta(hours=20)
        did = insert(cur, "diary_entry", {
            "user_id": USER_ID,
            "date": d,
            "title": title,
            "content": (
                f"## {title}\n\n今天心情档位 **{mood}**。\n\n"
                f"写一段足够长的 Markdown，方便关键词检索「{titles[i % len(titles)]}」。\n\n"
                f"- 天气 {WEATHERS[i % len(WEATHERS)]}\n"
                f"- 日期 {d.isoformat()}\n"
            ),
            "mood": mood,
            "weather": WEATHERS[i % len(WEATHERS)],
            "is_deleted": 0,
            "created_at": created,
            "updated_at": created,
            "location": loc[0],
            "latitude": loc[1],
            "longitude": loc[2],
            "image_files": None,
        })
        names = []
        # 大部分带 1~3 张配图
        if i % 5 != 4:
            n_img = 1 + (i % 3)
            for j in range(n_img):
                fn = f"{uid_hex()}.png"
                write_bytes(f"diaries/{did}/images/{fn}", make_png(f"Diary#{did}-{j}", size=(720, 480)))
                names.append(fn)
            cur.execute(
                "UPDATE diary_entry SET image_files=%s WHERE id=%s",
                (json.dumps(names, ensure_ascii=False), did),
            )
        ids.append(did)
    return ids


def seed_bookmarks(cur, cat_ids: list[int], tag_ids: list[int]) -> list[int]:
    sites = [
        ("MDN Web Docs", "https://developer.mozilla.org/", "前端权威文档"),
        ("Vue.js", "https://vuejs.org/", "Vue 官方"),
        ("Spring", "https://spring.io/", "Spring 生态"),
        ("MySQL 手册", "https://dev.mysql.com/doc/", "数据库文档"),
        ("Redis", "https://redis.io/docs/", "缓存文档"),
        ("GitHub", "https://github.com/", "代码托管"),
        ("Stack Overflow", "https://stackoverflow.com/", "问答"),
        ("Can I Use", "https://caniuse.com/", "兼容性"),
        ("Element Plus", "https://element-plus.org/", "UI 库"),
        ("Vite", "https://vitejs.dev/", "构建工具"),
        ("Docker Hub", "https://hub.docker.com/", "镜像"),
        ("Nginx", "https://nginx.org/en/docs/", "代理"),
        ("RFC 7519 JWT", "https://datatracker.ietf.org/doc/html/rfc7519", "JWT"),
        ("Pillow", "https://pillow.readthedocs.io/", "图像库"),
        ("PyMySQL", "https://pymysql.readthedocs.io/", "驱动"),
        ("图标库 Lucide", "https://lucide.dev/", "图标"),
        ("字体 Google Fonts", "https://fonts.google.com/", "字体"),
        ("图床示例", "https://picsum.photos/", "占位图"),
        ("JSON 校验", "https://jsonlint.com/", "工具"),
        ("正则可视化", "https://regex101.com/", "工具"),
        ("Cron 表达式", "https://crontab.guru/", "工具"),
        ("HTTP Status", "https://httpstatuses.com/", "状态码"),
        ("ASCII 表", "https://www.asciitable.com/", "参考"),
        ("时区转换", "https://www.timeanddate.com/worldclock/", "工具"),
        ("颜色对比", "https://webaim.org/resources/contrastchecker/", "无障碍"),
        ("OpenAPI", "https://swagger.io/specification/", "规范"),
        ("Jackson", "https://github.com/FasterXML/jackson", "JSON"),
        ("Lombok", "https://projectlombok.org/", "样板代码"),
        ("Hutool", "https://hutool.cn/", "工具库"),
        ("阿里规约", "https://github.com/alibaba/p3c", "规约"),
    ]
    ids = []
    for i, (title, url, desc) in enumerate(sites):
        bid = insert(cur, "bookmark_url", {
            "user_id": USER_ID,
            "title": title,
            "url": url,
            "description": desc,
            "category_id": cat_ids[i % len(cat_ids)] if cat_ids else None,
            "show_on_dashboard": 1 if i < 10 else 0,
            "is_deleted": 0,
            "created_at": dt_ago(40 - i),
            "updated_at": dt_ago(40 - i),
        })
        if tag_ids:
            bind_tag(cur, tag_ids[i % len(tag_ids)], "bookmark", bid)
            if i % 3 == 0:
                bind_tag(cur, tag_ids[(i + 1) % len(tag_ids)], "bookmark", bid)
        ids.append(bid)
    return ids


def seed_files(cur, cat_ids: list[int]) -> list[int]:
    """覆盖 image / pdf / doc / archive 分组筛选。"""
    specs = []
    # images
    for ext, mime, maker in [
        ("png", "image/png", lambda n: make_png(n)),
        ("jpg", "image/jpeg", lambda n: make_jpeg(n)),
        ("gif", "image/png", lambda n: make_png(n)),  # 用 png 字节但扩展名覆盖筛选即可；改用真实 gif 太重
        ("webp", "image/webp", None),
    ]:
        for i in range(5):
            specs.append((ext, mime, maker, f"截图-{ext}-{i+1}.{ext}"))
    # pdf
    for i in range(6):
        specs.append(("pdf", "application/pdf", lambda n, i=i: make_pdf(n), f"说明书-{i+1}.pdf"))
    # doc group
    for i in range(4):
        specs.append(("txt", "text/plain", lambda n: make_txt(n + "\nline2\n"), f"备忘-{i+1}.txt"))
    for i in range(4):
        specs.append(("md", "text/markdown", lambda n: make_md(n, "seed md file"), f"笔记导出-{i+1}.md"))
    for i in range(4):
        specs.append(("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                      lambda n: make_docx(n), f"报告-{i+1}.docx"))
    # archive
    for i in range(5):
        specs.append(("zip", "application/zip", lambda n: make_zip(n, "readme.txt"), f"打包-{i+1}.zip"))
    for i in range(2):
        specs.append(("rar", "application/x-rar-compressed", lambda n: make_zip(n, "inner.dat"), f"旧包-{i+1}.rar"))

    ids = []
    ym = TODAY.strftime("%Y/%m")
    for i, (ext, mime, maker, name) in enumerate(specs):
        if ext in ("png", "jpg", "jpeg", "gif", "svg", "webp", "bmp"):
            prefix = "image"
        elif ext == "pdf":
            prefix = "document"
        elif ext in ("doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "md"):
            prefix = "document" if ext != "md" else "markdown"
        else:
            prefix = "other"
        stored = f"{uid_hex()}.{ext}"
        rel = f"uploads/{prefix}/{ym}/{stored}"
        if ext == "webp":
            # pillow webp
            img = Image.new("RGB", (320, 240), (100, 140, 200))
            buf = io.BytesIO()
            try:
                img.save(buf, format="WEBP")
                data = buf.getvalue()
            except Exception:
                data = make_png(name)
                rel = rel.replace(".webp", ".png")
                name = name.replace(".webp", ".png")
                ext = "png"
                mime = "image/png"
        elif maker:
            data = maker(name)
        else:
            data = make_png(name)
        path, size = write_bytes(rel, data)
        # gif: write as png content is ok for size; better write real gif via pillow
        if ext == "gif":
            img = Image.new("RGB", (200, 150), (80, 160, 120))
            buf = io.BytesIO()
            img.save(buf, format="GIF")
            path, size = write_bytes(rel, buf.getvalue())

        fid = insert(cur, "file_resource", {
            "user_id": USER_ID,
            "name": name,
            "path": path,
            "size": size,
            "type": ext,
            "mime_type": mime,
            "category_id": cat_ids[i % len(cat_ids)] if cat_ids else None,
            "is_deleted": 0,
            "created_at": dt_ago(30 - (i % 30)),
            "updated_at": dt_ago(30 - (i % 30)),
        })
        ids.append(fid)
    return ids


def seed_readings(cur) -> list[int]:
    books = [
        ("深入理解计算机系统", "Randal Bryant", 0),
        ("设计数据密集型应用", "Martin Kleppmann", 1),
        ("Java 并发编程实战", "Brian Goetz", 1),
        ("重构：改善既有代码的设计", "Martin Fowler", 2),
        ("代码整洁之道", "Robert C. Martin", 2),
        ("人类简史", "尤瓦尔·赫拉利", 2),
        ("原则", "Ray Dalio", 0),
        ("软技能：代码之外的生存指南", "John Sonmez", 1),
        ("算法导论", "Cormen", 0),
        ("Effective Java", "Joshua Bloch", 1),
        ("Vue.js 设计与实现", "霍春阳", 2),
        ("Spring 实战", "Craig Walls", 1),
        ("MySQL 技术内幕", "姜承尧", 0),
        ("高性能 MySQL", "Baron Schwartz", 1),
        ("领域驱动设计", "Eric Evans", 0),
        ("持续交付", "Jez Humble", 2),
        ("凤凰项目", "Gene Kim", 2),
        ("程序员修炼之道", "Hunt & Thomas", 1),
        ("你不知道的 JavaScript", "Kyle Simpson", 0),
        ("CSS 揭秘", "Lea Verou", 1),
        ("HTTP 权威指南", "David Gourley", 2),
        ("图解 HTTP", "上野宣", 2),
        ("操作系统导论", "Remzi", 0),
        ("计算机网络：自顶向下", "Kurose", 1),
    ]
    ids = []
    for i, (title, author, status) in enumerate(books):
        total = RNG.randint(10, 40)
        if status == 0:
            cur_ch, prog = 0, 0
            start, end = None, None
        elif status == 1:
            cur_ch = RNG.randint(1, total - 1)
            prog = int(cur_ch / total * 100)
            start, end = TODAY - timedelta(days=20 + i), None
        else:
            cur_ch, prog = total, 100
            start = TODAY - timedelta(days=90 + i)
            end = TODAY - timedelta(days=10 + i)
        ids.append(insert(cur, "reading_record", {
            "user_id": USER_ID,
            "book_title": title,
            "author": author,
            "cover_url": None,
            "total_chapters": total,
            "current_chapter": cur_ch,
            "progress": prog,
            "status": status,
            "notes": f"读《{title}》的笔记草稿，关键词 {author}。",
            "start_date": start,
            "end_date": end,
            "is_deleted": 0,
            "created_at": dt_ago(100 - i),
            "updated_at": dt_ago(i % 20),
            "total_duration": RNG.randint(30, 2000) if status else 0,
            "rating": RNG.randint(3, 5) if status == 2 else (RNG.randint(1, 5) if status == 1 else None),
        }))
    return ids


def seed_study_plans(cur, tag_ids: list[int]) -> list[int]:
    plans = [
        ("Spring Boot 进阶", "慕课网", "张老师", 0),
        ("Vue3 实战课", "B站", "李老师", 1),
        ("MySQL 优化专题", "极客时间", "丁奇", 1),
        ("算法每日一题", "LeetCode", None, 1),
        ("英语六级冲刺", "网课", "王老师", 2),
        ("系统设计面试", "书籍", "Alex Xu", 2),
        ("Kubernetes 入门", "官方文档", None, 3),
        ("Rust 所有权", "The Book", None, 0),
        ("TypeScript 深入", "官网", None, 1),
        ("Redis 源码阅读", "GitHub", None, 3),
        ("产品感训练", "文章合集", None, 0),
        ("写作练习 30 天", "自驱", None, 1),
        ("摄影构图", "线下课", "陈老师", 2),
        ("健身增肌计划", "自驱", None, 1),
        ("开源贡献打卡", "GitHub", None, 0),
        ("DDD 工作坊", "内训", None, 2),
        ("日志可观测性", "博客", None, 3),
        ("前端性能优化", "文章", None, 1),
        ("安全攻防基础", "课程", None, 0),
        ("软考高项备考", "教材", None, 2),
    ]
    ids = []
    for i, (name, source, author, status) in enumerate(plans):
        if status == 0:
            prog = 0
        elif status == 1:
            prog = RNG.randint(10, 80)
        elif status == 2:
            prog = 100
        else:
            prog = RNG.randint(5, 50)
        start = TODAY - timedelta(days=60 - i)
        end = start + timedelta(days=30 + i)
        pid = insert(cur, "study_plan", {
            "user_id": USER_ID,
            "name": name,
            "source": source,
            "author": author,
            "url": f"https://example.com/plan/{i+1}",
            "remark": f"计划备注：{name}，可检索「{source or '自驱'}」。",
            "progress": prog,
            "start_date": start,
            "end_date": end,
            "status": status,
            "is_deleted": 0,
            "created_at": dt_ago(70 - i),
            "updated_at": dt_ago(i % 15),
        })
        if tag_ids:
            bind_tag(cur, tag_ids[i % len(tag_ids)], "study_plan", pid)
        ids.append(pid)
    return ids


def seed_study_records(cur) -> list[int]:
    subjects = [
        "Spring 事务", "Vue Router", "索引设计", "Redis 集群", "Docker 网络",
        "JWT 刷新", "日志分析", "算法双指针", "英语听力", "系统设计",
    ]
    ids = []
    for i in range(40):
        d = TODAY - timedelta(days=i)
        sub = subjects[i % len(subjects)]
        ids.append(insert(cur, "study_record", {
            "user_id": USER_ID,
            "subject": f"{sub} · Day{i+1}",
            "date": d,
            "duration": RNG.randint(25, 180),
            "content": f"学习内容：{sub}，关键词检索覆盖。",
            "reflection": f"心得：坚持第 {i+1} 天。",
            "is_deleted": 0,
            "created_at": datetime.combine(d, datetime.min.time()) + timedelta(hours=21),
            "updated_at": datetime.combine(d, datetime.min.time()) + timedelta(hours=22),
        }))
    return ids


def seed_todos(cur) -> list[int]:
    """覆盖 dueScope: overdue / today / week / later / done。"""
    items = []
    # overdue
    for i in range(6):
        items.append((f"已逾期任务 {i+1}", TODAY - timedelta(days=i + 1), 0, 1 if i < 2 else 2))
    # today
    for i in range(5):
        items.append((f"今日待办 {i+1}", TODAY, 0, 1 if i == 0 else 2))
    # week (tomorrow .. +7)
    for i in range(8):
        items.append((f"本周任务 {i+1}", TODAY + timedelta(days=i + 1), 0, 2 if i % 2 else 3))
    # later: null due or > +7
    for i in range(5):
        items.append((f"以后再说 {i+1}", None, 0, 3))
    for i in range(4):
        items.append((f"远期计划 {i+1}", TODAY + timedelta(days=10 + i), 0, 2))
    # done
    for i in range(8):
        items.append((f"已完成事项 {i+1}", TODAY - timedelta(days=i), 1, 2))

    ids = []
    for i, (title, due, done, pri) in enumerate(items):
        completed = dt_ago(i) if done else None
        ids.append(insert(cur, "todo_task", {
            "user_id": USER_ID,
            "title": title,
            "content": f"详情：{title}，优先级 {pri}。",
            "is_done": done,
            "priority": pri,
            "due_date": due,
            "is_deleted": 0,
            "created_at": dt_ago(20 - (i % 20)),
            "updated_at": dt_ago(i % 10),
            "completed_at": completed,
        }))
    return ids


def seed_notifications(cur, todo_ids: list[int], plan_ids: list[int]):
    rows = [
        ("TODO_OVERDUE", "待办已逾期", "有任务已超过截止日期", "todo", todo_ids[0] if todo_ids else None, 0),
        ("TODO_OVERDUE", "待办提醒", "另一条逾期提醒", "todo", todo_ids[1] if len(todo_ids) > 1 else None, 0),
        ("PLAN_DEADLINE", "学习计划临近截止", "请尽快推进进度", "study_plan", plan_ids[0] if plan_ids else None, 0),
        ("PLAN_DEADLINE", "计划截止提醒", "本周到期计划", "study_plan", plan_ids[1] if len(plan_ids) > 1 else None, 1),
        ("PLAN_COMPLETED", "计划已完成", "恭喜完成一个学习计划", "study_plan", plan_ids[4] if len(plan_ids) > 4 else None, 0),
    ]
    for i in range(10):
        rows.append((
            "TODO_OVERDUE" if i % 2 == 0 else "PLAN_DEADLINE",
            f"通知样例 {i+1}",
            f"用于列表分页与已读状态演示 {i+1}",
            "todo" if i % 2 == 0 else "study_plan",
            (todo_ids[i % len(todo_ids)] if todo_ids else None) if i % 2 == 0 else (plan_ids[i % len(plan_ids)] if plan_ids else None),
            1 if i % 3 == 0 else 0,
        ))
    for i, (typ, title, content, rtype, rid, read) in enumerate(rows):
        insert(cur, "sys_notification", {
            "user_id": USER_ID,
            "type": typ,
            "title": title,
            "content": content,
            "is_read": read,
            "is_dismissed": 0,
            "related_id": rid,
            "related_type": rtype,
            "created_at": dt_ago(i),
        })


def seed_audit(cur, note_ids: list[int]):
    insert(cur, "audit_log", {
        "module": "AUTH", "business_id": None, "action": "LOGIN",
        "content": "用户登录成功", "operator_id": USER_ID, "created_at": dt_ago(0, 1),
    })
    for i, nid in enumerate(note_ids[-8:]):
        insert(cur, "audit_log", {
            "module": "NOTE", "business_id": nid, "action": "DELETE",
            "content": f"删除笔记 #{nid}", "operator_id": USER_ID, "created_at": dt_ago(i + 1),
        })
    if note_ids:
        insert(cur, "audit_log", {
            "module": "NOTE", "business_id": note_ids[0], "action": "RESTORE",
            "content": f"恢复笔记 #{note_ids[0]}（演示）", "operator_id": USER_ID, "created_at": dt_ago(2),
        })
    for mod, act in [("FILE", "CREATE"), ("DIARY", "CREATE"), ("BOOKMARK", "UPDATE"), ("TODO", "CREATE")]:
        insert(cur, "audit_log", {
            "module": mod, "business_id": 1, "action": act,
            "content": f"{mod} {act} 演示", "operator_id": USER_ID, "created_at": dt_ago(3),
        })


def enrich_user(cur):
    avatar_name = f"{uid_hex()}.png"
    write_bytes(f"avatars/{avatar_name}", make_png("Admin", size=(256, 256), color=(88, 101, 180)))
    cur.execute(
        """
        UPDATE sys_user SET
          nickname=%s, avatar=%s, email=%s, gender=%s, birthday=%s, phone=%s,
          country=%s, province=%s, city=%s, district=%s,
          website=%s, github=%s, bio=%s
        WHERE id=%s
        """,
        (
            "管理员",
            f"/api/files/avatar/{avatar_name}",
            "admin@personal-hub.local",
            1,
            date(1995, 6, 15),
            "13800000000",
            "中国", "浙江", "杭州", "西湖区",
            "https://example.com",
            "https://github.com/example",
            "Personal Hub 演示账号，用于全功能巡检。",
            USER_ID,
        ),
    )


def main():
    print(f"Storage → {STORAGE}")
    STORAGE.mkdir(parents=True, exist_ok=True)
    conn = connect()
    try:
        with conn.cursor() as cur:
            print("Clearing business tables…")
            clear_business(cur)
            enrich_user(cur)
            cats = seed_categories(cur)
            tags = seed_tags(cur)
            print("Seeding notes + files…")
            notes = seed_notes(cur, cats["note"], tags)
            diaries = seed_diaries(cur)
            bookmarks = seed_bookmarks(cur, cats["bookmark"], tags)
            files = seed_files(cur, cats["file"])
            readings = seed_readings(cur)
            plans = seed_study_plans(cur, tags)
            studies = seed_study_records(cur)
            todos = seed_todos(cur)
            seed_notifications(cur, todos, plans)
            seed_audit(cur, notes)
        conn.commit()
        print("Done. Counts:")
        with conn.cursor() as cur:
            for t in [
                "category", "tag", "tag_rel", "note_note", "note_category_rel",
                "diary_entry", "bookmark_url", "file_resource", "reading_record",
                "study_plan", "study_record", "todo_task", "sys_notification", "audit_log",
            ]:
                cur.execute(f"SELECT COUNT(*) AS c FROM `{t}`")
                print(f"  {t}: {cur.fetchone()['c']}")
            cur.execute("SELECT COUNT(*) AS c FROM note_note WHERE is_deleted=1")
            print(f"  notes in recycle: {cur.fetchone()['c']}")
            cur.execute("SELECT COUNT(*) AS c FROM note_note WHERE is_favorite=1 AND is_deleted=0")
            print(f"  favorite notes: {cur.fetchone()['c']}")
        # file tree sample
        n_files = sum(1 for _ in STORAGE.rglob("*") if _.is_file())
        print(f"Disk files under storage: {n_files}")
    except Exception:
        conn.rollback()
        raise
    finally:
        conn.close()


if __name__ == "__main__":
    main()
