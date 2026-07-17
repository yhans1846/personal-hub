package com.personalhub.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 滑动拼图松手预检
 */
@Data
@Schema(description = "拼图预检请求")
public class CaptchaCheckDTO {

    @NotBlank
    @Schema(description = "验证码 ID")
    private String captchaId;

    @NotNull
    @Schema(description = "滑块水平偏移（像素）")
    private Integer sliderX;
}
