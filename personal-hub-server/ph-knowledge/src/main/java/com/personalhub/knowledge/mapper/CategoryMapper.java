package com.personalhub.knowledge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.personalhub.knowledge.entity.Category;
import org.apache.ibatis.annotations.Mapper;

/**
 * 统一分类 Mapper
 */
@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}
