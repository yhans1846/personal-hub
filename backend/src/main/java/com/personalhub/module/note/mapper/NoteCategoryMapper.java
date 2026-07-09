package com.personalhub.module.note.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.personalhub.module.note.entity.NoteCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 笔记分类 Mapper
 */
@Mapper
public interface NoteCategoryMapper extends BaseMapper<NoteCategory> {
}
