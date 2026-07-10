# 日记模块 实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 新增日记模块，支持日记 CRUD、按月/周聚合查看、关键词搜索、心情/天气记录

**架构：** 后端新增 ph-diary 子模块（遵循 ph-todo 模式：Entity → DTO → VO → Mapper → Service → Controller），前端新增日记列表/表单页面。后端 API 路径 `/api/diaries`，前端路由 `/diaries`。

**技术栈：** Spring Boot 3 / MyBatis-Plus / Vue 3 / Element Plus / Lucide

**参考模块：** ph-todo（完整 CRUD + 分页查询 + 逻辑删除模式）

---

### 任务 1：数据库表 + 后端模块脚手架

**文件：**
- 修改：`sql/init.sql` — 追加 diary_entry 表
- 创建：`backend/ph-diary/pom.xml`
- 修改：`backend/pom.xml` — 添加 ph-diary 模块
- 修改：`backend/ph-boot/pom.xml` — 添加 ph-diary 依赖

- [ ] **步骤 1：在 sql/init.sql 追加日记表**

```sql
-- ========================================
-- 10. 日记表
-- ========================================
CREATE TABLE IF NOT EXISTS `diary_entry` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`    BIGINT       NOT NULL COMMENT '所属用户',
    `date`       DATE         NOT NULL COMMENT '日记日期',
    `title`      VARCHAR(200) DEFAULT NULL COMMENT '日记标题',
    `content`    TEXT         DEFAULT NULL COMMENT '日记内容（Markdown）',
    `mood`       TINYINT      DEFAULT NULL COMMENT '心情 1-很好 2-好 3-一般 4-不好 5-很差',
    `weather`    VARCHAR(50)  DEFAULT NULL COMMENT '天气',
    `is_deleted` TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id_date` (`user_id`, `date`),
    INDEX `idx_date` (`date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='日记表';
```

- [ ] **步骤 2：创建 ph-diary/pom.xml**

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

    <artifactId>ph-diary</artifactId>
    <name>ph-diary</name>
    <description>日记模块</description>

    <dependencies>
        <dependency>
            <groupId>com.personalhub</groupId>
            <artifactId>ph-common</artifactId>
        </dependency>
    </dependencies>
</project>
```

- [ ] **步骤 3：修改 backend/pom.xml — 添加 ph-diary 模块**

在 `<modules>` 中添加 `<module>ph-diary</module>`（在 ph-boot 之前），在 `<dependencyManagement>` 中添加 ph-diary 依赖：

```xml
<module>ph-diary</module>
```

```xml
<dependency>
    <groupId>com.personalhub</groupId>
    <artifactId>ph-diary</artifactId>
    <version>${project.version}</version>
</dependency>
```

- [ ] **步骤 4：修改 ph-boot/pom.xml — 添加 ph-diary 依赖**

```xml
<dependency>
    <groupId>com.personalhub</groupId>
    <artifactId>ph-diary</artifactId>
</dependency>
```

---

### 任务 2：后端 Entity + DTO + VO

**文件：**
- 创建：`backend/ph-diary/src/main/java/com/personalhub/module/diary/entity/DiaryEntry.java`
- 创建：`backend/ph-diary/src/main/java/com/personalhub/module/diary/dto/DiaryCreateDTO.java`
- 创建：`backend/ph-diary/src/main/java/com/personalhub/module/diary/dto/DiaryQueryDTO.java`
- 创建：`backend/ph-diary/src/main/java/com/personalhub/module/diary/vo/DiaryVO.java`

- [ ] **步骤 1：创建 DiaryEntry Entity**

```java
package com.personalhub.module.diary.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 日记条目实体
 */
@Data
@TableName("diary_entry")
public class DiaryEntry {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属用户ID */
    private Long userId;

    /** 日记日期 */
    private LocalDate date;

    /** 日记标题 */
    private String title;

    /** 日记内容（Markdown） */
    private String content;

    /** 心情 1-很好 2-好 3-一般 4-不好 5-很差 */
    private Integer mood;

    /** 天气 */
    private String weather;

    /** 逻辑删除 0-正常 1-删除 */
    @TableLogic
    private Integer isDeleted;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
```

- [ ] **步骤 2：创建 DiaryCreateDTO**

