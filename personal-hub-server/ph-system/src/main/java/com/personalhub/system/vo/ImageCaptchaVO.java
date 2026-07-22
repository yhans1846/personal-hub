package com.personalhub.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 字符图验证码
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "字符图验证码")
public class ImageCaptchaVO {

    @Schema(description = "验证码 ID")
    private String captchaId;

    @Schema(description = "PNG Base64（不含 data: 前缀）")
    private String imageBase64;
}
