package com.personalhub.planning.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.personalhub.planning.dto.StudyPlanCreateDTO;
import com.personalhub.planning.dto.StudyPlanQueryDTO;
import com.personalhub.planning.vo.StudyPlanStatsVO;
import com.personalhub.planning.vo.StudyPlanVO;

/**
 * 学习计划服务接口
 */
public interface StudyPlanService {

    IPage<StudyPlanVO> list(Long userId, StudyPlanQueryDTO query);

    StudyPlanStatsVO stats(Long userId);

    StudyPlanVO getById(Long id, Long userId);

    StudyPlanVO create(Long userId, StudyPlanCreateDTO dto);

    StudyPlanVO update(Long id, Long userId, StudyPlanCreateDTO dto);

    void delete(Long id, Long userId);
}