```java
package com.personalhub.module.diary.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * 创建/编辑日记 DTO
 */
@Data
@Schema(description = "创建/编辑日记请求")
public class DiaryCreateDTO {

    @Schema(description = "日记日期", example = "2026-07-10")
    private LocalDate date;

    @Size(max = 200, message = "标题长度不能超过200")
    @Schema(description = "日记标题", example = "忙碌的一天")
    private String title;

    @Schema(description = "日记内容（Markdown）")
    private String content;

    @Schema(description = "心情 1-很好 2-好 3-一般 4-不好 5-很差", example = "2")
    private Integer mood;

    @Schema(description = "天气", example = "晴")
    private String weather;
}
```

- [ ] **步骤 3：创建 DiaryQueryDTO**

```java
package com.personalhub.module.diary.dto;

import com.personalhub.common.result.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 日记查询 DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "日记查询参数")
public class DiaryQueryDTO extends PageParam {

    @Schema(description = "搜索关键词（标题/内容）")
    private String keyword;

    @Schema(description = "开始日期")
    private LocalDate startDate;

    @Schema(description = "结束日期")
    private LocalDate endDate;

    @Schema(description = "心情筛选")
    private Integer mood;

    @Schema(description = "按年+月查询（格式：2026-07）")
    private String month;
}
```

- [ ] **步骤 4：创建 DiaryVO**

```java
package com.personalhub.module.diary.vo;

import com.personalhub.module.diary.entity.DiaryEntry;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 日记 VO
 */
@Data
@Schema(description = "日记详情")
public class DiaryVO {

    @Schema(description = "日记ID")
    private Long id;

    @Schema(description = "日记日期")
    private LocalDate date;

    @Schema(description = "日记标题")
    private String title;

    @Schema(description = "日记内容（Markdown）")
    private String content;

    @Schema(description = "内容摘要（列表用）")
    private String contentSummary;

    @Schema(description = "心情 1-很好 2-好 3-一般 4-不好 5-很差")
    private Integer mood;

    @Schema(description = "心情中文标签")
    private String moodLabel;

    @Schema(description = "天气")
    private String weather;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    public static DiaryVO from(DiaryEntry entry) {
        DiaryVO vo = new DiaryVO();
        vo.setId(entry.getId());
        vo.setDate(entry.getDate());
        vo.setTitle(entry.getTitle());
        vo.setContent(entry.getContent());
        vo.setMood(entry.getMood());
        vo.setWeather(entry.getWeather());
        vo.setCreatedAt(entry.getCreatedAt());
        vo.setUpdatedAt(entry.getUpdatedAt());

        // 内容摘要（取前100字）
        if (entry.getContent() != null) {
            String plain = entry.getContent().replaceAll("#+\\s*", "").replaceAll("[\\*\\[\\]`>]", "");
            vo.setContentSummary(plain.length() > 100 ? plain.substring(0, 100) + "..." : plain);
        }

        // 心情标签
        if (entry.getMood() != null) {
            vo.setMoodLabel(switch (entry.getMood()) {
                case 1 -> "很好";
                case 2 -> "好";
                case 3 -> "一般";
                case 4 -> "不好";
                case 5 -> "很差";
                default -> "未知";
            });
        }
        return vo;
    }
}
```

---

### 任务 3：后端 Mapper + Service + Controller

**文件：**
- 创建：`backend/ph-diary/src/main/java/com/personalhub/module/diary/mapper/DiaryEntryMapper.java`
- 创建：`backend/ph-diary/src/main/java/com/personalhub/module/diary/service/DiaryEntryService.java`
- 创建：`backend/ph-diary/src/main/java/com/personalhub/module/diary/service/impl/DiaryEntryServiceImpl.java`
- 创建：`backend/ph-diary/src/main/java/com/personalhub/module/diary/controller/DiaryEntryController.java`

- [ ] **步骤 1：创建 DiaryEntryMapper**

```java
package com.personalhub.module.diary.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.personalhub.module.diary.entity.DiaryEntry;
import org.apache.ibatis.annotations.Mapper;

/**
 * 日记 Mapper
 */
@Mapper
public interface DiaryEntryMapper extends BaseMapper<DiaryEntry> {
}
```

- [ ] **步骤 2：创建 DiaryEntryService 接口**

```java
package com.personalhub.module.diary.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.personalhub.module.diary.dto.DiaryCreateDTO;
import com.personalhub.module.diary.dto.DiaryQueryDTO;
import com.personalhub.module.diary.vo.DiaryVO;

