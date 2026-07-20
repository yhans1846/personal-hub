package com.personalhub.knowledge.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 清空回收站结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "清空回收站结果")
public class RecycleEmptyVO {

    @Schema(description = "永久删除条数")
    private int deleted;
}
