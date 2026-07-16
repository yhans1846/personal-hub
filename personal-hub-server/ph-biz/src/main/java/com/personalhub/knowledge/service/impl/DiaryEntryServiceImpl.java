package com.personalhub.knowledge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.personalhub.common.exception.NotFoundException;
import com.personalhub.common.util.EntityGuard;
import com.personalhub.knowledge.dto.DiaryCreateDTO;
import com.personalhub.knowledge.dto.DiaryQueryDTO;
import com.personalhub.knowledge.entity.DiaryEntry;
import com.personalhub.knowledge.mapper.DiaryEntryMapper;
import com.personalhub.knowledge.service.DiaryEntryService;
import com.personalhub.knowledge.vo.DiaryVO;
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
        DiaryEntry entry = EntityGuard.requireOwned(
                diaryEntryMapper.selectById(id), userId, DiaryEntry::getUserId, "日记不存在");
        return DiaryVO.from(entry);
    }

    @Override
    @Transactional
    public DiaryVO create(Long userId, DiaryCreateDTO dto) {
        var entry = DiaryEntry.builder()
                .userId(userId)
                .date(dto.getDate() != null ? dto.getDate() : LocalDate.now())
                .title(dto.getTitle())
                .content(dto.getContent())
                .mood(dto.getMood())
                .weather(dto.getWeather())
                .location(dto.getLocation())
                .imageFileId(dto.getImageFileId())
                .build();
        diaryEntryMapper.insert(entry);
        log.info("新建日记: id={}, userId={}, date={}", entry.getId(), userId, entry.getDate());
        return DiaryVO.from(entry);
    }

    @Override
    @Transactional
    public DiaryVO update(Long id, Long userId, DiaryCreateDTO dto) {
        DiaryEntry entry = EntityGuard.requireOwned(
                diaryEntryMapper.selectById(id), userId, DiaryEntry::getUserId, "日记不存在");
        if (dto.getDate() != null) entry.setDate(dto.getDate());
        if (dto.getTitle() != null) entry.setTitle(dto.getTitle());
        if (dto.getContent() != null) entry.setContent(dto.getContent());
        if (dto.getMood() != null) entry.setMood(dto.getMood());
        if (dto.getWeather() != null) entry.setWeather(dto.getWeather());
        if (dto.getLocation() != null) entry.setLocation(dto.getLocation());
        if (dto.getImageFileId() != null) entry.setImageFileId(dto.getImageFileId());
        diaryEntryMapper.updateById(entry);
        log.info("编辑日记: id={}, userId={}", id, userId);
        return DiaryVO.from(entry);
    }

    @Override
    @Transactional
    public void delete(Long id, Long userId) {
        EntityGuard.requireOwned(
                diaryEntryMapper.selectById(id), userId, DiaryEntry::getUserId, "日记不存在");
        diaryEntryMapper.deleteById(id);
        log.info("删除日记: id={}, userId={}", id, userId);
    }
}
