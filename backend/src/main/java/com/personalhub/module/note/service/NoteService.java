package com.personalhub.module.note.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.personalhub.module.note.dto.NoteCreateDTO;
import com.personalhub.module.note.dto.NoteQueryDTO;
import com.personalhub.module.note.vo.NoteVO;

/**
 * 笔记服务接口
 */
public interface NoteService {

    /** 分页查询笔记列表 */
    IPage<NoteVO> listNotes(Long userId, NoteQueryDTO query);

    /** 获取笔记详情 */
    NoteVO getById(Long id, Long userId);

    /** 新建笔记 */
    NoteVO create(Long userId, NoteCreateDTO dto);

    /** 编辑笔记 */
    NoteVO update(Long id, Long userId, NoteCreateDTO dto);

    /** 删除笔记（软删除到回收站） */
    void delete(Long id, Long userId);

    /** 恢复笔记 */
    void restore(Long id, Long userId);

    /** 永久删除 */
    void permanentDelete(Long id, Long userId);

    /** 切换收藏状态 */
    void toggleFavorite(Long id, Long userId);

    /** 最近编辑列表 */
    IPage<NoteVO> getRecent(Long userId, int page, int size);
}
