package com.personalhub.backup;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 备份历史项
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "备份历史项")
public class UserBackupVO {

    private Long id;
    private Long fileSize;
    private String triggerType;
    private String status;
    private String errorMessage;
    private LocalDateTime createdAt;

    public static UserBackupVO from(UserBackup e) {
        return UserBackupVO.builder()
                .id(e.getId())
                .fileSize(e.getFileSize())
                .triggerType(e.getTriggerType())
                .status(e.getStatus())
                .errorMessage(e.getErrorMessage())
                .createdAt(e.getCreatedAt())
                .build();
    }
}
