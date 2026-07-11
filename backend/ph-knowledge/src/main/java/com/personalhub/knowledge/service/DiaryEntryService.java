package com.personalhub.knowledge.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.personalhub.knowledge.dto.DiaryCreateDTO;
import com.personalhub.knowledge.dto.DiaryQueryDTO;
import com.personalhub.knowledge.vo.DiaryVO;

import java.util.List;

/**
 * 日记服务接口
 */
public interface DiaryEntryService {

    /**
     * 分页查询日记列表
     */
    IPage<DiaryVO> list(Long userId, DiaryQueryDTO query);

    /**
     * 按月查询日记（月视图聚合）
     */
    List<DiaryVO> listByMonth(Long userId, String month);

    /**
     * 获取日记详情
     */
    DiaryVO getById(Long id, Long userId);

    /**
     * 新建日记
     */
    DiaryVO create(Long userId, DiaryCreateDTO dto);

    /**
     * 编辑日记
     */
    DiaryVO update(Long id, Long userId, DiaryCreateDTO dto);

    /**
     * 删除日记（软删除）
     */
    void delete(Long id, Long userId);
}
