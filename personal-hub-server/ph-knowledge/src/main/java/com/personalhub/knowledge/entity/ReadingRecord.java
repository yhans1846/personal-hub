package com.personalhub.knowledge.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("reading_record")
public class ReadingRecord {
    @TableId(type = IdType.AUTO) private Long id;
    private Long userId;
    private String bookTitle;
    private String author;
    private String coverUrl;
    private Integer totalChapters;
    private Integer currentChapter;
    private Integer progress;
    private Integer rating;
    private Integer totalDuration;
    private Integer status;
    private String notes;
    private LocalDate startDate;
    private LocalDate endDate;
    @TableLogic private Integer isDeleted;
    @TableField(fill = FieldFill.INSERT) private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE) private LocalDateTime updatedAt;
}
