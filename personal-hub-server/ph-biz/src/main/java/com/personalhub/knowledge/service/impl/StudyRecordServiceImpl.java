package com.personalhub.knowledge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.personalhub.common.util.EntityGuard;
import com.personalhub.knowledge.dto.StudyRecordCreateDTO;
import com.personalhub.knowledge.dto.StudyRecordQueryDTO;
import com.personalhub.knowledge.entity.StudyRecord;
import com.personalhub.knowledge.mapper.StudyRecordMapper;
import com.personalhub.knowledge.service.StudyRecordService;
import com.personalhub.knowledge.vo.StudyRecordVO;
import com.personalhub.knowledge.vo.StudyStatsVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

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
        StudyRecord record = EntityGuard.requireOwned(
                mapper.selectById(id), userId, StudyRecord::getUserId, "学习记录不存在");
        return StudyRecordVO.from(record);
    }

    @Override
    public StudyRecordVO create(Long userId, StudyRecordCreateDTO dto) {
        var record = StudyRecord.builder()
                .userId(userId)
                .subject(dto.getSubject())
                .date(dto.getDate())
                .duration(dto.getDuration())
                .content(dto.getContent())
                .reflection(dto.getReflection())
                .build();
        mapper.insert(record);
        log.info("新建学习记录: id={}, userId={}, subject={}", record.getId(), userId, dto.getSubject());
        return StudyRecordVO.from(record);
    }

    @Override
    public StudyRecordVO update(Long id, Long userId, StudyRecordCreateDTO dto) {
        StudyRecord record = EntityGuard.requireOwned(
                mapper.selectById(id), userId, StudyRecord::getUserId, "学习记录不存在");
        record.setSubject(dto.getSubject());
        record.setDate(dto.getDate());
        record.setDuration(dto.getDuration());
        record.setContent(dto.getContent());
        record.setReflection(dto.getReflection());
        mapper.updateById(record);
        log.info("编辑学习记录: id={}, userId={}", id, userId);
        return StudyRecordVO.from(record);
    }

    @Override
    public void delete(Long id, Long userId) {
        EntityGuard.requireOwned(
                mapper.selectById(id), userId, StudyRecord::getUserId, "学习记录不存在");
        mapper.deleteById(id); // 逻辑删除
        log.info("删除学习记录: id={}, userId={}", id, userId);
    }

    @Override
    public StudyStatsVO stats(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(DayOfWeek.MONDAY);

        long todayDuration = mapper.sumDurationByDate(userId, today);
        long weekDuration = mapper.sumDurationSince(userId, weekStart);

        // 连续天数：查询所有学习过的日期，倒序计算
        List<LocalDate> dates = mapper.listDistinctDates(userId);
        long streak = 0;
        for (LocalDate d : dates) {
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
