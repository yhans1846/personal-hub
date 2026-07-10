package com.personalhub.module.study.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.personalhub.common.exception.NotFoundException;
import com.personalhub.module.study.dto.StudyRecordCreateDTO;
import com.personalhub.module.study.dto.StudyRecordQueryDTO;
import com.personalhub.module.study.entity.StudyRecord;
import com.personalhub.module.study.mapper.StudyRecordMapper;
import com.personalhub.module.study.service.StudyRecordService;
import com.personalhub.module.study.vo.StudyRecordVO;
import com.personalhub.module.study.vo.StudyStatsVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudyRecordServiceImpl implements StudyRecordService {

    private final StudyRecordMapper mapper;

    @Override
    public IPage<StudyRecordVO> list(Long userId, StudyRecordQueryDTO query) {
        LambdaQueryWrapper<StudyRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudyRecord::getUserId, userId);

        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w
                    .like(StudyRecord::getSubject, query.getKeyword())
                    .or()
                    .like(StudyRecord::getContent, query.getKeyword())
            );
        }
        if (query.getStartDate() != null) {
            wrapper.ge(StudyRecord::getDate, query.getStartDate());
        }
        if (query.getEndDate() != null) {
            wrapper.le(StudyRecord::getDate, query.getEndDate());
        }

        wrapper.orderByDesc(StudyRecord::getDate).orderByDesc(StudyRecord::getCreatedAt);

        Page<StudyRecord> page = new Page<>(query.getPage(), query.getSize());
        IPage<StudyRecord> recordPage = mapper.selectPage(page, wrapper);
        return recordPage.convert(StudyRecordVO::from);
    }

    @Override
    public StudyRecordVO getById(Long id, Long userId) {
        StudyRecord record = mapper.selectById(id);
        if (record == null || !record.getUserId().equals(userId)) {
            log.warn("学习记录不存在或无权访问: id={}, userId={}", id, userId);
            throw new NotFoundException("学习记录不存在");
        }
        return StudyRecordVO.from(record);
    }

    @Override
    public StudyRecordVO create(Long userId, StudyRecordCreateDTO dto) {
        StudyRecord record = new StudyRecord();
        record.setUserId(userId);
        record.setSubject(dto.getSubject());
        record.setDate(dto.getDate());
        record.setDuration(dto.getDuration());
        record.setContent(dto.getContent());
        record.setReflection(dto.getReflection());
        record.setPlanId(dto.getPlanId());
        mapper.insert(record);
        log.info("新建学习记录: id={}, userId={}, subject={}", record.getId(), userId, dto.getSubject());
        return StudyRecordVO.from(record);
    }

    @Override
    public StudyRecordVO update(Long id, Long userId, StudyRecordCreateDTO dto) {
        StudyRecord record = mapper.selectById(id);
        if (record == null || !record.getUserId().equals(userId)) {
            log.warn("编辑学习记录不存在或无权访问: id={}, userId={}", id, userId);
            throw new NotFoundException("学习记录不存在");
        }
        record.setSubject(dto.getSubject());
        record.setDate(dto.getDate());
        record.setDuration(dto.getDuration());
        record.setContent(dto.getContent());
        record.setReflection(dto.getReflection());
        record.setPlanId(dto.getPlanId());
        mapper.updateById(record);
        log.info("编辑学习记录: id={}, userId={}", id, userId);
        return StudyRecordVO.from(record);
    }

    @Override
    public void delete(Long id, Long userId) {
        StudyRecord record = mapper.selectById(id);
        if (record == null || !record.getUserId().equals(userId)) {
            log.warn("删除学习记录不存在或无权访问: id={}, userId={}", id, userId);
            throw new NotFoundException("学习记录不存在");
        }
        mapper.deleteById(id); // 逻辑删除
        log.info("删除学习记录: id={}, userId={}", id, userId);
    }

    @Override
    public StudyStatsVO stats(Long userId) {
        // 获取全部有效记录（个人项目数据量小）
        LambdaQueryWrapper<StudyRecord> all = new LambdaQueryWrapper<>();
        all.eq(StudyRecord::getUserId, userId);
        java.util.List<StudyRecord> records = mapper.selectList(all);

        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDate weekStart = today.with(java.time.DayOfWeek.MONDAY);

        long todayDuration = 0;
        long weekDuration = 0;
        java.util.Set<java.time.LocalDate> uniqueDates = new java.util.HashSet<>();

        for (StudyRecord r : records) {
            if (r.getDate() != null) {
                if (r.getDate().equals(today)) todayDuration += r.getDuration();
                if (!r.getDate().isBefore(weekStart)) weekDuration += r.getDuration();
                uniqueDates.add(r.getDate());
            }
        }

        // 连续天数：按日期倒序，从今天开始数连续
        java.util.List<java.time.LocalDate> sorted = uniqueDates.stream()
                .sorted(java.util.Comparator.reverseOrder()).toList();
        long streak = 0;
        for (java.time.LocalDate d : sorted) {
            if (d.equals(today) || d.equals(today.minusDays(streak))) {
                streak++;
            } else break;
        }

        StudyStatsVO vo = new StudyStatsVO();
        vo.setTodayDuration(todayDuration);
        vo.setWeekDuration(weekDuration);
        vo.setStreak(streak);
        return vo;
    }
}
