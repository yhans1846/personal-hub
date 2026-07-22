package com.personalhub.knowledge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "移动笔记文件夹")
public class NoteFolderMoveDTO {

    @Schema(description = "新父文件夹ID，空为根")
    private Long parentId;

    @NotNull(message = "排序不能为空")
    @Schema(description = "同级排序")
    private Integer sortOrder;
}
