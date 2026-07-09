package com.personalhub.module.study.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.personalhub.module.study.dto.StudyRecordCreateDTO;
import com.personalhub.module.study.dto.StudyRecordQueryDTO;
import com.personalhub.module.study.vo.StudyRecordVO;

/**
 * 学习记录服务接口
 */
public interface StudyRecordService {

    IPage<StudyRecordVO> list(Long userId, StudyRecordQueryDTO query);

    StudyRecordVO getById(Long id, Long userId);

    StudyRecordVO create(Long userId, StudyRecordCreateDTO dto);

    StudyRecordVO update(Long id, Long userId, StudyRecordCreateDTO dto);

    void delete(Long id, Long userId);
}
