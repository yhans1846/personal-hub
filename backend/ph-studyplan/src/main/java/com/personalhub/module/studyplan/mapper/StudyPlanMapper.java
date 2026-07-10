package com.personalhub.module.studyplan.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.personalhub.module.studyplan.entity.StudyPlan;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学习计划 Mapper
 */
@Mapper
public interface StudyPlanMapper extends BaseMapper<StudyPlan> {
}
