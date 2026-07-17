# 后端模块合并为 ph-biz 实现计划

> **状态：已落地（历史档案）**  
> Maven 已收敛为 `ph-common` / `ph-system` / `ph-biz` / `ph-boot`。文中旧模块路径与 `PROJECT_AUDIT` 引用仅作追溯。

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 将 8 个 Maven 模块收敛为 4 个：`ph-common` / `ph-system` / `ph-biz` / `ph-boot`，降低过早拆分带来的构建与依赖税；**不改 Java 包名、不改 API、不改前端**。

**架构：**  
- `ph-biz` = 原 `ph-knowledge` + `ph-planning` + `ph-resource` + `ph-dashboard`（包路径保持 `com.personalhub.knowledge|planning|resource|module.dashboard`）。  
- `ph-storage` **必须**并入 `ph-common`（包仍为 `com.personalhub.storage`），因为 `ph-system` 已依赖 `StorageService`；若把 storage 放进 `ph-biz` 会形成 `system ↔ biz` 循环依赖。  
- `ph-boot` 仅依赖 `ph-common` + `ph-system` + `ph-biz`。

**技术栈：** Java 21 · Maven 多模块 · Spring Boot 3.3 · MyBatis-Plus · 现有 JUnit 5 单测

**规格来源：** 会话结论（方案 B + 定名 `ph-biz`）+ `docs/PROJECT_AUDIT.md` §5 + Canvas `backend-module-review`

**非目标（本计划不做）：**  
- 修复 dashboard 实体泄漏 / 抽跨模块 API  
- 重命名 Java package（如 `module.dashboard` → `dashboard`）  
- 改前端 `modules/` 目录或 API 路径  
- 引入 MapStruct / 微服务拆分

---

## 目标结构

```
personal-hub-server/
├── pom.xml
├── ph-common     # + storage 三文件
├── ph-system     # 去掉对 ph-storage 的显式依赖（改由 common 传递）
├── ph-biz        # knowledge + planning + resource + dashboard
└── ph-boot
```

依赖方向（合并后）：

```
ph-common
    ↑
ph-system
    ↑
ph-biz
    ↑
ph-boot
```

---

## 文件结构（锁定）

| 操作 | 路径 | 职责 |
|------|------|------|
| 创建 | `personal-hub-server/ph-biz/pom.xml` | 业务合集模块 POM |
| 迁入 | knowledge + planning + resource + dashboard 全部 `src/**` | 包名不变，仅换模块目录 |
| 迁入 | `ph-storage` 3 个 Java → `ph-common/.../storage/` | 存储 SPI 沉到 common |
| 修改 | `personal-hub-server/pom.xml` | modules + dependencyManagement |
| 修改 | `ph-common/pom.xml` | 增加 configuration-processor（原 storage 有） |
| 修改 | `ph-system/pom.xml` | 删除 `ph-storage` 依赖 |
| 修改 | `ph-boot/pom.xml` | 只保留 common/system/biz |
| 删除 | `ph-knowledge` `ph-planning` `ph-resource` `ph-dashboard` `ph-storage` 目录 | 旧模块清空 |
| 修改 | `docs/PROJECT.md` `docs/TECH_STACK.md` `docs/STYLE_GUIDE.md` `docs/CHANGELOG.md` `docs/PROJECT_AUDIT.md` `README.md` | 同步模块图 |
| 轻改 | `docs/superpowers/specs/2026-07-17-backend-data-basics-design.md` | 枚举路径改为 `ph-biz/...`（历史计划可不改） |

**策略：** 以 `git mv ph-knowledge ph-biz` 为基底（保留 knowledge 历史），再把其余模块源码 `git mv` 进 `ph-biz`；storage `git mv` 进 `ph-common`。

---

### 任务 0：分支与基线验证

**文件：** 无代码变更

- [ ] **步骤 1：确认工作区干净到可合并（允许已有无关 untracked，但不要有未提交的模块 POM 半成品）**

```bash
cd e:/WorkSpace/VibeCoding/personal-hub
git status
git checkout -b refactor/merge-to-ph-biz
```

- [ ] **步骤 2：记录合并前基线（必须全绿再动手）**

```bash
cd personal-hub-server
mvn -q test
```

