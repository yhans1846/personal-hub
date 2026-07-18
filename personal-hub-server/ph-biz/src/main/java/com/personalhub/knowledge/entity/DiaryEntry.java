package com.personalhub.knowledge.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 日记条目实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("diary_entry")
public class DiaryEntry {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private LocalDate date;

    private String title;

    private String content;

    private Integer mood;

    private String weather;

    private String location;

    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private BigDecimal latitude;

    @TableField(updateStrategy = FieldStrategy.ALWAYS)
    private BigDecimal longitude;

    /** 配图文件名列表(JSON字符串数组) */
    private String imageFiles;

    /**
     * 解析为文件名列表；历史数字 id JSON 视为无效返回空。
     */
    public List<String> parseImageFiles() {
        if (imageFiles == null || imageFiles.isBlank()) return Collections.emptyList();
        try {
            List<?> raw = new ObjectMapper().readValue(imageFiles, new TypeReference<List<?>>() {});
            return raw.stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .filter(s -> !s.isBlank() && !s.contains("..") && !s.contains("/") && !s.contains("\\"))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
