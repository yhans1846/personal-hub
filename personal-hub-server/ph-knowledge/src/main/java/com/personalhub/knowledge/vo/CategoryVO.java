package com.personalhub.knowledge.vo;

import com.personalhub.knowledge.entity.Category;
import lombok.Data;

/**
 * 分类视图对象
 */
@Data
public class CategoryVO {

    private Long id;
    private String name;
    private String type;
    private Integer sortOrder;
    private Integer count;
    private String createdAt;

    public static CategoryVO from(Category category, Integer count) {
        CategoryVO vo = new CategoryVO();
        vo.setId(category.getId());
        vo.setName(category.getName());
        vo.setType(category.getType());
        vo.setSortOrder(category.getSortOrder());
        vo.setCount(count != null ? count : 0);
        vo.setCreatedAt(category.getCreatedAt() != null ? category.getCreatedAt().toString() : null);
        return vo;
    }
}