import java.time.LocalDate;
import java.util.List;

/**
 * 日记服务接口
 */
public interface DiaryEntryService {

    /**
     * 分页查询日记列表
     */
    IPage<DiaryVO> list(Long userId, DiaryQueryDTO query);

    /**
     * 按月查询日记（月视图聚合）
     */
    List<DiaryVO> listByMonth(Long userId, String month);

    /**
     * 获取日记详情
     */
    DiaryVO getById(Long id, Long userId);

    /**
     * 新建日记
     */
    DiaryVO create(Long userId, DiaryCreateDTO dto);

    /**
     * 编辑日记
     */
    DiaryVO update(Long id, Long userId, DiaryCreateDTO dto);

    /**
     * 删除日记（软删除）
     */
    void delete(Long id, Long userId);
}
```

- [ ] **步骤 3：创建 DiaryEntryServiceImpl**

```java
package com.personalhub.module.diary.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.personalhub.common.exception.NotFoundException;
import com.personalhub.module.diary.dto.DiaryCreateDTO;
import com.personalhub.module.diary.dto.DiaryQueryDTO;
import com.personalhub.module.diary.entity.DiaryEntry;
import com.personalhub.module.diary.mapper.DiaryEntryMapper;
import com.personalhub.module.diary.service.DiaryEntryService;
import com.personalhub.module.diary.vo.DiaryVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;

/**
 * 日记服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DiaryEntryServiceImpl implements DiaryEntryService {

    private final DiaryEntryMapper diaryEntryMapper;

    @Override
    public IPage<DiaryVO> list(Long userId, DiaryQueryDTO query) {
        LambdaQueryWrapper<DiaryEntry> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DiaryEntry::getUserId, userId);

        // 搜索关键词（标题+内容）
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(DiaryEntry::getTitle, query.getKeyword())
                    .or()
                    .like(DiaryEntry::getContent, query.getKeyword()));
        }
        // 日期范围筛选
        if (query.getStartDate() != null) {
            wrapper.ge(DiaryEntry::getDate, query.getStartDate());
        }
        if (query.getEndDate() != null) {
            wrapper.le(DiaryEntry::getDate, query.getEndDate());
        }
        // 心情筛选
        if (query.getMood() != null) {
            wrapper.eq(DiaryEntry::getMood, query.getMood());
        }
        // 按月查询
        if (StringUtils.hasText(query.getMonth()) && query.getMonth().matches("\\d{4}-\\d{2}")) {
            String[] parts = query.getMonth().split("-");
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            wrapper.ge(DiaryEntry::getDate, LocalDate.of(year, month, 1));
            wrapper.le(DiaryEntry::getDate, LocalDate.of(year, month, 1).plusMonths(1).minusDays(1));
        }

        wrapper.orderByDesc(DiaryEntry::getDate);

        Page<DiaryEntry> page = new Page<>(query.getPage(), query.getSize());
        IPage<DiaryEntry> entryPage = diaryEntryMapper.selectPage(page, wrapper);

        return entryPage.convert(DiaryVO::from);
    }

    @Override
    public List<DiaryVO> listByMonth(Long userId, String month) {
        if (!StringUtils.hasText(month) || !month.matches("\\d{4}-\\d{2}")) {
            throw new IllegalArgumentException("月份格式错误，应为 YYYY-MM");
        }
        String[] parts = month.split("-");
        int year = Integer.parseInt(parts[0]);
        int monthInt = Integer.parseInt(parts[1]);

        LambdaQueryWrapper<DiaryEntry> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DiaryEntry::getUserId, userId);
        wrapper.ge(DiaryEntry::getDate, LocalDate.of(year, monthInt, 1));
        wrapper.le(DiaryEntry::getDate, LocalDate.of(year, monthInt, 1).plusMonths(1).minusDays(1));
        wrapper.orderByDesc(DiaryEntry::getDate);

        return diaryEntryMapper.selectList(wrapper).stream().map(DiaryVO::from).toList();
    }

    @Override
    public DiaryVO getById(Long id, Long userId) {
        DiaryEntry entry = diaryEntryMapper.selectById(id);
        if (entry == null || !entry.getUserId().equals(userId)) {
            log.warn("日记不存在或无权访问: id={}, userId={}", id, userId);
            throw new NotFoundException("日记不存在");
        }
        return DiaryVO.from(entry);
    }

    @Override
    @Transactional
    public DiaryVO create(Long userId, DiaryCreateDTO dto) {
        DiaryEntry entry = new DiaryEntry();
        entry.setUserId(userId);
        entry.setDate(dto.getDate() != null ? dto.getDate() : LocalDate.now());
        entry.setTitle(dto.getTitle());
        entry.setContent(dto.getContent());
        entry.setMood(dto.getMood());
        entry.setWeather(dto.getWeather());
        diaryEntryMapper.insert(entry);
        log.info("新建日记: id={}, userId={}, date={}", entry.getId(), userId, entry.getDate());
        return DiaryVO.from(entry);
    }

    @Override
    @Transactional
    public DiaryVO update(Long id, Long userId, DiaryCreateDTO dto) {
        DiaryEntry entry = diaryEntryMapper.selectById(id);
        if (entry == null || !entry.getUserId().equals(userId)) {
            log.warn("编辑日记不存在或无权访问: id={}, userId={}", id, userId);
            throw new NotFoundException("日记不存在");
        }
        if (dto.getDate() != null) entry.setDate(dto.getDate());
        if (dto.getTitle() != null) entry.setTitle(dto.getTitle());
        if (dto.getContent() != null) entry.setContent(dto.getContent());
        if (dto.getMood() != null) entry.setMood(dto.getMood());
        if (dto.getWeather() != null) entry.setWeather(dto.getWeather());
        diaryEntryMapper.updateById(entry);
        log.info("编辑日记: id={}, userId={}", id, userId);
        return DiaryVO.from(entry);
    }

    @Override
    @Transactional
    public void delete(Long id, Long userId) {
        DiaryEntry entry = diaryEntryMapper.selectById(id);
        if (entry == null || !entry.getUserId().equals(userId)) {
            log.warn("删除日记不存在或无权访问: id={}, userId={}", id, userId);
            throw new NotFoundException("日记不存在");
        }
        diaryEntryMapper.deleteById(id);
        log.info("删除日记: id={}, userId={}", id, userId);
    }
}
```

- [ ] **步骤 4：创建 DiaryEntryController**

```java
package com.personalhub.module.diary.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.personalhub.common.result.PageResult;
import com.personalhub.common.result.Result;
import com.personalhub.module.diary.dto.DiaryCreateDTO;
import com.personalhub.module.diary.dto.DiaryQueryDTO;
import com.personalhub.module.diary.service.DiaryEntryService;
import com.personalhub.module.diary.vo.DiaryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 日记控制器
 */
