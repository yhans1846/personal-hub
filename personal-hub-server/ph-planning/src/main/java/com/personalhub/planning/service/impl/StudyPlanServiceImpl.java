package com.personalhub.planning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.personalhub.common.exception.NotFoundException;
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

import java.util.Collections;
import java.util.List;
import java.util.Map;

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
        StudyPlan plan = studyPlanMapper.selectById(id);
        if (plan == null || !plan.getUserId().equals(userId)) {
            throw new NotFoundException("学习计划不存在");
        }
        StudyPlanVO vo = StudyPlanVO.from(plan);
        vo.setTags(tagService.getTags(ENTITY_TYPE, id));
        return vo;
    }

    @Override
    @Transactional
    public StudyPlanVO create(Long userId, StudyPlanCreateDTO dto) {
        StudyPlan plan = new StudyPlan();
        plan.setUserId(userId);
        applyDto(plan, dto);
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
        StudyPlan plan = studyPlanMapper.selectById(id);
        if (plan == null || !plan.getUserId().equals(userId)) {
            throw new NotFoundException("学习计划不存在");
        }
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
        StudyPlan plan = studyPlanMapper.selectById(id);
        if (plan == null || !plan.getUserId().equals(userId)) {
            throw new NotFoundException("学习计划不存在");
        }
        tagService.unbindAll(ENTITY_TYPE, id);
        studyPlanMapper.deleteById(id);
        log.info("删除学习计划: id={}, userId={}", id, userId);
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
        plan.setProgress(dto.getProgress() != null ? dto.getProgress() : 0);
        plan.setStartDate(dto.getStartDate());
        plan.setEndDate(dto.getEndDate());
        plan.setStatus(dto.getStatus() != null ? dto.getStatus() : 0);
    }
}
