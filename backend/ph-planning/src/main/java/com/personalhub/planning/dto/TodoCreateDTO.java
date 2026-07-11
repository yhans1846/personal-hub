package com.personalhub.planning.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * 创建/编辑待办任务 DTO
 */
@Data
@Schema(description = "创建/编辑待办任务请求")
public class TodoCreateDTO {

    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题长度不能超过200")
    @Schema(description = "任务标题", example = "完成接口文档")
    private String title;

    @Schema(description = "任务内容")
    private String content;

    @Min(value = 1, message = "优先级最小为 1")
    @Max(value = 3, message = "优先级最大为 3")
    @Schema(description = "优先级 1-高 2-中 3-低", example = "2")
    private Integer priority = 2;

    @Schema(description = "截止日期", example = "2026-07-15")
    private LocalDate dueDate;
}