@Tag(name = "日记", description = "日记的增删改查、月视图聚合")
@RestController
@RequestMapping("/api/diaries")
@RequiredArgsConstructor
public class DiaryEntryController {

    private final DiaryEntryService diaryEntryService;

    @Operation(summary = "日记列表", description = "分页查询日记，支持关键词搜索、日期范围筛选、心情筛选、按月查询")
    @GetMapping
    public Result<PageResult<DiaryVO>> list(
            @Parameter(hidden = true) Authentication authentication,
            DiaryQueryDTO query) {
        Long userId = Long.valueOf(authentication.getName());
        IPage<DiaryVO> page = diaryEntryService.list(userId, query);
        return Result.success(PageResult.of(page));
    }

    @Operation(summary = "月视图", description = "按月查询日记列表，返回当月所有日记")
    @GetMapping("/month")
    public Result<List<DiaryVO>> listByMonth(
            @Parameter(hidden = true) Authentication authentication,
            @RequestParam String month) {
        Long userId = Long.valueOf(authentication.getName());
        return Result.success(diaryEntryService.listByMonth(userId, month));
    }

    @Operation(summary = "日记详情")
    @GetMapping("/{id}")
    public Result<DiaryVO> getById(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long id) {
        Long userId = Long.valueOf(authentication.getName());
        return Result.success(diaryEntryService.getById(id, userId));
    }

    @Operation(summary = "新建日记")
    @PostMapping
    public Result<DiaryVO> create(
            @Parameter(hidden = true) Authentication authentication,
            @Valid @RequestBody DiaryCreateDTO dto) {
        Long userId = Long.valueOf(authentication.getName());
        return Result.success(diaryEntryService.create(userId, dto));
    }

