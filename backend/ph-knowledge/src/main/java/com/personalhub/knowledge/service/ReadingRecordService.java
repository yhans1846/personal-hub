package com.personalhub.knowledge.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.personalhub.knowledge.dto.ReadingCreateDTO;
import com.personalhub.knowledge.dto.ReadingQueryDTO;
import com.personalhub.knowledge.vo.ReadingVO;

public interface ReadingRecordService {
    IPage<ReadingVO> list(Long userId, ReadingQueryDTO query);
    ReadingVO getById(Long id, Long userId);
    ReadingVO create(Long userId, ReadingCreateDTO dto);
    ReadingVO update(Long id, Long userId, ReadingCreateDTO dto);
    void delete(Long id, Long userId);
}
