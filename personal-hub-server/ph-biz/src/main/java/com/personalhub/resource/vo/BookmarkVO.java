package com.personalhub.resource.vo;

import com.personalhub.resource.entity.BookmarkUrl;
import com.personalhub.knowledge.vo.TagVO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 收藏夹 VO
 */
@Data
@Schema(description = "收藏夹详情")
public class BookmarkVO {

    @Schema(description = "收藏ID")
    private Long id;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "网址")
    private String url;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "是否展示到首页外部快捷：0 否 / 1 是")
    private Integer showOnDashboard;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "关联标签列表")
    private List<TagVO> tags;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    public static BookmarkVO from(BookmarkUrl url) {
        BookmarkVO vo = new BookmarkVO();
        vo.setId(url.getId());
        vo.setTitle(url.getTitle());
        vo.setUrl(url.getUrl());
        vo.setDescription(url.getDescription());
        vo.setCategoryId(url.getCategoryId());
        vo.setShowOnDashboard(url.getShowOnDashboard() != null ? url.getShowOnDashboard() : 0);
        vo.setCreatedAt(url.getCreatedAt());
        vo.setUpdatedAt(url.getUpdatedAt());
        return vo;
    }
}
