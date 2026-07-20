package com.personalhub.knowledge.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.personalhub.knowledge.dto.NoteCreateDTO;
import com.personalhub.knowledge.dto.NoteQueryDTO;
import com.personalhub.knowledge.vo.NoteVO;
import com.personalhub.knowledge.vo.RecycleEmptyVO;

/**
 * 笔记服务接口
 */
public interface NoteService {

    /**
     * 分页查询笔记列表
     *
     * @param userId 用户ID
     * @param query  查询条件（支持关键词搜索、分类/标签/收藏/回收站筛选）
     * @return 笔记分页数据
     */
    IPage<NoteVO> listNotes(Long userId, NoteQueryDTO query);

    /**
     * 获取笔记详情
     *
     * @param id     笔记ID
     * @param userId 用户ID
     * @return 笔记详情VO
     */
    NoteVO getById(Long id, Long userId);

    /**
     * 新建笔记
     *
     * @param userId 用户ID
     * @param dto    笔记创建参数
     * @return 新建的笔记VO
     */
    NoteVO create(Long userId, NoteCreateDTO dto);

    /**
     * 编辑笔记
     *
     * @param id     笔记ID
     * @param userId 用户ID
     * @param dto    笔记编辑参数
     * @return 更新后的笔记VO
     */
    NoteVO update(Long id, Long userId, NoteCreateDTO dto);

    /**
     * 删除笔记（软删除到回收站，原因 USER_DELETE）
     *
     * @param id     笔记ID
     * @param userId 用户ID
     */
    void delete(Long id, Long userId);

    /**
     * 归档笔记（软删除到回收站，原因 AUTO_ARCHIVE）
     *
     * @param id     笔记ID
     * @param userId 用户ID
     */
    void archive(Long id, Long userId);

    /**
     * 恢复笔记
     *
     * @param id     笔记ID
     * @param userId 用户ID
     */
    void restore(Long id, Long userId);

    /**
     * 永久删除
     *
     * @param id     笔记ID
     * @param userId 用户ID
     */
    void permanentDelete(Long id, Long userId);

    /**
     * 清空回收站（永久删除当前用户全部已删/已归档笔记）
     *
     * @param userId 用户ID
     * @return 删除条数
     */
    RecycleEmptyVO emptyRecycleBin(Long userId);

    /**
     * 切换收藏状态
     *
     * @param id     笔记ID
     * @param userId 用户ID
     */
    void toggleFavorite(Long id, Long userId);

    /**
     * 最近编辑列表
     *
     * @param userId 用户ID
     * @param page   页码
     * @param size   每页条数
     * @return 笔记分页数据
     */
    IPage<NoteVO> getRecent(Long userId, int page, int size);

    /**
     * 分页查询回收站（已删除笔记，按删除时间倒序）
     *
     * @param userId 用户ID
     * @param query  查询参数（分页）
     * @return 回收站笔记分页数据
     */
    IPage<NoteVO> getRecycleList(Long userId, NoteQueryDTO query);

    /**
     * 只读预览笔记（允许查看已删除笔记）
     *
     * @param id     笔记ID
     * @param userId 用户ID
     * @return 笔记详情VO
     */
    NoteVO getPreview(Long id, Long userId);
}
