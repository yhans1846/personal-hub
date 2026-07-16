# 后端基础数据优化实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 落地状态枚举、`EntityGuard` 归属校验、Entity 默认值收口；不引入 MapStruct。

**架构：** `ph-common` 提供 `EntityGuard`；各业务模块新增 `enums` 包；VO `from` 用 `labelOf` 替换 switch；带 `userId` 的 ServiceImpl 统一 `requireOwned`；create 路径依赖 Entity 字段初值。

**技术栈：** Java 21 · Spring Boot 3.3 · JUnit 5 · 现有 MyBatis-Plus Entity/VO

**规格：** `docs/superpowers/specs/2026-07-17-backend-data-basics-design.md`

---

## 文件结构

| 文件 | 职责 |
|------|------|
| 创建 `ph-common/.../util/EntityGuard.java` | 归属校验工具 |
| 创建 `ph-common/.../util/EntityGuardTest.java` | Guard 单测 |
| 创建 `ph-knowledge/.../enums/ReadingStatus.java` | 阅读状态码表 |
| 创建 `ph-knowledge/.../enums/DiaryMood.java` | 日记心情码表 |
| 创建 `ph-knowledge/.../enums/ReadingStatusTest.java` | labelOf 单测 |
| 创建 `ph-knowledge/.../enums/DiaryMoodTest.java` | labelOf 单测 |
| 创建 `ph-planning/.../enums/StudyPlanStatus.java` | 学习计划状态 |
| 创建 `ph-planning/.../enums/TodoPriority.java` | 待办优先级 |
| 创建 `ph-planning/.../enums/StudyPlanStatusTest.java` | labelOf 单测 |
| 创建 `ph-planning/.../enums/TodoPriorityTest.java` | labelOf 单测 |
| 修改 `ReadingVO` / `DiaryVO` / `StudyPlanVO` / `TodoVO` | `from` 改用枚举 |
| 修改 `ReadingRecord` / `StudyPlan` / `Category` | 字段初值 |
| 修改对应 `*ServiceImpl` | Guard + 去掉 create `?: 0` |
| 修改 `docs/STYLE_GUIDE.md` / `docs/CHANGELOG.md` | 约定与变更记录 |

**Note 特例：** `NoteServiceImpl` 在 `requireOwned` 之后，对需排除回收站的读路径仍检查 `isDeleted == 1` 并抛 `NotFoundException`（与现行为一致）。

**不做：** Dashboard、UserService、Notification、UserLayout、FileVO.typeLabel、MapStruct。

---

### 任务 1：EntityGuard + 单测

**文件：**
- 创建：`personal-hub-server/ph-common/src/main/java/com/personalhub/common/util/EntityGuard.java`
- 测试：`personal-hub-server/ph-common/src/test/java/com/personalhub/common/util/EntityGuardTest.java`

- [ ] **步骤 1：编写失败的测试**

```java
package com.personalhub.common.util;

import com.personalhub.common.exception.NotFoundException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EntityGuardTest {

    static class Item {
        Long userId;
        Item(Long userId) { this.userId = userId; }
        Long getUserId() { return userId; }
    }

    @Test
    void requireOwned_null_throws() {
        assertThrows(NotFoundException.class,
                () -> EntityGuard.requireOwned(null, 1L, Item::getUserId, "不存在"));
    }

    @Test
    void requireOwned_wrongUser_throws() {
        assertThrows(NotFoundException.class,
                () -> EntityGuard.requireOwned(new Item(2L), 1L, Item::getUserId, "不存在"));
    }

    @Test
    void requireOwned_ok_returnsEntity() {
        Item item = new Item(1L);
        assertSame(item, EntityGuard.requireOwned(item, 1L, Item::getUserId, "不存在"));
    }
}
```

- [ ] **步骤 2：运行测试验证失败**

```bash
cd personal-hub-server
mvn -pl ph-common -Dtest=EntityGuardTest test
```

预期：FAIL（找不到 `EntityGuard`）

- [ ] **步骤 3：实现 EntityGuard**

```java
package com.personalhub.common.util;

import com.personalhub.common.exception.NotFoundException;

import java.util.Objects;
import java.util.function.Function;

/**
 * 实体归属校验：不存在或不属于当前用户时统一抛 NotFoundException。
 */
public final class EntityGuard {

    private EntityGuard() {
    }

    /**
     * @param entity       查询结果，可为 null
     * @param userId       当前用户 ID
     * @param userIdGetter 从实体取归属用户 ID
     * @param message      对外提示（统一「不存在」语义）
     * @return 非空且归属正确的实体
     */
    public static <T> T requireOwned(
            T entity,
            Long userId,
            Function<T, Long> userIdGetter,
            String message) {
        if (entity == null || !Objects.equals(userIdGetter.apply(entity), userId)) {
            throw new NotFoundException(message);
        }
        return entity;
    }
}
```

- [ ] **步骤 4：运行测试验证通过**

```bash
mvn -pl ph-common -Dtest=EntityGuardTest test
```

预期：PASS

- [ ] **步骤 5：Commit**