    @Operation(summary = "编辑日记")
    @PutMapping("/{id}")
    public Result<DiaryVO> update(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody DiaryCreateDTO dto) {
        Long userId = Long.valueOf(authentication.getName());
        return Result.success(diaryEntryService.update(id, userId, dto));
    }

    @Operation(summary = "删除日记")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long id) {
        Long userId = Long.valueOf(authentication.getName());
        diaryEntryService.delete(id, userId);
        return Result.success();
    }
}
```

---

### 任务 4：前端类型 + API

**文件：**
- 创建：`frontend/src/types/diary.ts`
- 创建：`frontend/src/api/diaryApi.ts`

- [ ] **步骤 1：创建 diary.ts 类型定义**

```typescript
/** 日记 */
export interface DiaryVO {
  id: number
  date: string
  title: string
  content: string
  contentSummary: string
  mood: number
  moodLabel: string
  weather: string
  createdAt: string
  updatedAt: string
}

/** 创建/编辑日记 */
export interface DiaryCreateDTO {
  date?: string
  title?: string
  content?: string
  mood?: number
  weather?: string
}

/** 查询参数 */
export interface DiaryQuery {
  page?: number
  size?: number
  keyword?: string
  startDate?: string
  endDate?: string
  mood?: number
  month?: string
}
```

- [ ] **步骤 2：创建 diaryApi.ts**

```typescript
import request from './request'
import type { Result, PageResult } from '@/types/common'
import type { DiaryVO, DiaryCreateDTO, DiaryQuery } from '@/types/diary'

export function getDiaryList(params: DiaryQuery) {
  return request.get<Result<PageResult<DiaryVO>>>('/diaries', { params })
}

export function getDiaryByMonth(month: string) {
  return request.get<Result<DiaryVO[]>>('/diaries/month', { params: { month } })
}

export function getDiaryById(id: number) {
  return request.get<Result<DiaryVO>>(`/diaries/${id}`)
}

export function createDiary(data: DiaryCreateDTO) {
  return request.post<Result<DiaryVO>>('/diaries', data)
}

export function updateDiary(id: number, data: DiaryCreateDTO) {
  return request.put<Result<DiaryVO>>(`/diaries/${id}`, data)
}

export function deleteDiary(id: number) {
  return request.delete<Result<void>>(`/diaries/${id}`)
}
```

---

### 任务 5：前端日记列表页

**文件：**
- 创建：`frontend/src/views/diary/DiaryList.vue`

- [ ] **步骤 1：创建 DiaryList.vue**

```vue
<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { getDiaryList, deleteDiary } from '@/api/diaryApi'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Pencil, Trash2, Calendar, Sun, Cloud, CloudRain, Smile, Frown, Meh } from 'lucide-vue-next'
import type { DiaryVO, DiaryQuery } from '@/types/diary'

const router = useRouter()
const list = ref<DiaryVO[]>([])
const total = ref(0)
const loading = ref(false)
const query = ref<DiaryQuery>({ page: 1, size: 20, keyword: '' })

const currentMonth = computed(() => {
  const now = new Date()
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
})

onMounted(() => fetchList())

async function fetchList() {
  loading.value = true
  try {
    const res = await getDiaryList(query.value)
    list.value = res.data.data.records
    total.value = res.data.data.total
  } finally {
    loading.value = false
  }
}

function onSearch() { query.value.page = 1; fetchList() }
function onPageChange(page: number) { query.value.page = page; fetchList() }
function goCreate() { router.push('/diaries/new') }
function goEdit(id: number) { router.push(`/diaries/${id}/edit`) }

async function handleDelete(id: number) {
  await ElMessageBox.confirm('确定删除这篇日记？', '提示', { type: 'warning' })
  await deleteDiary(id)
  ElMessage.success('已删除')
  fetchList()
}

function getMoodIcon(mood: number) {
  if (mood <= 2) return Smile
  if (mood === 3) return Meh
  return Frown
}

function getMoodColor(mood: number) {
  if (mood === 1) return 'var(--success)'
  if (mood === 2) return 'var(--accent)'
  if (mood === 3) return 'var(--text-secondary)'
  return 'var(--danger)'
}

