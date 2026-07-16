package com.personalhub.knowledge.vo;

import com.personalhub.knowledge.entity.ReadingRecord;
import com.personalhub.knowledge.enums.ReadingStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Schema(description = "阅读记录详情")
public class ReadingVO {
    @Schema private Long id;
    @Schema private String bookTitle;
    @Schema private String author;
    @Schema private String coverUrl;
    @Schema private Integer totalChapters;
    @Schema private Integer currentChapter;
    @Schema private Integer progress;
    @Schema private Integer rating;
    @Schema private Integer totalDuration;
    @Schema private Integer status;
    @Schema private String statusLabel;
    @Schema private String notes;
    @Schema private LocalDate startDate;
    @Schema private LocalDate endDate;
    @Schema private LocalDateTime createdAt;
    @Schema private LocalDateTime updatedAt;

    public static ReadingVO from(ReadingRecord r) {
        ReadingVO vo = new ReadingVO();
        vo.setId(r.getId()); vo.setBookTitle(r.getBookTitle()); vo.setAuthor(r.getAuthor());
        vo.setCoverUrl(r.getCoverUrl()); vo.setTotalChapters(r.getTotalChapters());
        vo.setCurrentChapter(r.getCurrentChapter()); vo.setProgress(r.getProgress());
        vo.setRating(r.getRating()); vo.setTotalDuration(r.getTotalDuration());
        vo.setStatus(r.getStatus()); vo.setNotes(r.getNotes());
        vo.setStartDate(r.getStartDate()); vo.setEndDate(r.getEndDate());
        vo.setCreatedAt(r.getCreatedAt()); vo.setUpdatedAt(r.getUpdatedAt());
        vo.setStatusLabel(ReadingStatus.labelOf(r.getStatus()));
        return vo;
    }
}