```bash
git add personal-hub-server/ph-common/src/main/java/com/personalhub/common/util/EntityGuard.java \
  personal-hub-server/ph-common/src/test/java/com/personalhub/common/util/EntityGuardTest.java
git commit -m "feat(common): 新增 EntityGuard 归属校验工具"
```

---

### 任务 2：knowledge 枚举 + VO + 单测

**文件：**
- 创建：`.../knowledge/enums/ReadingStatus.java`、`DiaryMood.java`
- 测试：`.../knowledge/enums/ReadingStatusTest.java`、`DiaryMoodTest.java`
- 修改：`.../vo/ReadingVO.java`、`.../vo/DiaryVO.java`

- [ ] **步骤 1：编写 ReadingStatus / DiaryMood 失败测试**

`ReadingStatusTest.java`：

```java
package com.personalhub.knowledge.enums;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ReadingStatusTest {
    @Test void labelOf_null() { assertNull(ReadingStatus.labelOf(null)); }
    @Test void labelOf_known() { assertEquals("在读", ReadingStatus.labelOf(1)); }
    @Test void labelOf_unknown() { assertEquals("未知", ReadingStatus.labelOf(99)); }
}
```

`DiaryMoodTest.java`：null → null；`1` → `"很好"`；`99` → `"未知"`。

- [ ] **步骤 2：运行确认失败**

```bash
mvn -pl ph-knowledge -Dtest=ReadingStatusTest,DiaryMoodTest test
```

预期：FAIL

- [ ] **步骤 3：实现枚举**

`ReadingStatus`：

```java
package com.personalhub.knowledge.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReadingStatus {
    UNREAD(0, "未读"),
    READING(1, "在读"),
    DONE(2, "读完");

    private final int code;
    private final String label;

    public static String labelOf(Integer code) {
        if (code == null) {
            return null;
        }
        for (ReadingStatus s : values()) {
            if (s.code == code) {
                return s.label;
            }
        }
        return "未知";
    }
}
```

`DiaryMood`：`GREAT(1,"很好")` … `TERRIBLE(5,"很差")`，同样 `labelOf`。

- [ ] **步骤 4：改造 VO.from**

`ReadingVO.from`：删除 status switch，改为 `vo.setStatusLabel(ReadingStatus.labelOf(r.getStatus()));`

`DiaryVO.from`：`vo.setMoodLabel(DiaryMood.labelOf(entry.getMood()));`

- [ ] **步骤 5：运行测试通过**

```bash
mvn -pl ph-knowledge -Dtest=ReadingStatusTest,DiaryMoodTest test
```

预期：PASS

- [ ] **步骤 6：Commit**

```bash
git commit -m "feat(knowledge): 阅读/日记状态枚举并替换 VO switch"
```

---

### 任务 3：planning 枚举 + VO + 单测

**文件：**
- 创建：`.../planning/enums/StudyPlanStatus.java`、`TodoPriority.java` 及对应 `*Test`
- 修改：`.../vo/StudyPlanVO.java`、`.../vo/TodoVO.java`

- [ ] **步骤 1：编写失败测试**

- `StudyPlanStatus.labelOf(1)` → `"学习中"`；null → null；99 → `"未知"`
- `TodoPriority.labelOf(1)` → `"高"`；null → null；99 → `"未知"`

- [ ] **步骤 2：运行确认失败** → 实现枚举 → 测试通过

枚举值：

- `StudyPlanStatus`：`NOT_STARTED(0,"未开始")` / `LEARNING(1,"学习中")` / `COMPLETED(2,"已完成")` / `PAUSED(3,"已暂停")`
- `TodoPriority`：`HIGH(1,"高")` / `MEDIUM(2,"中")` / `LOW(3,"低")`

- [ ] **步骤 3：VO.from 改用 `labelOf`**

- [ ] **步骤 4：Commit**

```bash
git commit -m "feat(planning): 计划状态/待办优先级枚举并替换 VO switch"
```

---

### 任务 4：Entity 默认值 + Service create 收口

**文件：**
- 修改：`ReadingRecord.java`、`StudyPlan.java`、`Category.java`
- 修改：`ReadingRecordServiceImpl.java`、`StudyPlanServiceImpl.java`、`CategoryServiceImpl.java`

- [ ] **步骤 1：Entity 字段初值**

```java
// ReadingRecord
private Integer totalChapters = 0;
private Integer currentChapter = 0;
private Integer progress = 0;
private Integer status = 0;

// StudyPlan
private Integer progress = 0;
private Integer status = 0;

// Category
private Integer sortOrder = 0;
```

- [ ] **步骤 2：Service create 简化**

`ReadingRecordServiceImpl.create`：将

```java
r.setTotalChapters(dto.getTotalChapters() != null ? dto.getTotalChapters() : 0);
// ... progress / status 同理
```

改为（仅 DTO 非 null 时覆盖，否则保留 Entity 初值）：

```java
if (dto.getTotalChapters() != null) {
    r.setTotalChapters(dto.getTotalChapters());
}
if (dto.getCurrentChapter() != null) {
    r.setCurrentChapter(dto.getCurrentChapter());
}
if (dto.getProgress() != null) {
    r.setProgress(dto.getProgress());
}
if (dto.getStatus() != null) {
    r.setStatus(dto.getStatus());
}
```

