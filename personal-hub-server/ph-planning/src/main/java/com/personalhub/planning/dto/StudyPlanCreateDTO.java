package com.personalhub.planning.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 学习计划创建/编辑 DTO
 */
@Data
@Schema(description = "学习计划请求")
public class StudyPlanCreateDTO {

    @NotBlank(message = "计划名称不能为空")
    @Size(max = 200, message = "计划名称长度不能超过200")
    @Schema(description = "计划名称", example = "苍穹外卖")
    private String name;

    @Size(max = 100, message = "来源长度不能超过100")
    @Schema(description = "来源", example = "B站")
    private String source;

    @Size(max = 100, message = "作者长度不能超过100")
    @Schema(description = "作者", example = "黑马")
    private String author;

    @Size(max = 500, message = "地址长度不能超过500")
    @Schema(description = "资源地址")
    private String url;

    @Schema(description = "备注")
    private String remark;

    @Min(value = 0, message = "进度最小为0")
    @Max(value = 100, message = "进度最大为100")
    @Schema(description = "进度百分比 0-100", example = "0")
    private Integer progress;

    @Schema(description = "开始日期", example = "2025-10-20")
    private LocalDate startDate;

    @Schema(description = "结束日期", example = "2025-12-28")
    private LocalDate endDate;

    @Min(value = 0, message = "状态值无效")
    @Max(value = 3, message = "状态值无效")
    @Schema(description = "状态 0-未开始 1-学习中 2-已完成 3-已暂停", example = "0")
    private Integer status;

    @Schema(description = "标签ID列表")
    private List<Long> tagIds;
}
