package com.personalhub.knowledge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.personalhub.knowledge.entity.NoteCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 笔记分类 Mapper
 */
@Mapper
public interface NoteCategoryMapper extends BaseMapper<NoteCategory> {
}