或先 `setXxx(dto.getXxx())` 且仅对可能为 null 且需保留默认的字段用 if。书名等必填字段仍直接 set。

`StudyPlanServiceImpl` create 中 `progress`/`status` 同理去掉 `?: 0`。

`CategoryServiceImpl` create：`if (dto.getSortOrder() != null) category.setSortOrder(dto.getSortOrder());`

导出单元格的 `vo.getX() != null ? … : 0` **保留**（防历史脏数据）。

- [ ] **步骤 3：编译相关模块**

```bash
mvn -pl ph-knowledge,ph-planning -am compile -DskipTests
```

预期：BUILD SUCCESS

- [ ] **步骤 4：Commit**

```bash
git commit -m "refactor: Entity 默认值收口，简化 create 兜底逻辑"
```

---

### 任务 5：ServiceImpl 全面替换 EntityGuard

**文件（均修改）：**
- knowledge：`ReadingRecordServiceImpl`、`DiaryEntryServiceImpl`、`StudyRecordServiceImpl`、`CategoryServiceImpl`、`TagServiceImpl`、`NoteServiceImpl`
- planning：`TodoTaskServiceImpl`、`StudyPlanServiceImpl`
- resource：`BookmarkUrlServiceImpl`、`FileResourceServiceImpl`、`NoteFileServiceImpl`

- [ ] **步骤 1：标准替换模式**

将：

```java
Xxx entity = mapper.selectById(id);
if (entity == null || !entity.getUserId().equals(userId)) {
    throw new NotFoundException("xxx不存在");
}
```

改为：

```java
Xxx entity = EntityGuard.requireOwned(
        mapper.selectById(id), userId, Xxx::getUserId, "xxx不存在");
```

import：`com.personalhub.common.util.EntityGuard`

- [ ] **步骤 2：NoteServiceImpl 特例**

原逻辑含 `isDeleted == 1` 的路径：

```java
Note note = EntityGuard.requireOwned(
        noteMapper.selectById(id), userId, Note::getUserId, "笔记不存在");
if (Integer.valueOf(1).equals(note.getIsDeleted())) {
    throw new NotFoundException("笔记不存在");
}
```

仅「回收站/恢复」等本就不检查 isDeleted 的路径：只调 `requireOwned`。

- [ ] **步骤 3：编译全后端**

```bash
cd personal-hub-server
mvn -pl ph-boot -am test -DskipTests=false -Dtest=EntityGuardTest,ReadingStatusTest,DiaryMoodTest,StudyPlanStatusTest,TodoPriorityTest
```

或分模块跑测试后：

```bash
mvn -pl ph-boot -am compile -DskipTests
```

预期：BUILD SUCCESS

- [ ] **步骤 4：Commit**

```bash
git commit -m "refactor: Service 归属校验统一使用 EntityGuard"
```

---

### 任务 6：文档同步

**文件：**
- 修改：`docs/STYLE_GUIDE.md`、`docs/CHANGELOG.md`
- 修改：`docs/superpowers/specs/2026-07-17-backend-data-basics-design.md` 状态为「已实现」（完成全部编码后）

- [ ] **步骤 1：STYLE_GUIDE — 在「公共组件编码思想」下追加**

```markdown
#### 4. 状态码文案用模块枚举
有固定 int 码表的字段（阅读状态、日记心情、计划状态、待办优先级）使用模块 `enums` 包枚举，VO 标签用 `XxxEnum.labelOf(code)`，禁止在 VO/Service 内嵌业务 switch。

#### 5. 归属校验用 EntityGuard
`selectById` 后校验「存在且属于当前用户」统一调用 `EntityGuard.requireOwned(entity, userId, Entity::getUserId, "xxx不存在")`，禁止手写 null + userId 判断（笔记回收站等额外条件可在 Guard 之后追加）。
```

- [ ] **步骤 2：CHANGELOG Unreleased 追加**

```markdown
### 2026-07-17 后端基础数据优化
- **枚举**：ReadingStatus / DiaryMood / StudyPlanStatus / TodoPriority，VO label 统一 labelOf
- **EntityGuard**：归属校验收口到 ph-common
- **默认值**：ReadingRecord / StudyPlan / Category 字段初值，create 去掉散落 ?: 0
```

- [ ] **步骤 3：规格状态改为「已实现」**

- [ ] **步骤 4：Commit**

```bash
git commit -m "docs: 同步基础数据优化约定与 CHANGELOG"
```

---

## 自检（相对规格）

| 规格章节 | 对应任务 |
|----------|----------|
| §1 状态枚举 | 任务 2、3 |
| §2 默认值 | 任务 4 |
| §3 EntityGuard | 任务 1、5 |
| §4 文档与测试 | 任务 1–3、6 |
| §5 兼容 / §6 验收 | 任务 5 编译 + 任务 6 |

无 MapStruct；Note isDeleted 特例已写明；Dashboard/User 不改造。
