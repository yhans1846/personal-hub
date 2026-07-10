package com.personalhub.module.studyplan.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.personalhub.module.studyplan.dto.StudyPlanCreateDTO;
import com.personalhub.module.studyplan.dto.StudyPlanQueryDTO;
import com.personalhub.module.studyplan.vo.StudyPlanVO;

/**
 * 学习计划服务接口
 */
public interface StudyPlanService {

    IPage<StudyPlanVO> list(Long userId, StudyPlanQueryDTO query);

    StudyPlanVO getById(Long id, Long userId);

    StudyPlanVO create(Long userId, StudyPlanCreateDTO dto);

    StudyPlanVO update(Long id, Long userId, StudyPlanCreateDTO dto);

    void delete(Long id, Long userId);
}
