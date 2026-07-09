package com.personalhub.module.note.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.personalhub.module.note.entity.NoteTag;
import org.apache.ibatis.annotations.Mapper;

/**
 * 笔记标签 Mapper
 */
@Mapper
public interface NoteTagMapper extends BaseMapper<NoteTag> {
}
