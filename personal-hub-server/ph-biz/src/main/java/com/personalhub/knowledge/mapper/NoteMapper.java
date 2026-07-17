package com.personalhub.knowledge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.personalhub.knowledge.dto.NoteQueryDTO;
import com.personalhub.knowledge.entity.Note;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 笔记 Mapper
 */
@Mapper
public interface NoteMapper extends BaseMapper<Note> {

    /** 分页查询笔记（含分类/标签子查询） */
    IPage<Note> selectNotePage(Page<?> page, @Param("userId") Long userId, @Param("query") NoteQueryDTO query);

    /** 查询笔记的分类列表 */
    List<Map<String, Object>> selectCategoriesByNoteId(@Param("noteId") Long noteId);

    /** 批量查询笔记分类（返回含 noteId/id/name） */
    List<Map<String, Object>> selectCategoriesByNoteIds(@Param("noteIds") List<Long> noteIds);

    /** 分页查询回收站（已删除笔记，按删除时间倒序） */
    IPage<Note> selectRecyclePage(Page<?> page, @Param("userId") Long userId, @Param("query") NoteQueryDTO query);

    /** 按笔记删除分类关联 */
    void deleteCategoryRelsByNoteId(@Param("noteId") Long noteId);

    /** 批量插入笔记-分类关联 */
    void insertCategoryRels(@Param("noteId") Long noteId, @Param("categoryIds") List<Long> categoryIds);
}
