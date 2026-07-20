package com.personalhub.knowledge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 笔记批量导出请求
 */
@Data
@Schema(description = "笔记批量导出")
public class NoteBatchExportDTO {

    @NotEmpty(message = "请选择要导出的笔记")
    @Size(max = 50, message = "单次最多导出50篇笔记")
    @Schema(description = "笔记 ID 列表", example = "[1, 2, 3]")
    private List<Long> ids;
}
