package com.personalhub.system.vo;

import com.personalhub.system.entity.UserLayout;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 布局配置 VO
 */
@Data
@Schema(description = "布局配置")
public class LayoutVO {

    @Schema(description = "布局类型")
    private String layoutType;

    @Schema(description = "布局配置 JSON")
    private String layoutJson;

    public static LayoutVO from(UserLayout entity) {
        LayoutVO vo = new LayoutVO();
        vo.setLayoutType(entity.getLayoutType());
        vo.setLayoutJson(entity.getLayoutJson());
        return vo;
    }
}
