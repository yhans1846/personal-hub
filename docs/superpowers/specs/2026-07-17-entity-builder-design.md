# Entity @Builder 改造 — 设计规格

日期：2026-07-17  
状态：已确认

## 目标

在 Entity create 路径使用 Lombok `@Builder` 链式构造，减少 `new` + 连续 `set`；VO / update 保持现状。

## 非目标

- 不改 VO（不强制 `@Builder`，保留静态 `from`）
- 不改 DTO、不引入 MapStruct
- update 仍用 setter / 条件 set（局部更新不适合 Builder）

## 决策

采用 **Lombok 四件套**：`@Data` + `@Builder` + `@NoArgsConstructor` + `@AllArgsConstructor`。  
有字段初值时使用 `@Builder.Default`，避免 builder 丢弃默认值。

---

## 1. Entity 注解约定

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("reading_record")
public class ReadingRecord {
    @Builder.Default
    private Integer status = 0;
    // ...
}
```

| 注解 | 作用 |
|------|------|
| `@Data` | getter/setter（MyBatis / update 仍需要） |
| `@Builder` | create 链式构造 |
| `@NoArgsConstructor` | MyBatis 反序列化 |
| `@AllArgsConstructor` | 与 Builder 共存所需 |
| `@Builder.Default` | 保留 `= 0` 等字段初值 |

已有仅 `@Builder` 的 `Category`：补齐 `@NoArgsConstructor` + `@AllArgsConstructor`，字段初值加 `@Builder.Default`。

---

## 2. create 写法

```java
var r = ReadingRecord.builder()
        .userId(userId)
        .bookTitle(dto.getBookTitle())
        .author(dto.getAuthor())
        .coverUrl(dto.getCoverUrl())
        .rating(dto.getRating())
        .notes(dto.getNotes())
        .startDate(dto.getStartDate())
        .endDate(dto.getEndDate())
        .build();
if (dto.getTotalChapters() != null) {
    r.setTotalChapters(dto.getTotalChapters());
}
// progress / status / currentChapter 等同理：仅非 null 时覆盖 @Builder.Default
```

原则：
- 必填 / 总是赋值的字段进 builder 链
- 「null 则保留 Entity 默认」的字段：不放进 builder，build 后再条件 `set`
- 禁止：`builder().status(dto.getStatus())` 在 dto 为 null 时把默认值冲成 null

---

## 3. 改造清单

### 3.1 Entity 加四件套（+ 默认值加 `@Builder.Default`）

| Entity | 模块 | 备注 |
|--------|------|------|
| ReadingRecord | knowledge | 已有 status/progress 等初值 |
| StudyPlan | planning | 已有 progress/status 初值 |
| Category | knowledge | 已有 `@Builder`，补全；sortOrder 初值 |
| Note | knowledge | |
| DiaryEntry | knowledge | |
| StudyRecord | knowledge | |
| Tag | knowledge | |
| TagRel | knowledge | 仅注解，无独立 Service create 可只加注解 |
| TodoTask | planning | |
| BookmarkUrl | resource | |
| FileResource | resource | |
| Notification | system | |
| UserLayout | system | |
| AuditLog | system | |
| User | system | 仅注解；注册路径若存在再改 create |

### 3.2 Service create 改为 builder

凡 `new Xxx()` 后批量 set 的 create / 内部新建路径（含 Category 默认分类等）：

- ReadingRecordServiceImpl / NoteServiceImpl / DiaryEntryServiceImpl / StudyRecordServiceImpl  
- CategoryServiceImpl / TagServiceImpl  
- StudyPlanServiceImpl / TodoTaskServiceImpl  
- BookmarkUrlServiceImpl / FileResourceServiceImpl  
- NotificationServiceImpl / UserLayoutServiceImpl / AuditLogServiceImpl  

StudyPlan 的 `applyDto`：create 可改为 builder + apply 差异字段；update 继续用现有 setter 逻辑。

---

## 4. 文档

| 文档 | 变更 |
|------|------|
| `docs/STYLE_GUIDE.md` | Entity 使用 Lombok 四件套；create 优先 Builder；VO 不强制 Builder |
| `docs/CHANGELOG.md` | Unreleased 记一条 |

---

## 5. 风险

- **MyBatis**：必须保留无参构造（`@NoArgsConstructor`）
- **默认值**：遗漏 `@Builder.Default` 会导致 insert 写入 null（若列无 DB 默认）
- **Jackson**：Entity 一般不直接入参，风险低

## 6. 验收

1. 上表 Entity 均具备四件套；有初值字段均有 `@Builder.Default`
2. 所列 Service create（及等价新建）无大段 `new`+连续 set，改为 builder
3. VO / update 行为与协议不变
4. `mvn -pl ph-boot -am compile` 通过；STYLE_GUIDE / CHANGELOG 已同步
