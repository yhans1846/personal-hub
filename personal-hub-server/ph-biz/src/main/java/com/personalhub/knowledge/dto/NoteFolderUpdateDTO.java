package com.personalhub.knowledge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "重命名笔记文件夹")
public class NoteFolderUpdateDTO {

    @NotBlank(message = "名称不能为空")
    @Size(max = 100, message = "名称长度不能超过100")
    @Schema(description = "名称")
    private String name;
}
