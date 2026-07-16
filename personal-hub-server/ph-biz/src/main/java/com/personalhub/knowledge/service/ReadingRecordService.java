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

    /**
     * 导出阅读记录为 XLSX（内存生成，不落盘）
     *
     * @param userId 用户 ID
     * @param query  查询条件（scope=filtered 时生效）
     * @param scope  filtered=当前筛选 / all=全部
     * @return XLSX 字节
     */
    byte[] exportXlsx(Long userId, ReadingQueryDTO query, String scope);
}
