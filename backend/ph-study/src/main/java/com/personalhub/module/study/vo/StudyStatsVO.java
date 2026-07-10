package com.personalhub.module.study.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 学习统计 VO
 */
@Data
@Schema(description = "学习统计数据")
public class StudyStatsVO {

    @Schema(description = "今日学习时长（分钟）")
    private long todayDuration;

    @Schema(description = "本周学习时长（分钟）")
    private long weekDuration;

    @Schema(description = "连续学习天数")
    private long streak;
}