预期：BUILD SUCCESS（现有单测全部通过）。若失败，先修基线，不要开始合并。

- [ ] **步骤 3：记录模块列表快照**

```bash
mvn -q -Dexec.skip=true help:evaluate -Dexpression=project.modules -DforceStdout
# 或直接打开父 pom，确认当前 8 个子模块
```

预期 modules 含：common、system、knowledge、planning、resource、storage、dashboard、boot。

---

### 任务 1：将 ph-storage 迁入 ph-common

**文件：**
- 移动：`ph-storage/src/main/java/com/personalhub/storage/*.java` → `ph-common/src/main/java/com/personalhub/storage/`
- 修改：`ph-common/pom.xml`
- 修改：`ph-system/pom.xml`（删 storage 依赖）
- 删除：`ph-storage/` 目录与父 POM 中的 module 声明（本任务末尾一并改父 POM 的 storage 部分，或放到任务 4 统一改——**本任务只做迁文件 + common/system pom，父 POM 在任务 4 统一收口**）

- [ ] **步骤 1：移动 storage 源码（保留 git 历史）**

在 `personal-hub-server` 下执行（PowerShell）：

```powershell
New-Item -ItemType Directory -Force -Path "ph-common\src\main\java\com\personalhub\storage" | Out-Null
git mv ph-storage/src/main/java/com/personalhub/storage/StorageService.java ph-common/src/main/java/com/personalhub/storage/
git mv ph-storage/src/main/java/com/personalhub/storage/StorageProperties.java ph-common/src/main/java/com/personalhub/storage/
git mv ph-storage/src/main/java/com/personalhub/storage/LocalStorageServiceImpl.java ph-common/src/main/java/com/personalhub/storage/
```

- [ ] **步骤 2：ph-common/pom.xml 增加 configuration-processor**

在 `ph-common/pom.xml` 的 `<dependencies>` 内、已有 `spring-boot-starter-web` 附近加入：

```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-configuration-processor</artifactId>
            <optional>true</optional>
        </dependency>
```

说明：`ph-common` 已有 `spring-boot-starter-web`，可覆盖原 `ph-storage` 的 web 依赖，无需再加 web。

- [ ] **步骤 3：ph-system/pom.xml 删除 ph-storage 依赖块**

删除整段：

```xml
        <dependency>
            <groupId>com.personalhub</groupId>
            <artifactId>ph-storage</artifactId>
        </dependency>
```

system 仍依赖 `ph-common`，`StorageService` 通过 common 可见。

- [ ] **步骤 4：暂不删除 ph-storage 目录以外的引用——先编译验证 common+system**

此时父 POM 仍声明 `ph-storage` 模块会导致空模块编译失败。**立刻**从父 `pom.xml` 的 `<modules>` 去掉 `<module>ph-storage</module>`，并从 `<dependencyManagement>` 去掉 `ph-storage` 条目；同时改掉所有仍引用 `ph-storage` artifact 的子 POM：

临时处理（本步必须做完）：
- `ph-knowledge/pom.xml`：删除 `ph-storage` 依赖（改由 common 传递；knowledge 已依赖 common）
- `ph-resource/pom.xml`：删除 `ph-storage` 依赖
- `ph-boot/pom.xml`：删除 `ph-storage` 依赖

然后删除空目录：

```powershell
Remove-Item -Recurse -Force ph-storage
```

若 `ph-storage` 仍被 git 跟踪，用：

```bash
git rm -r ph-storage
```

- [ ] **步骤 5：编译验证**

```bash
cd personal-hub-server
mvn -q -pl ph-common,ph-system -am compile
```

预期：BUILD SUCCESS。`UserController` 的 `import com.personalhub.storage.StorageService` 无需修改。

- [ ] **步骤 6：Commit**

```bash
git add -A personal-hub-server/ph-common personal-hub-server/ph-system personal-hub-server/pom.xml \
  personal-hub-server/ph-knowledge/pom.xml personal-hub-server/ph-resource/pom.xml personal-hub-server/ph-boot/pom.xml
git commit -m "$(cat <<'EOF'
refactor: 将 ph-storage 并入 ph-common

避免后续 ph-biz 与 ph-system 因 StorageService 形成循环依赖。
EOF
)"
```

