package com.personalhub.knowledge.vo;

import com.personalhub.knowledge.entity.Tag;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 标签 VO
 */
@Data
@Schema(description = "标签信息")
public class TagVO {

    @Schema(description = "标签ID")
    private Long id;

    @Schema(description = "标签名称")
    private String name;

    @Schema(description = "标签颜色")
    private String color;

    @Schema(description = "使用次数")
    private Long usageCount;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    public static TagVO from(Tag tag) {
        TagVO vo = new TagVO();
        vo.setId(tag.getId());
        vo.setName(tag.getName());
        vo.setColor(tag.getColor());
        vo.setCreatedAt(tag.getCreatedAt());
        return vo;
    }
}
