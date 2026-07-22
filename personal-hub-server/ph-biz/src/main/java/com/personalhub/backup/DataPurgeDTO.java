package com.personalhub.backup;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 清空数据请求
 */
@Data
@Schema(description = "清空数据（需字符图验证码）")
public class DataPurgeDTO {

    @NotBlank(message = "验证码 ID 不能为空")
    private String captchaId;

    @NotBlank(message = "请输入验证码")
    private String captchaCode;
}
