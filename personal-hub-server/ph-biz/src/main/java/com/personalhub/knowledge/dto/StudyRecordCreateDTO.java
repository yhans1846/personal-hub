package com.personalhub.knowledge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 学习记录创建/编辑 DTO
 */
@Data
@Schema(description = "学习记录创建/编辑请求")
public class StudyRecordCreateDTO {

    @NotBlank(message = "学习主题不能为空")
    @Schema(description = "学习主题", example = "Spring Boot 基础")
    private String subject;

    @NotNull(message = "学习日期不能为空")
    @Schema(description = "学习日期", example = "2026-07-09")
    private LocalDate date;

    @NotNull(message = "学习时长不能为空")
    @Min(value = 1, message = "学习时长至少1分钟")
    @Schema(description = "学习时长（分钟）", example = "120")
    private Integer duration;

    @Schema(description = "学习内容")
    private String content;

    @Schema(description = "学习心得")
    private String reflection;

    @Schema(description = "关联学习计划ID")
    private Long planId;
}