Windows 若无 HEREDOC，可用：

```powershell
git commit -m "refactor: 将 ph-storage 并入 ph-common"
```

---

### 任务 2：将 ph-knowledge 重命名为 ph-biz

**文件：**
- 重命名目录：`ph-knowledge` → `ph-biz`
- 修改：`ph-biz/pom.xml`（artifactId / name / description）
- 修改：`personal-hub-server/pom.xml`（module 名 + dependencyManagement）
- 修改：仍引用 `ph-knowledge` 的 POM（planning / resource / dashboard / boot）——**本任务只改父 POM 与 ph-biz 自身；下游依赖 artifactId 在任务 3/4 再切到 ph-biz**

为降低中间态编译失败，本任务与任务 3 可连续做完再统一编译；若分 commit，中间态父 POM 需同时保留 knowledge 别名——**推荐本任务只做目录 rename + ph-biz/pom，父 POM 仍暂时用 module 路径 `ph-biz` 但 dependencyManagement 同时保留旧 artifact 名会混乱。采用以下明确步骤：**

- [ ] **步骤 1：git mv 目录**

```bash
cd personal-hub-server
git mv ph-knowledge ph-biz
```

- [ ] **步骤 2：重写 ph-biz/pom.xml**

将文件内容替换为：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.personalhub</groupId>
        <artifactId>personal-hub</artifactId>
        <version>0.1.0</version>
        <relativePath>../pom.xml</relativePath>
    </parent>

    <artifactId>ph-biz</artifactId>
    <name>ph-biz</name>
    <description>业务合集：知识 / 规划 / 资源 / Dashboard 聚合</description>

    <dependencies>
        <dependency>
            <groupId>com.personalhub</groupId>
            <artifactId>ph-common</artifactId>
        </dependency>
        <dependency>
            <groupId>com.personalhub</groupId>
            <artifactId>ph-system</artifactId>
        </dependency>

        <dependency>
            <groupId>org.apache.poi</groupId>
            <artifactId>poi-ooxml</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

说明：不再依赖 `ph-storage`；poi 保留（原 knowledge + planning 都用）。

- [ ] **步骤 3：父 pom.xml 模块列表——将 knowledge 改为 biz**

`<modules>` 中：

```xml
        <module>ph-biz</module>
```

替换原来的 `<module>ph-knowledge</module>`。

`<dependencyManagement>` 中：

```xml
            <dependency>
                <groupId>com.personalhub</groupId>
                <artifactId>ph-biz</artifactId>
                <version>${project.version}</version>
            </dependency>
```

替换原来的 `ph-knowledge` 条目。

- [ ] **步骤 4：临时修复仍引用 ph-knowledge 的子模块 POM（否则无法 reactor 构建）**

编辑以下文件，把 `ph-knowledge` 依赖改为 `ph-biz`：

1. `ph-planning/pom.xml`
2. `ph-resource/pom.xml`
3. `ph-dashboard/pom.xml`
4. `ph-boot/pom.xml`

示例（四处相同替换）：

```xml
        <dependency>
            <groupId>com.personalhub</groupId>
            <artifactId>ph-biz</artifactId>
        </dependency>
```

注意：此时 planning/resource/dashboard 仍是独立模块，它们依赖 `ph-biz`（含原 knowledge），**可以编译**，只是稍后会被并入。

- [ ] **步骤 5：编译**

```bash
mvn -q -pl ph-biz,ph-planning,ph-resource,ph-dashboard,ph-boot -am compile
```

预期：BUILD SUCCESS。

- [ ] **步骤 6：Commit**

```bash
git add -A personal-hub-server
git commit -m "refactor: 将 ph-knowledge 重命名为 ph-biz"
```

---

### 任务 3：迁入 planning / resource / dashboard 源码

**文件：**
- 移动：`ph-planning/src/**` → `ph-biz/src/`（合并进同一 src 树）
- 移动：`ph-resource/src/**` → `ph-biz/src/`
- 移动：`ph-dashboard/src/**` → `ph-biz/src/`
- 删除：三个旧模块目录
- 修改：父 POM / boot POM（任务 4 收口也可，本任务结束前必须去掉三个 module）

