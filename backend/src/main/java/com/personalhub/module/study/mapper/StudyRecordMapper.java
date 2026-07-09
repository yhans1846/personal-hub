package com.personalhub.module.study.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.personalhub.module.study.entity.StudyRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学习记录 Mapper
 */
@Mapper
public interface StudyRecordMapper extends BaseMapper<StudyRecord> {
}
