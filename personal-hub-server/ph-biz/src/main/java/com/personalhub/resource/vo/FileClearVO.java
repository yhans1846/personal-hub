package com.personalhub.resource.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 清空文件结果 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "清空文件结果")
public class FileClearVO {

    @Schema(description = "删除条数")
    private int deleted;
}
