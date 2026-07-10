package com.personalhub.module.diary.vo;

import com.personalhub.module.diary.entity.DiaryEntry;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 日记 VO
 */
@Data
@Schema(description = "日记详情")
public class DiaryVO {

    @Schema(description = "日记ID")
    private Long id;

    @Schema(description = "日记日期")
    private LocalDate date;

    @Schema(description = "日记标题")
    private String title;

    @Schema(description = "日记内容（Markdown）")
    private String content;

    @Schema(description = "内容摘要（列表用）")
    private String contentSummary;

    @Schema(description = "心情 1-很好 2-好 3-一般 4-不好 5-很差")
    private Integer mood;

    @Schema(description = "心情中文标签")
    private String moodLabel;

    @Schema(description = "天气")
    private String weather;

    @Schema(description = "地点")
    private String location;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    public static DiaryVO from(DiaryEntry entry) {
        DiaryVO vo = new DiaryVO();
        vo.setId(entry.getId());
        vo.setDate(entry.getDate());
        vo.setTitle(entry.getTitle());
        vo.setContent(entry.getContent());
        vo.setMood(entry.getMood());
        vo.setWeather(entry.getWeather());
        vo.setLocation(entry.getLocation());
        vo.setCreatedAt(entry.getCreatedAt());
        vo.setUpdatedAt(entry.getUpdatedAt());

        // 内容摘要（取前100字，去除 Markdown 标记）
        if (entry.getContent() != null) {
            String plain = entry.getContent()
                    .replaceAll("#+\\s*", "")
                    .replaceAll("[*\\[\\]`>]", "")
                    .replaceAll("!\\[.*?\\]\\(.*?\\)", "")
                    .replaceAll("\\[.*?\\]\\(.*?\\)", "");
            vo.setContentSummary(plain.length() > 100 ? plain.substring(0, 100) + "..." : plain);
        }

        // 心情标签
        if (entry.getMood() != null) {
            vo.setMoodLabel(switch (entry.getMood()) {
                case 1 -> "很好";
                case 2 -> "好";
                case 3 -> "一般";
                case 4 -> "不好";
                case 5 -> "很差";
                default -> "未知";
            });
        }
        return vo;
    }
}
