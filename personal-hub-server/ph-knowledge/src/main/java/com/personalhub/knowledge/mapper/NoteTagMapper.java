package com.personalhub.knowledge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.personalhub.knowledge.entity.NoteTag;
import org.apache.ibatis.annotations.Mapper;

/**
 * 笔记标签 Mapper
 */
@Mapper
public interface NoteTagMapper extends BaseMapper<NoteTag> {
}
