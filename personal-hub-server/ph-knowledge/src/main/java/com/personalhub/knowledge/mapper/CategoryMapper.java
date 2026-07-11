package com.personalhub.knowledge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.personalhub.knowledge.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 统一分类 Mapper
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {

    /** 统计分类下关联的实体数量 */
    Integer countByType(@Param("categoryId") Long categoryId, @Param("type") String type);

    /** 删除 note 分类关联 */
    void deleteNoteCategoryRels(@Param("categoryId") Long categoryId);
}
