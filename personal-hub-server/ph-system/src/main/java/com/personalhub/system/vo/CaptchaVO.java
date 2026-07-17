package com.personalhub.system.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 书架归位验证码响应
 */
@Data
@Builder
@Schema(description = "书架归位验证码")
public class CaptchaVO {

    @Schema(description = "验证码 ID")
    private String captchaId;

    @Schema(description = "书架格数")
    private int slotCount;

    @Schema(description = "书架上已有书本 emoji（空串表示空位）")
    private java.util.List<String> shelfBooks;

    @Schema(description = "待归位书本 emoji")
    private String dragBook;
}
