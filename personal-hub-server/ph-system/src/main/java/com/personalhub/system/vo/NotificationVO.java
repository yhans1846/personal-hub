package com.personalhub.system.vo;

import com.personalhub.system.entity.Notification;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知返回 VO
 */
@Data
@Schema(description = "通知VO")
public class NotificationVO {

    @Schema
    private Long id;

    @Schema
    private String type;

    @Schema
    private String title;

    @Schema
    private String content;

    @Schema
    private Boolean isRead;

    @Schema
    private Long relatedId;

    @Schema
    private String relatedType;

    @Schema
    private LocalDateTime createdAt;

    public static NotificationVO from(Notification n) {
        NotificationVO vo = new NotificationVO();
        vo.setId(n.getId());
        vo.setType(n.getType());
        vo.setTitle(n.getTitle());
        vo.setContent(n.getContent());
        vo.setIsRead(n.getIsRead() != null && n.getIsRead() == 1);
        vo.setRelatedId(n.getRelatedId());
        vo.setRelatedType(n.getRelatedType());
        vo.setCreatedAt(n.getCreatedAt());
        return vo;
    }
}
