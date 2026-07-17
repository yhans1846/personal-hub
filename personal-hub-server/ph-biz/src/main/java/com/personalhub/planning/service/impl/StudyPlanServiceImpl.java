package com.personalhub.planning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.personalhub.common.util.EntityGuard;
import com.personalhub.knowledge.entity.TagRel;
import com.personalhub.knowledge.mapper.TagRelMapper;
import com.personalhub.knowledge.service.TagService;
import com.personalhub.knowledge.vo.TagVO;
import com.personalhub.planning.dto.StudyPlanCreateDTO;
import com.personalhub.planning.dto.StudyPlanQueryDTO;
import com.personalhub.planning.entity.StudyPlan;
import com.personalhub.planning.mapper.StudyPlanMapper;
import com.personalhub.planning.service.StudyPlanService;
import com.personalhub.planning.vo.StudyPlanStatsVO;
import com.personalhub.planning.vo.StudyPlanVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 学习计划服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StudyPlanServiceImpl implements StudyPlanService {

    private static final String ENTITY_TYPE = "study_plan";

    private final StudyPlanMapper studyPlanMapper;
    private final TagService tagService;
    private final TagRelMapper tagRelMapper;

    @Override
    public IPage<StudyPlanVO> list(Long userId, StudyPlanQueryDTO query) {
        LambdaQueryWrapper<StudyPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudyPlan::getUserId, userId);
        if (StringUtils.hasText(query.getKeyword())) {
            String kw = query.getKeyword();
            wrapper.and(w -> w.like(StudyPlan::getName, kw)
                    .or().like(StudyPlan::getSource, kw)
                    .or().like(StudyPlan::getAuthor, kw)
                    .or().like(StudyPlan::getRemark, kw));
        }
        if (query.getStatus() != null) {
            wrapper.eq(StudyPlan::getStatus, query.getStatus());
        }
        if (query.getTagId() != null) {
            List<Long> ids = listEntityIdsByTag(query.getTagId());
            if (ids.isEmpty()) {
                Page<StudyPlanVO> empty = new Page<>(query.getPage(), query.getSize(), 0);
                empty.setRecords(Collections.emptyList());
                return empty;
            }
            wrapper.in(StudyPlan::getId, ids);
        }
        applySort(wrapper, query.getSortBy(), query.getSortDir());

        Page<StudyPlan> page = new Page<>(query.getPage(), query.getSize());
        IPage<StudyPlan> planPage = studyPlanMapper.selectPage(page, wrapper);

        List<Long> planIds = planPage.getRecords().stream().map(StudyPlan::getId).toList();
        Map<Long, List<TagVO>> tagsMap = tagService.getTagsMap(ENTITY_TYPE, planIds);

        return planPage.convert(plan -> {
            StudyPlanVO vo = StudyPlanVO.from(plan);
            vo.setTags(tagsMap.getOrDefault(plan.getId(), List.of()));
            return vo;
        });
    }

    /**
     * 应用排序
     *
     * @param wrapper 查询条件
     * @param sortBy  排序字段
     * @param sortDir 方向
     */
    private void applySort(LambdaQueryWrapper<StudyPlan> wrapper, String sortBy, String sortDir) {
        boolean asc = "asc".equalsIgnoreCase(sortDir);
        String field = StringUtils.hasText(sortBy) ? sortBy : "updatedAt";
        switch (field) {
            case "createdAt" -> {
                if (asc) wrapper.orderByAsc(StudyPlan::getCreatedAt);
                else wrapper.orderByDesc(StudyPlan::getCreatedAt);
            }
            case "startDate" -> {
                if (asc) wrapper.orderByAsc(StudyPlan::getStartDate);
                else wrapper.orderByDesc(StudyPlan::getStartDate);
            }
            case "endDate" -> {
                if (asc) wrapper.orderByAsc(StudyPlan::getEndDate);
                else wrapper.orderByDesc(StudyPlan::getEndDate);
            }
            case "name" -> {
                if (asc) wrapper.orderByAsc(StudyPlan::getName);
                else wrapper.orderByDesc(StudyPlan::getName);
            }
            default -> {
                if (asc) wrapper.orderByAsc(StudyPlan::getUpdatedAt);
                else wrapper.orderByDesc(StudyPlan::getUpdatedAt);
            }
        }
    }

    /**
     * 按标签查出关联的学习计划 ID
     *
     * @param tagId 标签 ID
     * @return 实体 ID 列表
     */
    private List<Long> listEntityIdsByTag(Long tagId) {
        return tagRelMapper.selectList(new LambdaQueryWrapper<TagRel>()
                        .eq(TagRel::getTagId, tagId)
                        .eq(TagRel::getEntityType, ENTITY_TYPE))
                .stream()
                .map(TagRel::getEntityId)
                .toList();
    }

    @Override
    public StudyPlanStatsVO stats(Long userId) {
        List<StudyPlan> plans = studyPlanMapper.selectList(
                new LambdaQueryWrapper<StudyPlan>().eq(StudyPlan::getUserId, userId));
        StudyPlanStatsVO vo = new StudyPlanStatsVO();
        vo.setTotal(plans.size());
        vo.setPending(plans.stream().filter(p -> p.getStatus() != null && p.getStatus() == 0).count());
        vo.setLearning(plans.stream().filter(p -> p.getStatus() != null && p.getStatus() == 1).count());
        vo.setDone(plans.stream().filter(p -> p.getStatus() != null && p.getStatus() == 2).count());
        vo.setPaused(plans.stream().filter(p -> p.getStatus() != null && p.getStatus() == 3).count());
        return vo;
    }

    @Override
    public StudyPlanVO getById(Long id, Long userId) {
        StudyPlan plan = EntityGuard.requireOwned(
                studyPlanMapper.selectById(id), userId, StudyPlan::getUserId, "学习计划不存在");
        StudyPlanVO vo = StudyPlanVO.from(plan);
        vo.setTags(tagService.getTags(ENTITY_TYPE, id));
        return vo;
    }

    @Override
    @Transactional
    public StudyPlanVO create(Long userId, StudyPlanCreateDTO dto) {
        var plan = StudyPlan.builder()
                .userId(userId)
                .name(dto.getName())
                .source(dto.getSource())
                .author(dto.getAuthor())
                .url(dto.getUrl())
                .remark(dto.getRemark())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .build();
        if (dto.getProgress() != null) {
            plan.setProgress(dto.getProgress());
        }
        if (dto.getStatus() != null) {
            plan.setStatus(dto.getStatus());
        }
        studyPlanMapper.insert(plan);
        if (dto.getTagIds() != null && !dto.getTagIds().isEmpty()) {
            tagService.bindTags(plan.getId(), ENTITY_TYPE, dto.getTagIds());
        }
        log.info("新建学习计划: id={}, userId={}, name={}", plan.getId(), userId, dto.getName());
        StudyPlanVO vo = StudyPlanVO.from(plan);
        vo.setTags(tagService.getTags(ENTITY_TYPE, plan.getId()));
        return vo;
    }

    @Override
    @Transactional
    public StudyPlanVO update(Long id, Long userId, StudyPlanCreateDTO dto) {
        StudyPlan plan = EntityGuard.requireOwned(
                studyPlanMapper.selectById(id), userId, StudyPlan::getUserId, "学习计划不存在");
        applyDto(plan, dto);
        studyPlanMapper.updateById(plan);
        tagService.bindTags(id, ENTITY_TYPE, dto.getTagIds());
        log.info("编辑学习计划: id={}, userId={}", id, userId);
        StudyPlanVO vo = StudyPlanVO.from(plan);
        vo.setTags(tagService.getTags(ENTITY_TYPE, id));
        return vo;
    }

    @Override
    @Transactional
    public void delete(Long id, Long userId) {
        StudyPlan plan = EntityGuard.requireOwned(
                studyPlanMapper.selectById(id), userId, StudyPlan::getUserId, "学习计划不存在");
        tagService.unbindAll(ENTITY_TYPE, id);
        studyPlanMapper.deleteById(id);
        log.info("删除学习计划: id={}, userId={}", id, userId);
    }

    @Override
    public byte[] exportXlsx(Long userId, StudyPlanQueryDTO query, String scope) {
        List<StudyPlanVO> rows = listForExport(userId, query, scope);
        DateTimeFormatter dateFmt = DateTimeFormatter.ISO_LOCAL_DATE;
        DateTimeFormatter dtFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String[] headers = {"名称", "分类", "状态", "进度", "开始", "结束", "来源", "作者", "URL", "备注", "更新时间"};
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("学习计划");
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }
            int r = 1;
            for (StudyPlanVO vo : rows) {
                String tags = vo.getTags() == null ? "" : vo.getTags().stream()
                        .map(TagVO::getName)
                        .collect(Collectors.joining("、"));
                Row row = sheet.createRow(r++);
                setCell(row, 0, vo.getName());
                setCell(row, 1, tags);
                setCell(row, 2, vo.getStatusLabel());
                Cell progressCell = row.createCell(3);
                progressCell.setCellValue(vo.getProgress() != null ? vo.getProgress() : 0);
                setCell(row, 4, vo.getStartDate() != null ? vo.getStartDate().format(dateFmt) : "");
                setCell(row, 5, vo.getEndDate() != null ? vo.getEndDate().format(dateFmt) : "");
                setCell(row, 6, vo.getSource());
                setCell(row, 7, vo.getAuthor());
                setCell(row, 8, vo.getUrl());
                setCell(row, 9, vo.getRemark());
                setCell(row, 10, vo.getUpdatedAt() != null ? vo.getUpdatedAt().format(dtFmt) : "");
            }
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(out);
            log.info("导出学习计划 XLSX: userId={}, scope={}, rows={}", userId, scope, rows.size());
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("导出学习计划失败", e);
        }
    }

    private static void setCell(Row row, int col, String value) {
        row.createCell(col).setCellValue(value != null ? value : "");
    }

    /**
     * 导出用列表（不分页）
     */
    private List<StudyPlanVO> listForExport(Long userId, StudyPlanQueryDTO query, String scope) {
        LambdaQueryWrapper<StudyPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudyPlan::getUserId, userId);
        boolean filtered = !"all".equalsIgnoreCase(scope);
        if (filtered && query != null) {
            if (StringUtils.hasText(query.getKeyword())) {
                String kw = query.getKeyword();
                wrapper.and(w -> w.like(StudyPlan::getName, kw)
                        .or().like(StudyPlan::getSource, kw)
                        .or().like(StudyPlan::getAuthor, kw)
                        .or().like(StudyPlan::getRemark, kw));
            }
            if (query.getStatus() != null) {
                wrapper.eq(StudyPlan::getStatus, query.getStatus());
            }
            if (query.getTagId() != null) {
                List<Long> ids = listEntityIdsByTag(query.getTagId());
                if (ids.isEmpty()) {
                    return Collections.emptyList();
                }
                wrapper.in(StudyPlan::getId, ids);
            }
            applySort(wrapper, query.getSortBy(), query.getSortDir());
        } else {
            wrapper.orderByDesc(StudyPlan::getUpdatedAt);
        }
        List<StudyPlan> plans = studyPlanMapper.selectList(wrapper);
        if (plans.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> planIds = plans.stream().map(StudyPlan::getId).toList();
        Map<Long, List<TagVO>> tagsMap = tagService.getTagsMap(ENTITY_TYPE, planIds);
        return plans.stream().map(plan -> {
            StudyPlanVO vo = StudyPlanVO.from(plan);
            vo.setTags(tagsMap.getOrDefault(plan.getId(), List.of()));
            return vo;
        }).toList();
    }

    /**
     * 将 DTO 字段写入实体
     *
     * @param plan 实体
     * @param dto  请求
     */
    private void applyDto(StudyPlan plan, StudyPlanCreateDTO dto) {
        plan.setName(dto.getName());
        plan.setSource(dto.getSource());
        plan.setAuthor(dto.getAuthor());
        plan.setUrl(dto.getUrl());
        plan.setRemark(dto.getRemark());
        if (dto.getProgress() != null) {
            plan.setProgress(dto.getProgress());
        }
        plan.setStartDate(dto.getStartDate());
        plan.setEndDate(dto.getEndDate());
        if (dto.getStatus() != null) {
            plan.setStatus(dto.getStatus());
        }
    }
}
