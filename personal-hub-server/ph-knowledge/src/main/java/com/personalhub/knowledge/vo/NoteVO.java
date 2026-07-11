package com.personalhub.knowledge.vo;

import com.personalhub.knowledge.entity.Note;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 笔记 VO
 */
@Data
@Schema(description = "笔记详情")
public class NoteVO {

    @Schema(description = "笔记ID")
    private Long id;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "Markdown 内容")
    private String content;

    @Schema(description = "是否收藏 0-否 1-是")
    private Integer isFavorite;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    @Schema(description = "关联分类列表")
    private List<CategoryItem> categories;

    @Schema(description = "关联标签列表")
    private List<TagItem> tags;

    @Data
    @Schema(description = "分类信息")
    public static class CategoryItem {
        @Schema(description = "分类ID")
        private Long id;
        @Schema(description = "分类名称")
        private String name;
    }

    @Data
    @Schema(description = "标签信息")
    public static class TagItem {
        @Schema(description = "标签ID")
        private Long id;
        @Schema(description = "标签名称")
        private String name;
        @Schema(description = "标签颜色")
        private String color;
    }

    public static NoteVO from(Note note) {
        NoteVO vo = new NoteVO();
        vo.setId(note.getId());
        vo.setTitle(note.getTitle());
        vo.setContent(note.getContent());
        vo.setIsFavorite(note.getIsFavorite());
        vo.setCreatedAt(note.getCreatedAt());
        vo.setUpdatedAt(note.getUpdatedAt());
        return vo;
    }
}