- [ ] **步骤 1：移动源码（包路径不变，直接合并到 ph-biz/src）**

PowerShell（在 `personal-hub-server`）：

```powershell
# planning
git mv ph-planning/src/main/java/com/personalhub/planning ph-biz/src/main/java/com/personalhub/planning
if (Test-Path ph-planning/src/test/java/com/personalhub/planning) {
  New-Item -ItemType Directory -Force -Path "ph-biz\src\test\java\com\personalhub" | Out-Null
  git mv ph-planning/src/test/java/com/personalhub/planning ph-biz/src/test/java/com/personalhub/planning
}

# resource
git mv ph-resource/src/main/java/com/personalhub/resource ph-biz/src/main/java/com/personalhub/resource

# dashboard（注意包是 module.dashboard）
New-Item -ItemType Directory -Force -Path "ph-biz\src\main\java\com\personalhub\module" | Out-Null
git mv ph-dashboard/src/main/java/com/personalhub/module/dashboard ph-biz/src/main/java/com/personalhub/module/dashboard
```

若 `git mv` 因目标已存在失败，改用：

```powershell
# 示例：复制后 git add + git rm 源（仅当 git mv 不可用）
Copy-Item -Recurse -Force ph-planning/src/main/java/com/personalhub/planning ph-biz/src/main/java/com/personalhub/
git add ph-biz/src/main/java/com/personalhub/planning
git rm -r ph-planning/src/main/java/com/personalhub/planning
```

- [ ] **步骤 2：确认 ph-biz 下包树**

预期存在：

```
ph-biz/src/main/java/com/personalhub/
├── knowledge/
├── planning/
├── resource/
└── module/dashboard/
```

测试：

```
ph-biz/src/test/java/com/personalhub/
├── knowledge/   # 原有
└── planning/    # 迁入的枚举单测等
```

- [ ] **步骤 3：删除空模块目录**

```bash
git rm -r ph-planning ph-resource ph-dashboard
```

若目录仍有残留 `pom.xml` / `target`：

```powershell
Remove-Item -Recurse -Force ph-planning, ph-resource, ph-dashboard -ErrorAction SilentlyContinue
```

- [ ] **步骤 4：父 pom.xml 去掉三个 module 与 dependencyManagement 条目**

从 `<modules>` 删除：

```xml
        <module>ph-planning</module>
        <module>ph-resource</module>
        <module>ph-dashboard</module>
```

从 `<dependencyManagement>` 删除 `ph-planning` / `ph-resource` / `ph-dashboard` 三个 dependency 块。

最终 `<modules>` 应为：

```xml
    <modules>
        <module>ph-common</module>
        <module>ph-system</module>
        <module>ph-biz</module>
        <module>ph-boot</module>
    </modules>
```

- [ ] **步骤 5：重写 ph-boot/pom.xml 依赖**

内部模块依赖仅保留：

```xml
        <dependency>
            <groupId>com.personalhub</groupId>
            <artifactId>ph-common</artifactId>
        </dependency>
        <dependency>
            <groupId>com.personalhub</groupId>
            <artifactId>ph-system</artifactId>
        </dependency>
        <dependency>
            <groupId>com.personalhub</groupId>
            <artifactId>ph-biz</artifactId>
        </dependency>
```

删除对 `ph-planning` / `ph-resource` / `ph-dashboard` / `ph-storage` / 旧 `ph-knowledge` 的依赖（若仍存在）。

保留：springdoc、mysql、test、security-test、spring-boot-maven-plugin 不变。

- [ ] **步骤 6：全量编译 + 测试**

```bash
cd personal-hub-server
mvn -q clean test
```

预期：BUILD SUCCESS。  
单测至少覆盖：`EntityGuardTest`、`ReadingStatusTest`、`DiaryMoodTest`、`StudyPlanStatusTest`、`TodoPriorityTest`、knowledge 相关测试、boot 测试。

若出现 `duplicate class`：说明旧模块目录未删净或 IDE 仍编译 target——执行 `mvn clean` 并确认 `ph-planning` 等目录不存在。

若出现 `cannot find symbol` 指向 planning/resource：说明源码未迁入 `ph-biz`，回到步骤 1。

