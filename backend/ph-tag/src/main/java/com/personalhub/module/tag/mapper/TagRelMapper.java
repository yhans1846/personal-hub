package com.personalhub.module.tag.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.personalhub.module.tag.entity.TagRel;
import org.apache.ibatis.annotations.Mapper;

/**
 * 标签关联 Mapper
 */
@Mapper
public interface TagRelMapper extends BaseMapper<TagRel> {
}
