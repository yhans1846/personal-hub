package com.personalhub.knowledge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "创建笔记文件夹")
public class NoteFolderCreateDTO {

    @NotBlank(message = "名称不能为空")
    @Size(max = 100, message = "名称长度不能超过100")
    @Schema(description = "名称")
    private String name;

    @Schema(description = "父文件夹ID，空为根")
    private Long parentId;
}
