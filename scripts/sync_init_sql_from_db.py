# -*- coding: utf-8 -*-
"""Sync sql/init.sql CREATE TABLE blocks from live personal_hub schema."""
import re
from pathlib import Path

import pymysql

ROOT = Path(__file__).resolve().parents[1]
INIT = ROOT / "sql" / "init.sql"

ORDER = [
    "bookmark_url",
    "diary_entry",
    "category",
    "file_resource",
    "note_category_rel",
    "note_note",
    "reading_record",
    "study_plan",
    "study_record",
    "sys_notification",
    "sys_user",
    "tag",
    "tag_rel",
    "todo_task",
    "user_layout",
    "audit_log",
]


def extract_seed_blocks(init_text: str) -> dict[str, str]:
    """Only keep sys_user seed INSERT; drop other demo data."""
    seeds: dict[str, str] = {}
    for m in re.finditer(
        r"-- ----------------------------\n-- Records of (\w+)\n-- ----------------------------\n(.*?)(?=\n-- ----------------------------\n-- Table structure|\nSET FOREIGN_KEY_CHECKS)",
        init_text,
        re.S,
    ):
        table = m.group(1)
        body = m.group(2).rstrip() + "\n"
        if table == "sys_user":
            # keep only INSERT lines for sys_user
            kept = [ln for ln in body.splitlines() if ln.startswith("INSERT INTO")]
            seeds[table] = ("\n".join(kept) + "\n") if kept else ""
        else:
            seeds[table] = ""
    return seeds


def main() -> None:
    old = INIT.read_text(encoding="utf-8")
    seeds = extract_seed_blocks(old)

    conn = pymysql.connect(
        host="127.0.0.1",
        user="al_user",
        password="123456",
        database="personal_hub",
        charset="utf8mb4",
    )
    try:
        with conn.cursor() as cur:
            cur.execute("SHOW TABLES")
            tables = [row[0] for row in cur.fetchall()]
            ordered = [t for t in ORDER if t in tables] + [t for t in tables if t not in ORDER]

            creates: dict[str, str] = {}
            for table in ordered:
                cur.execute(f"SHOW CREATE TABLE `{table}`")
                row = cur.fetchone()
                ddl = row[1]
                ddl = re.sub(r" AUTO_INCREMENT=\d+", "", ddl)
                # 项目统一校对集；列不声明字符集，继承表级
                ddl = ddl.replace("utf8mb4_0900_ai_ci", "utf8mb4_general_ci")
                ddl = re.sub(
                    r"(?<!DEFAULT) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci",
                    "",
                    ddl,
                )
                creates[table] = ddl.strip().rstrip(";") + ";"
    finally:
        conn.close()

    parts: list[str] = [
        "-- Personal Hub 数据库初始化脚本",
        "-- 表结构以 live personal_hub 库为准同步",
        "CREATE DATABASE IF NOT EXISTS personal_hub DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;",
        "",
        "",
        "SET NAMES utf8mb4;",
        "SET FOREIGN_KEY_CHECKS = 0;",
        "",
    ]

    for table in ordered:
        parts.append("-- ----------------------------")
        parts.append(f"-- Table structure for {table}")
        parts.append("-- ----------------------------")
        parts.append(f"DROP TABLE IF EXISTS `{table}`;")
        parts.append(creates[table])
        parts.append("")
        parts.append("-- ----------------------------")
        parts.append(f"-- Records of {table}")
        parts.append("-- ----------------------------")
        seed = seeds.get(table, "")
        if seed.strip():
            parts.append(seed.rstrip())
        parts.append("")

    parts.append("SET FOREIGN_KEY_CHECKS = 1;")
    parts.append("")

    INIT.write_text("\n".join(parts), encoding="utf-8")
    print(f"Wrote {INIT} with {len(ordered)} tables")
    for t in ordered:
        print(" -", t, "seed=" + ("yes" if seeds.get(t, "").strip() else "no"))


if __name__ == "__main__":
    # restore seed source if previous broken run
    main()
