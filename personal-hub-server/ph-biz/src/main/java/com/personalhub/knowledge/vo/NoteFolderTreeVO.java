package com.personalhub.knowledge.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "笔记文件夹树（含计数）")
public class NoteFolderTreeVO {

    @Schema(description = "文件夹树")
    private List<NoteFolderVO> folders = new ArrayList<>();

    @Schema(description = "未删除笔记总数")
    private long totalCount;

    @Schema(description = "未分类笔记数（folder_id 为空）")
    private long uncategorizedCount;

    @Schema(description = "未分类笔记摘要")
    private List<NoteFolderNoteItem> uncategorizedNotes = new ArrayList<>();
}
