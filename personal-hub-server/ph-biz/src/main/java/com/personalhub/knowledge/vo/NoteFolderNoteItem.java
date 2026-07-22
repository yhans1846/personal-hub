package com.personalhub.knowledge.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "文件夹树中的笔记摘要")
public class NoteFolderNoteItem {

    @Schema(description = "笔记ID")
    private Long id;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "所属文件夹，空=未分类")
    private Long folderId;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
