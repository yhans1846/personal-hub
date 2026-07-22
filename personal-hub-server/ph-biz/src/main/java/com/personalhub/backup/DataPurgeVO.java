package com.personalhub.backup;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 清空数据结果（保留的快照备份）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "清空数据结果")
public class DataPurgeVO {

    private Long backupId;
    private Long fileSize;
    private LocalDateTime createdAt;
}