function getWeatherIcon(weather: string) {
  if (!weather) return null
  const icons: Record<string, any> = { '晴': Sun, '多云': Cloud, '阴': Cloud, '雨': CloudRain, '雪': CloudRain }
  return icons[weather] || null
}

const moodOptions = [
  { value: undefined, label: '全部心情' },
  { value: 1, label: '很好' },
  { value: 2, label: '好' },
  { value: 3, label: '一般' },
  { value: 4, label: '不好' },
  { value: 5, label: '很差' }
]
</script>

<template>
  <div>
    <div class="page-header">
      <h2>日记</h2>
      <p>共 {{ total }} 篇</p>
    </div>

    <div class="toolbar">
      <div class="toolbar-left">
        <el-input v-model="query.keyword" placeholder="搜索日记..." style="width:200px" clearable @clear="onSearch" @keyup.enter="onSearch">
          <template #prefix><Search :size="14" style="color: var(--text-tertiary)" /></template>
        </el-input>
        <el-select v-model="query.mood" placeholder="心情" style="width:120px" clearable @change="onFilterChange">
          <el-option v-for="item in moodOptions" :key="item.label" :value="item.value" :label="item.label" />
        </el-select>
        <el-date-picker
          v-model="query.month"
          type="month"
          value-format="YYYY-MM"
          placeholder="按月筛选"
          style="width:140px"
          clearable
          @change="onFilterChange"
        />
      </div>
      <el-button type="primary" @click="goCreate">
        <Plus :size="14" /> 写日记
      </el-button>
    </div>

    <div v-if="loading" class="loading-skeleton">
      <div v-for="i in 5" :key="i" class="skeleton-diary" />
    </div>

    <div v-else-if="list.length === 0" class="empty-state">
      <div class="empty-state__icon"><Calendar :size="48" /></div>
      <div class="empty-state__text">还没有日记，开始记录吧</div>
      <el-button type="primary" @click="goCreate">写日记</el-button>
    </div>

    <div v-else class="diary-list">
      <div v-for="entry in list" :key="entry.id" class="diary-item" @click="goEdit(entry.id)">
        <div class="diary-date">
          <div class="diary-date-day">{{ entry.date.slice(8) }}</div>
          <div class="diary-date-month">{{ entry.date.slice(0, 7) }}</div>
        </div>
        <div class="diary-body">
          <div class="diary-header">
            <span class="diary-title">{{ entry.title || '无题' }}</span>
            <div class="diary-meta">
              <span v-if="entry.mood" class="diary-mood" :style="{ color: getMoodColor(entry.mood) }">
                <component :is="getMoodIcon(entry.mood)" :size="14" />
                {{ entry.moodLabel }}
              </span>
              <span v-if="entry.weather" class="diary-weather">
                <component :is="getWeatherIcon(entry.weather)" v-if="getWeatherIcon(entry.weather)" :size="14" />
                {{ entry.weather }}
              </span>
            </div>
          </div>
          <div v-if="entry.contentSummary" class="diary-summary">{{ entry.contentSummary }}</div>
        </div>
        <div class="diary-actions" @click.stop>
          <button class="icon-btn" @click.stop="goEdit(entry.id)"><Pencil :size="14" /></button>
          <button class="icon-btn icon-btn--danger" @click.stop="handleDelete(entry.id)"><Trash2 :size="14" /></button>
        </div>
      </div>
    </div>

    <el-pagination
      v-if="total > query.size"
      v-model:current-page="query.page"
      :total="total" :page-size="query.size"
      layout="total, prev, pager, next"
      style="margin-top: var(--sp-6); justify-content: flex-end"
      @current-change="onPageChange"
    />
  </div>
</template>

<style scoped>
.loading-skeleton { display: flex; flex-direction: column; gap: var(--sp-3); }
.skeleton-diary { height: 80px; border-radius: var(--radius-md); background: var(--bg-hover); animation: pulse 1.5s ease-in-out infinite; }

.diary-list { display: flex; flex-direction: column; gap: var(--sp-2); }
.diary-item {
  display: flex; align-items: flex-start; gap: var(--sp-4);
  padding: var(--sp-4) var(--sp-5); background: var(--bg-card);
  border: 1px solid var(--border-color); border-radius: var(--radius-md);
  cursor: pointer; transition: all var(--transition);
}
.diary-item:hover { box-shadow: var(--shadow-sm); border-color: var(--accent-border); }

