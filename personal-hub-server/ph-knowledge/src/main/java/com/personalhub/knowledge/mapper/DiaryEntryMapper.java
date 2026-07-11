package com.personalhub.knowledge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.personalhub.knowledge.entity.DiaryEntry;
import org.apache.ibatis.annotations.Mapper;

/**
 * 日记 Mapper
 */
@Mapper
public interface DiaryEntryMapper extends BaseMapper<DiaryEntry> {
}