- [ ] **步骤 7：Commit**

```bash
git add -A personal-hub-server
git commit -m "refactor: 将 planning/resource/dashboard 并入 ph-biz"
```

---

### 任务 4：扫描残留引用 + IDE/CI 检查

**文件：** 全仓库文本扫描

- [ ] **步骤 1：搜索旧 artifact / 目录名**

在仓库根目录：

```bash
rg -n "ph-knowledge|ph-planning|ph-resource|ph-storage|ph-dashboard" --glob '!**/target/**' --glob '!**/node_modules/**' --glob '!**/logs/**'
```

预期命中应仅剩：
- 历史文档 / 旧 plans / CHANGELOG 历史条目（可保留）
- **本任务要更新的现行文档**（下一步）
- 本合并计划自身

不应再命中：任何 `pom.xml`、Java、yml、Dockerfile、GitHub Actions。

- [ ] **步骤 2：确认启动类无需改包扫描**

`PersonalHubApplication` 位于 `com.personalhub`，`@SpringBootApplication` 默认扫描 `com.personalhub.**`，已覆盖 `knowledge` / `planning` / `resource` / `module.dashboard` / `storage` / `system` / `common`。  
**不要**新增无谓的 `@ComponentScan`。

- [ ] **步骤 3：启动冒烟（可选但推荐）**

```bash
cd personal-hub-server
mvn -pl ph-boot -am spring-boot:run -Dspring-boot.run.profiles=dev
```

预期日志含「Personal Hub 启动完成」；Swagger `http://localhost:8080/swagger-ui.html` 可开（端口以配置为准）。Ctrl+C 结束。

- [ ] **步骤 4：Commit（若本任务只有扫描无文件变更可跳过）**

---

### 任务 5：同步文档（提交前铁律）

**文件：**
- 修改：`docs/PROJECT.md`
- 修改：`docs/TECH_STACK.md`
- 修改：`docs/STYLE_GUIDE.md`
- 修改：`docs/CHANGELOG.md`
- 修改：`docs/PROJECT_AUDIT.md`（§5.2 模块职责表）
- 修改：`README.md`
- 修改：`docs/superpowers/specs/2026-07-17-backend-data-basics-design.md`（枚举模块列改为 `ph-biz/...`）

- [ ] **步骤 1：更新 PROJECT.md「领域模块设计」**

替换为：

```markdown
## 领域模块设计

```
后端 ph-*                    前端 modules/
ph-boot / ph-common          —
ph-system                    system/
ph-biz                       knowledge/ + planning/ + resource/
                             + dashboard/ + search/ + category/
  └ packages: knowledge, planning, resource, module.dashboard
```

### 划分原则
Maven 粗粒度（common / system / biz / boot）｜细领域用 Java package｜跨包优先走 Service｜Dashboard 只聚合不落业务库

## 依赖关系

```
ph-common ← ph-system ← ph-biz ← ph-boot
```
```

注意：旧文档写的 `ph-system → ph-knowledge/...` 方向是错的，合并时一并纠正。

- [ ] **步骤 2：更新 STYLE_GUIDE.md Maven 结构节**

```markdown
#### 当前结构（4 模块）

```
personal-hub-server/
├── pom.xml
├── ph-common    # 含 storage SPI（com.personalhub.storage）
├── ph-system
├── ph-biz       # knowledge / planning / resource / module.dashboard
└── ph-boot      # resources: application.yml + -dev / -prod
```

模块内：`controller/ dto/ entity/ mapper/ service/ vo/`。Maven 不按每个业务拆 jar；细领域用 package。
```

- [ ] **步骤 3：更新 TECH_STACK.md「Maven 多模块」段**

改为：父 POM 管版本；模块为 `ph-common` / `ph-system` / `ph-biz` / `ph-boot`；仅 `ph-boot` 打可执行 JAR。

- [ ] **步骤 4：更新 README.md 后端结构树**

```text
personal-hub-server/
├── ph-boot          启动入口
├── ph-common        公共能力 + 存储 SPI（Result / JWT / Security / StorageService）
├── ph-system        用户系统（登录 / 通知 / 布局 / 审计）
└── ph-biz           业务合集
                     · knowledge   笔记 / 日记 / 学习 / 阅读 / 标签 / 分类
                     · planning    Todo / 学习计划
                     · resource    收藏夹 / 文件 / 笔记附件
                     · dashboard   统计 / 搜索 / 趋势聚合
```

