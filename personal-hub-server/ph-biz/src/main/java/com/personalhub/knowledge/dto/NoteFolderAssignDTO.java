package com.personalhub.knowledge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "更新笔记所属文件夹")
public class NoteFolderAssignDTO {

    @Schema(description = "文件夹ID，null 表示未分类")
    private Long folderId;
}
