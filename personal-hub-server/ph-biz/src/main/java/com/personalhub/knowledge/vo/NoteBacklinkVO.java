package com.personalhub.knowledge.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 笔记回链摘要（引用了当前笔记的其它笔记）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "笔记回链项")
public class NoteBacklinkVO {

    @Schema(description = "笔记 ID")
    private Long id;

    @Schema(description = "笔记标题")
    private String title;
}
