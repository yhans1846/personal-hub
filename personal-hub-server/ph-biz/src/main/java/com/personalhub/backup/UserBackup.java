package com.personalhub.backup;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户备份历史
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_backup")
public class UserBackup {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String filePath;

    private Long fileSize;

    /** MANUAL / AUTO */
    private String triggerType;

    /** OK / FAILED */
    private String status;

    private String errorMessage;

    private LocalDateTime createdAt;
}
