package com.personalhub.backup;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 备份专用 SQL（物理删除、关联导出）。
 */
@Mapper
public interface BackupDataMapper {

    @Select("""
            SELECT r.note_id AS noteId, r.category_id AS categoryId
            FROM note_category_rel r
            INNER JOIN note_note n ON n.id = r.note_id
            WHERE n.user_id = #{userId}
            """)
    List<NoteCategoryRelRow> selectNoteCategoryRels(@Param("userId") Long userId);

    @Delete("""
            DELETE r FROM note_category_rel r
            INNER JOIN note_note n ON n.id = r.note_id
            WHERE n.user_id = #{userId}
            """)
    int deleteNoteCategoryRelsByUser(@Param("userId") Long userId);

    @Insert("""
            INSERT INTO note_category_rel (note_id, category_id) VALUES (#{noteId}, #{categoryId})
            """)
    int insertNoteCategoryRel(@Param("noteId") Long noteId, @Param("categoryId") Long categoryId);

    @Delete("""
            DELETE tr FROM tag_rel tr
            INNER JOIN tag t ON t.id = tr.tag_id
            WHERE t.user_id = #{userId}
            """)
    int deleteTagRelsByUser(@Param("userId") Long userId);

    @Delete("DELETE FROM note_note WHERE user_id = #{userId}")
    int deleteNotes(@Param("userId") Long userId);

    @Delete("DELETE FROM note_folder WHERE user_id = #{userId}")
    int deleteNoteFolders(@Param("userId") Long userId);

    @Delete("DELETE FROM diary_entry WHERE user_id = #{userId}")
    int deleteDiaries(@Param("userId") Long userId);

    @Delete("DELETE FROM todo_task WHERE user_id = #{userId}")
    int deleteTodos(@Param("userId") Long userId);

    @Delete("DELETE FROM bookmark_url WHERE user_id = #{userId}")
    int deleteBookmarks(@Param("userId") Long userId);

    @Delete("DELETE FROM study_record WHERE user_id = #{userId}")
    int deleteStudyRecords(@Param("userId") Long userId);

    @Delete("DELETE FROM study_plan WHERE user_id = #{userId}")
    int deleteStudyPlans(@Param("userId") Long userId);

    @Delete("DELETE FROM reading_record WHERE user_id = #{userId}")
    int deleteReadings(@Param("userId") Long userId);

    @Delete("DELETE FROM file_resource WHERE user_id = #{userId}")
    int deleteFiles(@Param("userId") Long userId);

    @Delete("DELETE FROM category WHERE user_id = #{userId}")
    int deleteCategories(@Param("userId") Long userId);

    @Delete("DELETE FROM tag WHERE user_id = #{userId}")
    int deleteTags(@Param("userId") Long userId);

    @Delete("DELETE FROM user_layout WHERE user_id = #{userId}")
    int deleteLayouts(@Param("userId") Long userId);
}
