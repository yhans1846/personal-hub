package com.personalhub.knowledge.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.personalhub.knowledge.dto.StudyRecordCreateDTO;
import com.personalhub.knowledge.dto.StudyRecordQueryDTO;
import com.personalhub.knowledge.vo.StudyRecordVO;
import com.personalhub.knowledge.vo.StudyStatsVO;

/**
 * 学习记录服务接口
 */
public interface StudyRecordService {

    /**
     * 分页查询学习记录
     *
     * @param userId 用户ID
     * @param query  查询条件（支持关键词搜索和日期范围筛选）
     * @return 学习记录分页数据
     */
    IPage<StudyRecordVO> list(Long userId, StudyRecordQueryDTO query);

    /**
     * 获取学习记录详情
     *
     * @param id     记录ID
     * @param userId 用户ID
     * @return 学习记录VO
     */
    StudyRecordVO getById(Long id, Long userId);

    /**
     * 新建学习记录
     *
     * @param userId 用户ID
     * @param dto    学习记录创建参数
     * @return 新建的学习记录VO
     */
    StudyRecordVO create(Long userId, StudyRecordCreateDTO dto);

    /**
     * 编辑学习记录
     *
     * @param id     记录ID
     * @param userId 用户ID
     * @param dto    学习记录编辑参数
     * @return 更新后的学习记录VO
     */
    StudyRecordVO update(Long id, Long userId, StudyRecordCreateDTO dto);

    /**
     * 删除学习记录
     *
     * @param id     记录ID
     * @param userId 用户ID
     */
    void delete(Long id, Long userId);

    /**
     * 学习统计（今日时长 + 本周时长 + 连续天数）
     *
     * @param userId 用户ID
     * @return 统计数据
     */
    StudyStatsVO stats(Long userId);
}