前端 modules 树**保持不变**。

- [ ] **步骤 5：CHANGELOG.md 顶部追加一条**

```markdown
### 重构
- 后端 Maven 模块 8→4：`ph-knowledge/planning/resource/dashboard` 合并为 `ph-biz`；`ph-storage` 并入 `ph-common`
```

（按现有 CHANGELOG 日期分组风格放置到当天版本节。）

- [ ] **步骤 6：PROJECT_AUDIT.md §5.2 改一句**

将「common/system/knowledge/storage/boot ✅｜planning/resource ⚠️｜dashboard ❌」更新为：

```markdown
common/system/biz/boot ✅｜biz 内 dashboard 包仍存在跨领域 Entity 直连（后续债，非本次范围）｜storage 已沉 common
```

- [ ] **步骤 7：规格文档枚举路径**

`docs/superpowers/specs/2026-07-17-backend-data-basics-design.md` 表格中 `ph-knowledge/...` / `ph-planning/...` 改为 `ph-biz/.../knowledge|planning/...`。

- [ ] **步骤 8：文档 Commit**

```bash
git add docs/PROJECT.md docs/TECH_STACK.md docs/STYLE_GUIDE.md docs/CHANGELOG.md \
  docs/PROJECT_AUDIT.md README.md \
  docs/superpowers/specs/2026-07-17-backend-data-basics-design.md
git commit -m "docs: 同步后端 4 模块结构（ph-biz）"
```

---

### 任务 6：最终验证清单

- [ ] **步骤 1：模块数确认**

```bash
cd personal-hub-server
ls -d ph-*
```

预期仅：`ph-biz` `ph-boot` `ph-common` `ph-system`（Windows：`Get-ChildItem -Directory ph-*`）。

- [ ] **步骤 2：全量测试**

```bash
mvn -q clean test
```

预期：BUILD SUCCESS。

- [ ] **步骤 3：确认无旧 artifact 依赖**

```bash
rg -n "artifactId>ph-(knowledge|planning|resource|storage|dashboard)<" personal-hub-server --glob 'pom.xml'
```

预期：无匹配。

- [ ] **步骤 4：对照非目标——确认未改 API / 前端**

```bash
git diff main --stat -- personal-hub-web docs/API.md
```

预期：`personal-hub-web` 与 `docs/API.md` 无业务协议变更（允许 0 diff）。若当前分支不叫 main，换成实际基线分支。

---

## 风险与回滚

| 风险 | 缓解 |
|------|------|
| `system ↔ biz` 循环依赖 | storage 只进 common（任务 1） |
| git mv 在 Windows 失败 | 用 Copy-Item + git add/rm 回退方案 |
| IDE 缓存旧模块 | `mvn clean`；Invalidate caches / 重导入 Maven |
| 历史 plans 仍写旧模块名 | 保留历史；仅更新现行 docs |
| dashboard 泄漏未修 | 明确非目标；审计文档标注后续债 |

回滚：`git checkout main`（或合并前分支）即可；合并过程建议每任务一 commit，便于 `git revert`。

---

## 后续债（不在本计划）

1. dashboard 禁止直接依赖 Entity/Mapper，改为各领域查询接口  
2. 可选：将 `com.personalhub.module.dashboard` 包名简化为 `com.personalhub.dashboard`  
3. 单领域持续 >5k LOC 且出现硬边界时，再考虑从 `ph-biz` 拆回独立模块  

---

## 自检（写作时已核对）

| 需求 | 对应任务 |
|------|----------|
| 定名 ph-biz | 任务 2 |
| knowledge+planning+resource 合并 | 任务 2–3 |
| dashboard 并入 | 任务 3 |
| storage 并入且无环依赖 | 任务 1 → common |
| boot 依赖收敛 | 任务 3 步骤 5 |
| 包名/API/前端不动 | 全文非目标 + 任务 6 |
| docs 铁律 | 任务 5 |
| 验证 | 任务 0 / 3 / 6 |