.diary-date { text-align: center; flex-shrink: 0; width: 48px; padding-top: 2px; }
.diary-date-day { font-size: 24px; font-weight: 700; line-height: 1; color: var(--text-primary); }
.diary-date-month { font-size: var(--text-xs); color: var(--text-tertiary); margin-top: 2px; }

.diary-body { flex: 1; min-width: 0; }
.diary-header { display: flex; align-items: center; gap: var(--sp-3); margin-bottom: 4px; }
.diary-title { font-size: var(--text-sm); font-weight: 500; }
.diary-meta { display: flex; align-items: center; gap: var(--sp-2); margin-left: auto; font-size: var(--text-xs); }
.diary-mood, .diary-weather { display: flex; align-items: center; gap: 2px; white-space: nowrap; }
.diary-summary {
  font-size: var(--text-sm); color: var(--text-secondary);
  line-height: var(--leading-normal); overflow: hidden;
  display: -webkit-box; -webkit-line-clamp: 1; -webkit-box-orient: vertical;
}

.diary-actions { display: flex; gap: var(--sp-1); flex-shrink: 0; opacity: 0; transition: opacity var(--transition); align-items: center; }
.diary-item:hover .diary-actions { opacity: 1; }

.icon-btn { background: none; border: none; cursor: pointer; padding: 6px; border-radius: var(--radius-sm); color: var(--text-tertiary); transition: all var(--transition); display: flex; align-items: center; }
.icon-btn:hover { color: var(--accent); background: var(--accent-light); }
.icon-btn--danger:hover { color: var(--danger); background: var(--danger-light); }
</style>
```

---

### 任务 6：前端日记表单页

**文件：**
- 创建：`frontend/src/views/diary/DiaryForm.vue`

- [ ] **步骤 1：创建 DiaryForm.vue**

```vue
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { createDiary, updateDiary, getDiaryById } from '@/api/diaryApi'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Smile, Meh, Frown } from 'lucide-vue-next'

const route = useRoute()
const router = useRouter()
const isEdit = !!route.params.id

const form = ref({ date: '', title: '', content: '', mood: 2, weather: '' })
const saving = ref(false)

