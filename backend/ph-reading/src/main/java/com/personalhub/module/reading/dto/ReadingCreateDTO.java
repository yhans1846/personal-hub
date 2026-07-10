package com.personalhub.module.reading.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.time.LocalDate;

@Data
@Schema(description = "阅读记录请求")
public class ReadingCreateDTO {
    @NotBlank @Size(max = 255) @Schema(description = "书名", example = "深入理解Java虚拟机")
    private String bookTitle;
    @Schema(description = "作者", example = "周志明") private String author;
    @Schema(description = "封面图") private String coverUrl;
    @Schema(description = "总章节数", example = "20") private Integer totalChapters;
    @Schema(description = "当前章节", example = "5") private Integer currentChapter;
    @Min(0) @Max(100) @Schema(description = "阅读进度 0-100", example = "25") private Integer progress;
    @Min(1) @Max(5) @Schema(description = "评分 1-5", example = "4") private Integer rating;
    @Schema(description = "总阅读时长（分钟）", example = "120") private Integer totalDuration;
    @Min(0) @Max(2) @Schema(description = "状态 0-未读 1-在读 2-读完", example = "1") private Integer status;
    @Schema(description = "阅读笔记") private String notes;
    @Schema(description = "开始阅读日期") private LocalDate startDate;
    @Schema(description = "读完日期") private LocalDate endDate;
}
