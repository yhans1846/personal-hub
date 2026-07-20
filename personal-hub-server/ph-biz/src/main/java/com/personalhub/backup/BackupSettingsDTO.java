package com.personalhub.backup;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 备份频率设置
 */
@Data
@Schema(description = "备份设置")
public class BackupSettingsDTO {

    @NotBlank
    @Pattern(regexp = "off|daily|weekly", message = "frequency 须为 off/daily/weekly")
    @Schema(description = "频率：off / daily / weekly", example = "daily")
    private String frequency;
}
