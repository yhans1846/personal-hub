package com.personalhub.module.study.vo;

import com.personalhub.module.study.entity.StudyRecord;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 学习记录 VO
 */
@Data
@Schema(description = "学习记录详情")
public class StudyRecordVO {

    @Schema(description = "记录ID")
    private Long id;

    @Schema(description = "学习主题")
    private String subject;

    @Schema(description = "学习日期")
    private LocalDate date;

    @Schema(description = "学习时长（分钟）")
    private Integer duration;

    @Schema(description = "学习内容")
    private String content;

    @Schema(description = "学习心得")
    private String reflection;

    @Schema(description = "关联学习计划ID")
    private Long planId;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    public static StudyRecordVO from(StudyRecord record) {
        StudyRecordVO vo = new StudyRecordVO();
        vo.setId(record.getId());
        vo.setSubject(record.getSubject());
        vo.setDate(record.getDate());
        vo.setDuration(record.getDuration());
        vo.setContent(record.getContent());
        vo.setReflection(record.getReflection());
        vo.setPlanId(record.getPlanId());
        vo.setCreatedAt(record.getCreatedAt());
        vo.setUpdatedAt(record.getUpdatedAt());
        return vo;
    }
}
