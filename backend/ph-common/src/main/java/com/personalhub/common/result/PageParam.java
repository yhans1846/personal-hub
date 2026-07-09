package com.personalhub.common.result;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

/**
 * 分页查询参数公共基类
 * <p>
 * 所有需要分页的查询 DTO 继承此类，避免重复定义 page / size 字段。
 * 默认 page=1, size=10，子类可通过 setter 改变默认值。
 */
@Getter
@Setter
public class PageParam {

    @Schema(description = "页码", example = "1")
    @Min(value = 1, message = "页码最小为 1")
    private int page = 1;

    @Schema(description = "每页条数", example = "10")
    @Min(value = 1, message = "每页条数最小为 1")
    private int size = 10;

    /**
     * MyBatis-Plus 分页偏移量（从 0 开始）
     */
    public long offset() {
        return (long) (page - 1) * size;
    }
}
