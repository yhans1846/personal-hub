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

    /**
     * 导出学习计划为 XLSX（内存生成，不落盘）
     *
     * @param userId 用户 ID
     * @param query  查询条件（scope=filtered 时生效）
     * @param scope  filtered=当前筛选 / all=全部
     * @return XLSX 字节
     */
    byte[] exportXlsx(Long userId, StudyPlanQueryDTO query, String scope);
}
