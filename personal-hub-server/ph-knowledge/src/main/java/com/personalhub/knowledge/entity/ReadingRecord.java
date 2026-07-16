package com.personalhub.knowledge.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("reading_record")
public class ReadingRecord {
    @TableId(type = IdType.AUTO) private Long id;
    private Long userId;
    private String bookTitle;
    private String author;
    private String coverUrl;
    @Builder.Default private Integer totalChapters = 0;
    @Builder.Default private Integer currentChapter = 0;
    @Builder.Default private Integer progress = 0;
    private Integer rating;
    private Integer totalDuration;
    @Builder.Default private Integer status = 0;
    private String notes;
    private LocalDate startDate;
    private LocalDate endDate;
    @TableLogic private Integer isDeleted;
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE) private LocalDateTime updatedAt;
}