onMounted(async () => {
  if (isEdit) {
    const res = await getDiaryById(Number(route.params.id))
    const r = res.data.data
    form.value.date = r.date
    form.value.title = r.title || ''
    form.value.content = r.content || ''
    form.value.mood = r.mood || 2
    form.value.weather = r.weather || ''
  } else {
    // 默认为今天
    const now = new Date()
    form.value.date = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`
  }
})

async function handleSave() {
  saving.value = true
  try {
    if (isEdit) {
      await updateDiary(Number(route.params.id), form.value)
      ElMessage.success('已更新')
    } else {
      await createDiary(form.value)
      ElMessage.success('已创建')
    }
    router.push('/diaries')
  } finally { saving.value = false }
}

const moodOptions = [
  { value: 1, label: '很好', icon: Smile, color: 'var(--success)' },
  { value: 2, label: '好', icon: Smile, color: 'var(--accent)' },
  { value: 3, label: '一般', icon: Meh, color: 'var(--text-secondary)' },
  { value: 4, label: '不好', icon: Frown, color: 'var(--warning)' },
  { value: 5, label: '很差', icon: Frown, color: 'var(--danger)' }
]

const weatherOptions = ['晴', '多云', '阴', '小雨', '大雨', '雷阵雨', '雪', '雾']
</script>

<template>
  <div class="form-page">
    <div class="form-topbar">
      <button class="icon-btn" @click="router.push('/diaries')">
        <ArrowLeft :size="16" /> 返回
      </button>
      <h2>{{ isEdit ? '编辑日记' : '写日记' }}</h2>
    </div>

    <div class="form-card">
      <el-form label-position="top" style="max-width: 640px">
        <el-form-item label="日期">
          <el-date-picker v-model="form.date" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width:100%" />
        </el-form-item>

        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="日记标题（可选）" maxlength="200" show-word-limit />
        </el-form-item>

        <el-form-item label="心情">
          <el-radio-group v-model="form.mood">
            <el-radio v-for="item in moodOptions" :key="item.value" :value="item.value" class="mood-radio">
              <component :is="item.icon" :size="16" :color="item.color" />
              <span :style="{ color: item.color }">{{ item.label }}</span>
            </el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="天气">
          <el-select v-model="form.weather" placeholder="选择天气" clearable style="width:100%">
            <el-option v-for="w in weatherOptions" :key="w" :value="w" :label="w" />
          </el-select>
        </el-form-item>

        <el-form-item label="内容">
          <el-input v-model="form.content" type="textarea" :rows="12" placeholder="开始写吧...（支持 Markdown 格式）" />
          <div class="form-hint">支持 Markdown 格式：标题、列表、加粗、代码等</div>
        </el-form-item>

        <el-form-item>
          <el-button @click="router.push('/diaries')">取消</el-button>
          <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
.form-page { max-width: 700px; }
.form-topbar { display: flex; align-items: center; gap: var(--sp-4); margin-bottom: var(--sp-6); }
.form-topbar h2 { font-size: var(--text-xl); font-weight: 600; }
.icon-btn {
  display: flex; align-items: center; gap: var(--sp-1);
  background: none; border: none; color: var(--text-secondary); font-size: var(--text-sm);
  cursor: pointer; padding: 4px 8px; border-radius: var(--radius-sm); transition: all var(--transition);
}
.icon-btn:hover { background: var(--bg-hover); color: var(--text-primary); }
.form-card {
  background: var(--bg-card); border: 1px solid var(--border-color);
  border-radius: var(--radius-lg); padding: var(--sp-6);
}
.form-hint { font-size: var(--text-xs); color: var(--text-tertiary); margin-top: 4px; }
.mood-radio { display: flex; align-items: center; gap: 4px; }
.mood-radio :deep(.el-radio__label) { display: flex; align-items: center; gap: 4px; }
</style>
```

---

### 任务 7：前端路由 + 导航

**文件：**
- 修改：`frontend/src/router/index.ts` — 添加日记路由
- 修改：`frontend/src/components/AppLayout.vue` — 添加日记导航

- [ ] **步骤 1：在 router/index.ts 添加日记路由**

在 `children` 数组中添加：

```typescript
{ path: 'diaries', name: 'DiaryList', component: () => import('@/views/diary/DiaryList.vue') },
{ path: 'diaries/new', name: 'DiaryCreate', component: () => import('@/views/diary/DiaryForm.vue') },
{ path: 'diaries/:id/edit', name: 'DiaryEdit', component: () => import('@/views/diary/DiaryForm.vue') },
```

- [ ] **步骤 2：在 AppLayout.vue 添加导航**

在 `<router-link to="/todos"` 和文件导航之间添加：

```html
<router-link to="/diaries" class="nav-item" :class="{ active: $route.path.startsWith('/diaries') }">
  <BookOpen :size="18" />
  <span>日记</span>
</router-link>
```

同时需要在 script 的 import 中确认 `BookOpen` 已导入（已有），如果没有则添加。注意：日记应该用不同的图标，比如 `Bookmark` 或 `PenLine` 或 `Notebook`。从现有的 lucide import 中，`BookOpen` 已被学习记录使用，所以日记应该用另一个图标。让我用 `NotebookText` 或 `PenSquare`。实际上，检查一下 lucide-vue-next 有哪些图标可用... 我会用 `PenLine` 或者 `StickyNote`。使用 `StickyNote` 比较合适。

实际上，让我直接用 `BookOpen` 换一个不同的。学习记录用的是 BookOpen，日记我可以用 `FileText` 但笔记已经用了。用 `BookMarked` 或 `Feather` 或 `PenLine`。

让我用 `PenLine`。

---

### 任务 8：编译验证

- [ ] **步骤 1：后端编译**

```bash
cd backend && mvn compile -q
```

预期：BUILD SUCCESS

- [ ] **步骤 2：前端编译**

```bash
cd frontend && npm run build
```

预期：构建成功

---

### 任务 9：更新文档

- [ ] **步骤 1：更新 ROADMAP.md — 标记日记为进行中/已完成**

- [ ] **步骤 2：更新 DATABASE.md — 添加 diary_entry 表**

- [ ] **步骤 3：更新 API.md — 添加日记接口**

- [ ] **步骤 4：更新 CHANGELOG.md — 添加日记模块记录**
