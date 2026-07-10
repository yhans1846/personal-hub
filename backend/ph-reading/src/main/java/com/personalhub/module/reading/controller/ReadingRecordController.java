package com.personalhub.module.reading.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.personalhub.common.result.PageResult;
import com.personalhub.common.result.Result;
import com.personalhub.module.reading.dto.ReadingCreateDTO;
import com.personalhub.module.reading.dto.ReadingQueryDTO;
import com.personalhub.module.reading.service.ReadingRecordService;
import com.personalhub.module.reading.vo.ReadingVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "阅读记录", description = "阅读记录的增删改查、章节进度跟踪")
@RestController
@RequestMapping("/api/readings")
@RequiredArgsConstructor
public class ReadingRecordController {
    private final ReadingRecordService readingService;

    @Operation(summary = "阅读列表")
    @GetMapping
    public Result<PageResult<ReadingVO>> list(@Parameter(hidden = true) Authentication auth, ReadingQueryDTO q) {
        return Result.success(PageResult.of(readingService.list(Long.valueOf(auth.getName()), q)));
    }

    @Operation(summary = "阅读详情") @GetMapping("/{id}")
    public Result<ReadingVO> getById(@Parameter(hidden = true) Authentication auth, @PathVariable Long id) {
        return Result.success(readingService.getById(id, Long.valueOf(auth.getName())));
    }

    @Operation(summary = "新建记录") @PostMapping
    public Result<ReadingVO> create(@Parameter(hidden = true) Authentication auth, @Valid @RequestBody ReadingCreateDTO dto) {
        return Result.success(readingService.create(Long.valueOf(auth.getName()), dto));
    }

    @Operation(summary = "编辑记录") @PutMapping("/{id}")
    public Result<ReadingVO> update(@Parameter(hidden = true) Authentication auth, @PathVariable Long id, @Valid @RequestBody ReadingCreateDTO dto) {
        return Result.success(readingService.update(id, Long.valueOf(auth.getName()), dto));
    }

    @Operation(summary = "删除记录") @DeleteMapping("/{id}")
    public Result<Void> delete(@Parameter(hidden = true) Authentication auth, @PathVariable Long id) {
        readingService.delete(id, Long.valueOf(auth.getName()));
        return Result.success();
    }
}
