package com.personalhub.module.studyplan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.personalhub.common.exception.NotFoundException;
import com.personalhub.module.study.entity.StudyRecord;
import com.personalhub.module.study.mapper.StudyRecordMapper;
import com.personalhub.module.studyplan.dto.StudyPlanCreateDTO;
import com.personalhub.module.studyplan.dto.StudyPlanQueryDTO;
import com.personalhub.module.studyplan.entity.StudyPlan;
import com.personalhub.module.studyplan.mapper.StudyPlanMapper;
import com.personalhub.module.studyplan.service.StudyPlanService;
import com.personalhub.module.studyplan.vo.StudyPlanVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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

    private final StudyPlanMapper studyPlanMapper;
    private final StudyRecordMapper studyRecordMapper;

    @Override
    public IPage<StudyPlanVO> list(Long userId, StudyPlanQueryDTO query) {
        LambdaQueryWrapper<StudyPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudyPlan::getUserId, userId);
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.like(StudyPlan::getName, query.getKeyword());
        }
        if (query.getStatus() != null) {
            wrapper.eq(StudyPlan::getStatus, query.getStatus());
        }
        wrapper.orderByDesc(StudyPlan::getCreatedAt);

        Page<StudyPlan> page = new Page<>(query.getPage(), query.getSize());
        IPage<StudyPlan> planPage = studyPlanMapper.selectPage(page, wrapper);

        // 批量加载关联记录数
        List<Long> planIds = planPage.getRecords().stream().map(StudyPlan::getId).toList();
        Map<Long, Long> countMap = loadRecordCounts(planIds);

        return planPage.convert(plan -> {
            StudyPlanVO vo = StudyPlanVO.from(plan);
            vo.setRecordCount(countMap.getOrDefault(plan.getId(), 0L));
            return vo;
        });
    }

    private Map<Long, Long> loadRecordCounts(List<Long> planIds) {
        if (planIds.isEmpty()) return Map.of();
        LambdaQueryWrapper<StudyRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(StudyRecord::getPlanId, planIds);
        return studyRecordMapper.selectList(wrapper).stream()
                .collect(Collectors.groupingBy(StudyRecord::getPlanId, Collectors.counting()));
    }

    @Override
    public StudyPlanVO getById(Long id, Long userId) {
        StudyPlan plan = studyPlanMapper.selectById(id);
        if (plan == null || !plan.getUserId().equals(userId)) {
            throw new NotFoundException("学习计划不存在");
        }
        StudyPlanVO vo = StudyPlanVO.from(plan);
        // 加载记录数
        LambdaQueryWrapper<StudyRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudyRecord::getPlanId, id);
        vo.setRecordCount(studyRecordMapper.selectCount(wrapper));
        return vo;
    }

    @Override
    @Transactional
    public StudyPlanVO create(Long userId, StudyPlanCreateDTO dto) {
        StudyPlan plan = new StudyPlan();
        plan.setUserId(userId);
        plan.setName(dto.getName());
        plan.setGoal(dto.getGoal());
        plan.setProgress(dto.getProgress() != null ? dto.getProgress() : 0);
        plan.setStartDate(dto.getStartDate());
        plan.setEndDate(dto.getEndDate());
        plan.setStatus(dto.getStatus() != null ? dto.getStatus() : 0);
        studyPlanMapper.insert(plan);
        log.info("新建学习计划: id={}, userId={}, name={}", plan.getId(), userId, dto.getName());
        return StudyPlanVO.from(plan);
    }

    @Override
    @Transactional
    public StudyPlanVO update(Long id, Long userId, StudyPlanCreateDTO dto) {
        StudyPlan plan = studyPlanMapper.selectById(id);
        if (plan == null || !plan.getUserId().equals(userId)) {
            throw new NotFoundException("学习计划不存在");
        }
        plan.setName(dto.getName());
        plan.setGoal(dto.getGoal());
        if (dto.getProgress() != null) plan.setProgress(dto.getProgress());
        if (dto.getStartDate() != null) plan.setStartDate(dto.getStartDate());
        if (dto.getEndDate() != null) plan.setEndDate(dto.getEndDate());
        if (dto.getStatus() != null) plan.setStatus(dto.getStatus());
        studyPlanMapper.updateById(plan);
        log.info("编辑学习计划: id={}, userId={}", id, userId);
        return StudyPlanVO.from(plan);
    }

    @Override
    @Transactional
    public void delete(Long id, Long userId) {
        StudyPlan plan = studyPlanMapper.selectById(id);
        if (plan == null || !plan.getUserId().equals(userId)) {
            throw new NotFoundException("学习计划不存在");
        }
        studyPlanMapper.deleteById(id);
        log.info("删除学习计划: id={}, userId={}", id, userId);
    }
}
