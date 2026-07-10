package com.personalhub.module.studyplan.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * 学习计划创建/编辑 DTO
 */
@Data
@Schema(description = "学习计划请求")
public class StudyPlanCreateDTO {

    @NotBlank(message = "计划名称不能为空")
    @Size(max = 200, message = "计划名称长度不能超过200")
    @Schema(description = "计划名称", example = "Spring Boot 深入学习")
    private String name;

    @Schema(description = "学习目标")
    private String goal;

    @Min(value = 0, message = "进度最小为0")
    @Max(value = 100, message = "进度最大为100")
    @Schema(description = "进度百分比 0-100", example = "0")
    private Integer progress;

    @Schema(description = "开始日期", example = "2026-07-01")
    private LocalDate startDate;

    @Schema(description = "结束日期", example = "2026-08-31")
    private LocalDate endDate;

    @Min(value = 0, message = "状态值无效")
    @Max(value = 3, message = "状态值无效")
    @Schema(description = "状态 0-未开始 1-进行中 2-已完成 3-已放弃", example = "0")
    private Integer status;
}
