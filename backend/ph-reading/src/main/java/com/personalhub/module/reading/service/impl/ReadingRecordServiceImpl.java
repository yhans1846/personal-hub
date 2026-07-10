package com.personalhub.module.reading.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.personalhub.common.exception.NotFoundException;
import com.personalhub.module.reading.dto.ReadingCreateDTO;
import com.personalhub.module.reading.dto.ReadingQueryDTO;
import com.personalhub.module.reading.entity.ReadingRecord;
import com.personalhub.module.reading.mapper.ReadingRecordMapper;
import com.personalhub.module.reading.service.ReadingRecordService;
import com.personalhub.module.reading.vo.ReadingVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReadingRecordServiceImpl implements ReadingRecordService {
    private final ReadingRecordMapper mapper;

    @Override
    public IPage<ReadingVO> list(Long userId, ReadingQueryDTO query) {
        LambdaQueryWrapper<ReadingRecord> w = new LambdaQueryWrapper<>();
        w.eq(ReadingRecord::getUserId, userId);
        if (StringUtils.hasText(query.getKeyword()))
            w.and(q -> q.like(ReadingRecord::getBookTitle, query.getKeyword()).or().like(ReadingRecord::getAuthor, query.getKeyword()));
        if (query.getStatus() != null) w.eq(ReadingRecord::getStatus, query.getStatus());
        w.orderByDesc(ReadingRecord::getCreatedAt);
        return mapper.selectPage(new Page<>(query.getPage(), query.getSize()), w).convert(ReadingVO::from);
    }

    @Override
    public ReadingVO getById(Long id, Long userId) {
        var r = mapper.selectById(id);
        if (r == null || !r.getUserId().equals(userId)) throw new NotFoundException("阅读记录不存在");
        return ReadingVO.from(r);
    }

    @Override @Transactional
    public ReadingVO create(Long userId, ReadingCreateDTO dto) {
        var r = new ReadingRecord();
        r.setUserId(userId); r.setBookTitle(dto.getBookTitle()); r.setAuthor(dto.getAuthor());
        r.setCoverUrl(dto.getCoverUrl()); r.setTotalChapters(dto.getTotalChapters() != null ? dto.getTotalChapters() : 0);
        r.setCurrentChapter(dto.getCurrentChapter() != null ? dto.getCurrentChapter() : 0);
        r.setProgress(dto.getProgress() != null ? dto.getProgress() : 0);
        r.setRating(dto.getRating()); r.setTotalDuration(dto.getTotalDuration());
        r.setStatus(dto.getStatus() != null ? dto.getStatus() : 0);
        r.setNotes(dto.getNotes()); r.setStartDate(dto.getStartDate()); r.setEndDate(dto.getEndDate());
        mapper.insert(r);
        log.info("新建阅读记录: id={}, book={}", r.getId(), dto.getBookTitle());
        return ReadingVO.from(r);
    }

    @Override @Transactional
    public ReadingVO update(Long id, Long userId, ReadingCreateDTO dto) {
        var r = mapper.selectById(id);
        if (r == null || !r.getUserId().equals(userId)) throw new NotFoundException("阅读记录不存在");
        r.setBookTitle(dto.getBookTitle()); r.setAuthor(dto.getAuthor()); r.setCoverUrl(dto.getCoverUrl());
        if (dto.getTotalChapters() != null) r.setTotalChapters(dto.getTotalChapters());
        if (dto.getCurrentChapter() != null) r.setCurrentChapter(dto.getCurrentChapter());
        if (dto.getProgress() != null) r.setProgress(dto.getProgress());
        if (dto.getRating() != null) r.setRating(dto.getRating());
        if (dto.getTotalDuration() != null) r.setTotalDuration(dto.getTotalDuration());
        if (dto.getStatus() != null) r.setStatus(dto.getStatus());
        r.setNotes(dto.getNotes()); r.setStartDate(dto.getStartDate()); r.setEndDate(dto.getEndDate());
        mapper.updateById(r);
        return ReadingVO.from(r);
    }

    @Override @Transactional
    public void delete(Long id, Long userId) {
        var r = mapper.selectById(id);
        if (r == null || !r.getUserId().equals(userId)) throw new NotFoundException("阅读记录不存在");
        mapper.deleteById(id);
    }
}
