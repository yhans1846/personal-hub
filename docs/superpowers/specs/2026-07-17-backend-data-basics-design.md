# 后端基础数据优化 — 设计规格

日期：2026-07-17  
状态：已确认

## 目标

在不引入 MapStruct / BeanUtils 的前提下，收口后端「魔法默认值、状态码文案、归属校验」三类重复模式，降低 Service / VO 样板代码，保持 API 与 DB 协议不变。

## 非目标

- 不引入 MapStruct、ModelMapper、Hutool BeanUtil 等对象拷贝库
- 不改动 VO 静态 `from(Entity)` 的整体结构（仅将 label 的 switch 改为枚举）
- 不改前端协议（status / priority / mood 仍为 Integer）
- 不强制改造 Dashboard 聚合 VO（Stats / Search / Trend 等手工组装）

## 决策摘要

| 项 | 决策 |
|----|------|
| 对象映射 | 保持手写 `set` + `VO.from` |
| 状态文案 | 模块内枚举 + `labelOf(Integer)` |
| 默认值 | Entity 字段初始化；Service create 去掉 `!= null ? x : 0` |
| 归属校验 | `ph-common` 提供 `EntityGuard.requireOwned` |

---

## 1. 状态枚举

DB 与 API 继续使用 `Integer`。枚举仅用于码表与中文标签，避免 VO / Service 内嵌 `switch`。

### 1.1 枚举清单

| 枚举 | 模块包 | 码 → 文案 |
|------|--------|-----------|
| `ReadingStatus` | `ph-knowledge/.../enums` | 0 未读 / 1 在读 / 2 读完 |
| `DiaryMood` | `ph-knowledge/.../enums` | 1 很好 / 2 好 / 3 一般 / 4 不好 / 5 很差 |
| `StudyPlanStatus` | `ph-planning/.../enums` | 0 未开始 / 1 学习中 / 2 已完成 / 3 已暂停 |
| `TodoPriority` | `ph-planning/.../enums` | 1 高 / 2 中 / 3 低 |

### 1.2 约定 API

每个枚举提供：

- `getCode()` / `getLabel()`
- `static String labelOf(Integer code)`：`null` → `null`；未匹配 → `"未知"`
- （可选）`static Integer codeOrDefault(Integer code, XxxEnum def)`：create 兜底时使用

### 1.3 VO 改造

`ReadingVO` / `DiaryVO` / `StudyPlanVO` / `TodoVO` 的 `from` 中，将 `switch` 替换为对应 `labelOf`。  
`FileVO.typeLabel`（按扩展名）不在本规格枚举范围内，保持现有工具方法即可。

---

## 2. 默认值收口

### 2.1 Entity 字段初始化

对 create 时常见「DTO 为 null 则写 0」的字段，在 Entity 上赋初值，例如：

- `ReadingRecord`：`totalChapters` / `currentChapter` / `progress` / `status` → `0`
- `StudyPlan`：`progress` / `status` → `0`
- `Category`：`sortOrder` → `0`
- 其他同类字段按现有 Service create 逻辑对齐

### 2.2 Service

- create：映射 DTO 时，若字段已有 Entity 默认值，删除 `dto.getX() != null ? dto.getX() : 0`
- update：保持「null 不覆盖」语义（现有 `if (dto.getX() != null)` 逻辑保留）
- 导出等展示层的 `vo.getX() != null ? vo.getX() : 0` 可保留（防脏数据），或依赖 Entity 默认后简化

---

## 3. EntityGuard

### 3.1 位置

`com.personalhub.common.util.EntityGuard`（`ph-common`）

### 3.2 API

```java
public final class EntityGuard {
    private EntityGuard() {}

    /**
     * entity 为 null，或 userIdGetter 结果与 userId 不相等时，抛出 NotFoundException。
     *
     * @param entity       查询结果
     * @param userId       当前用户 ID
     * @param userIdGetter 从实体取归属用户
     * @param message      不存在/无权限时的提示（对外统一「不存在」语义）
     * @return 非空且归属正确的实体
     */
    public static <T> T requireOwned(
            T entity,
            Long userId,
            Function<T, Long> userIdGetter,
            String message);
}
```

不强制 Entity 实现 `Owned` 接口，避免大范围改实体签名。

### 3.3 替换范围

凡 `selectById` 后出现 `entity == null || !entity.getUserId().equals(userId)` 的 ServiceImpl，一律改为 `EntityGuard.requireOwned(...)`。覆盖模块至少包括：

- knowledge：Note / Diary / Reading / StudyRecord / Category / Tag（若适用）
- planning：Todo / StudyPlan
- resource：Bookmark / File（若适用）
- system：Notification / Layout（若适用且有 userId）

`UserServiceImpl` 按自身 ID 查用户，可不强行套用（无「归属他人」语义时保持原样）。

---

## 4. 文档与测试

### 4.1 文档

| 文档 | 变更 |
|------|------|
| `docs/STYLE_GUIDE.md` | 补充：状态码用模块枚举 + `labelOf`；归属校验用 `EntityGuard`；禁止 VO 内嵌业务 switch |
| `docs/CHANGELOG.md` | Unreleased 记一条基础数据优化 |
| `docs/TECH_STACK.md` / `README.md` | 不新增映射库；无需改技术栈表 |

### 4.2 测试

- `EntityGuard` 单测：null、userId 不匹配、匹配成功三条路径
- 枚举 `labelOf`：null / 已知码 / 未知码
- 现有模块测试若依赖具体文案，同步断言枚举 label

---

## 5. 风险与兼容

- **协议兼容**：对外仍为 Integer + `*Label` 字符串，前端无感
- **MyBatis 插入**：Entity 字段默认值在 Java 侧生效；若 DB 列 NOT NULL 无默认且未赋值，行为与现「显式 set 0」一致
- **update null 语义**：不改变；仅收口 create 默认值

---

## 6. 验收标准

1. 上述四个枚举落地，相关 VO 无 status/priority/mood 的内联 switch
2. 目标 Entity 有合理字段初值；对应 Service create 无散落 `?: 0`（业务特例除外）
3. 带 userId 归属的 Service 读写删除路径统一走 `EntityGuard`
4. STYLE_GUIDE + CHANGELOG 已同步
5. `EntityGuard` 与枚举单测通过；相关模块可编译
