package com.personalhub.knowledge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.personalhub.common.util.EntityGuard;
import com.personalhub.knowledge.dto.ReadingCreateDTO;
import com.personalhub.knowledge.dto.ReadingQueryDTO;
import com.personalhub.knowledge.entity.ReadingRecord;
import com.personalhub.knowledge.mapper.ReadingRecordMapper;
import com.personalhub.knowledge.service.ReadingRecordService;
import com.personalhub.knowledge.vo.ReadingVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReadingRecordServiceImpl implements ReadingRecordService {
    private final ReadingRecordMapper mapper;

    @Override
    public IPage<ReadingVO> list(Long userId, ReadingQueryDTO query) {
        LambdaQueryWrapper<ReadingRecord> w = buildListWrapper(userId, query, true);
        return mapper.selectPage(new Page<>(query.getPage(), query.getSize()), w).convert(ReadingVO::from);
    }

    @Override
    public ReadingVO getById(Long id, Long userId) {
        var r = EntityGuard.requireOwned(
                mapper.selectById(id), userId, ReadingRecord::getUserId, "阅读记录不存在");
        return ReadingVO.from(r);
    }

    @Override
    @Transactional
    public ReadingVO create(Long userId, ReadingCreateDTO dto) {
        var r = ReadingRecord.builder()
                .userId(userId)
                .bookTitle(dto.getBookTitle())
                .author(dto.getAuthor())
                .coverUrl(dto.getCoverUrl())
                .rating(dto.getRating())
                .totalDuration(dto.getTotalDuration())
                .notes(dto.getNotes())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .build();
        if (dto.getTotalChapters() != null) {
            r.setTotalChapters(dto.getTotalChapters());
        }
        if (dto.getCurrentChapter() != null) {
            r.setCurrentChapter(dto.getCurrentChapter());
        }
        if (dto.getProgress() != null) {
            r.setProgress(dto.getProgress());
        }
        if (dto.getStatus() != null) {
            r.setStatus(dto.getStatus());
        }
        mapper.insert(r);
        log.info("新建阅读记录: id={}, book={}", r.getId(), dto.getBookTitle());
        return ReadingVO.from(r);
    }

    @Override
    @Transactional
    public ReadingVO update(Long id, Long userId, ReadingCreateDTO dto) {
        var r = EntityGuard.requireOwned(
                mapper.selectById(id), userId, ReadingRecord::getUserId, "阅读记录不存在");
        r.setBookTitle(dto.getBookTitle());
        r.setAuthor(dto.getAuthor());
        r.setCoverUrl(dto.getCoverUrl());
        if (dto.getTotalChapters() != null) {
            r.setTotalChapters(dto.getTotalChapters());
        }
        if (dto.getCurrentChapter() != null) {
            r.setCurrentChapter(dto.getCurrentChapter());
        }
        if (dto.getProgress() != null) {
            r.setProgress(dto.getProgress());
        }
        if (dto.getRating() != null) {
            r.setRating(dto.getRating());
        }
        if (dto.getTotalDuration() != null) {
            r.setTotalDuration(dto.getTotalDuration());
        }
        if (dto.getStatus() != null) {
            r.setStatus(dto.getStatus());
        }
        r.setNotes(dto.getNotes());
        r.setStartDate(dto.getStartDate());
        r.setEndDate(dto.getEndDate());
        mapper.updateById(r);
        return ReadingVO.from(r);
    }

    @Override
    @Transactional
    public void delete(Long id, Long userId) {
        EntityGuard.requireOwned(
                mapper.selectById(id), userId, ReadingRecord::getUserId, "阅读记录不存在");
        mapper.deleteById(id);
    }

    @Override
    public byte[] exportXlsx(Long userId, ReadingQueryDTO query, String scope) {
        List<ReadingVO> rows = listForExport(userId, query, scope);
        DateTimeFormatter dateFmt = DateTimeFormatter.ISO_LOCAL_DATE;
        DateTimeFormatter dtFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String[] headers = {
                "书名", "作者", "状态", "进度", "当前章", "总章", "评分", "时长(分)", "开始", "结束", "备注", "更新时间"
        };
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("阅读记录");
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }
            int r = 1;
            for (ReadingVO vo : rows) {
                Row row = sheet.createRow(r++);
                setCell(row, 0, vo.getBookTitle());
                setCell(row, 1, vo.getAuthor());
                setCell(row, 2, vo.getStatusLabel());
                Cell progressCell = row.createCell(3);
                progressCell.setCellValue(vo.getProgress() != null ? vo.getProgress() : 0);
                Cell curCell = row.createCell(4);
                curCell.setCellValue(vo.getCurrentChapter() != null ? vo.getCurrentChapter() : 0);
                Cell totalCell = row.createCell(5);
                totalCell.setCellValue(vo.getTotalChapters() != null ? vo.getTotalChapters() : 0);
                if (vo.getRating() != null) {
                    row.createCell(6).setCellValue(vo.getRating());
                } else {
                    setCell(row, 6, "");
                }
                if (vo.getTotalDuration() != null) {
                    row.createCell(7).setCellValue(vo.getTotalDuration());
                } else {
                    setCell(row, 7, "");
                }
                setCell(row, 8, vo.getStartDate() != null ? vo.getStartDate().format(dateFmt) : "");
                setCell(row, 9, vo.getEndDate() != null ? vo.getEndDate().format(dateFmt) : "");
                setCell(row, 10, vo.getNotes());
                setCell(row, 11, vo.getUpdatedAt() != null ? vo.getUpdatedAt().format(dtFmt) : "");
            }
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(out);
            log.info("导出阅读记录 XLSX: userId={}, scope={}, rows={}", userId, scope, rows.size());
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("导出阅读记录失败", e);
        }
    }

    private List<ReadingVO> listForExport(Long userId, ReadingQueryDTO query, String scope) {
        boolean filtered = !"all".equalsIgnoreCase(scope);
        LambdaQueryWrapper<ReadingRecord> w;
        if (filtered) {
            w = buildListWrapper(userId, query != null ? query : new ReadingQueryDTO(), true);
        } else {
            w = new LambdaQueryWrapper<>();
            w.eq(ReadingRecord::getUserId, userId);
            w.orderByDesc(ReadingRecord::getUpdatedAt);
        }
        List<ReadingRecord> records = mapper.selectList(w);
        if (records.isEmpty()) {
            return Collections.emptyList();
        }
        return records.stream().map(ReadingVO::from).toList();
    }

    /**
     * 构建列表查询条件（含筛选与排序）
     */
    private LambdaQueryWrapper<ReadingRecord> buildListWrapper(Long userId, ReadingQueryDTO query, boolean applySort) {
        LambdaQueryWrapper<ReadingRecord> w = new LambdaQueryWrapper<>();
        w.eq(ReadingRecord::getUserId, userId);
        if (query != null) {
            if (StringUtils.hasText(query.getKeyword())) {
                String kw = query.getKeyword();
                w.and(q -> q.like(ReadingRecord::getBookTitle, kw).or().like(ReadingRecord::getAuthor, kw));
            }
            if (query.getStatus() != null) {
                w.eq(ReadingRecord::getStatus, query.getStatus());
            }
            if (applySort) {
                applySort(w, query.getSortBy(), query.getSortDir());
            }
        } else if (applySort) {
            w.orderByDesc(ReadingRecord::getUpdatedAt);
        }
        return w;
    }

    private void applySort(LambdaQueryWrapper<ReadingRecord> wrapper, String sortBy, String sortDir) {
        boolean asc = "asc".equalsIgnoreCase(sortDir);
        String field = StringUtils.hasText(sortBy) ? sortBy : "updatedAt";
        switch (field) {
            case "createdAt" -> {
                if (asc) {
                    wrapper.orderByAsc(ReadingRecord::getCreatedAt);
                } else {
                    wrapper.orderByDesc(ReadingRecord::getCreatedAt);
                }
            }
            case "progress" -> {
                if (asc) {
                    wrapper.orderByAsc(ReadingRecord::getProgress);
                } else {
                    wrapper.orderByDesc(ReadingRecord::getProgress);
                }
            }
            case "startDate" -> {
                if (asc) {
                    wrapper.orderByAsc(ReadingRecord::getStartDate);
                } else {
                    wrapper.orderByDesc(ReadingRecord::getStartDate);
                }
            }
            case "bookTitle" -> {
                if (asc) {
                    wrapper.orderByAsc(ReadingRecord::getBookTitle);
                } else {
                    wrapper.orderByDesc(ReadingRecord::getBookTitle);
                }
            }
            default -> {
                if (asc) {
                    wrapper.orderByAsc(ReadingRecord::getUpdatedAt);
                } else {
                    wrapper.orderByDesc(ReadingRecord::getUpdatedAt);
                }
            }
        }
    }

    private static void setCell(Row row, int col, String value) {
        row.createCell(col).setCellValue(value != null ? value : "");
    }
}
