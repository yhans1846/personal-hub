package com.personalhub.module.dashboard.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Dashboard 数据查询 Mapper
 */
@Mapper
public interface DashboardMapper {

    // ====== 趋势查询 ======

    /** 学习时长趋势（按天分组） */
    List<Map<String, Object>> selectStudyTrend(@Param("userId") Long userId, @Param("since") LocalDate since);

    /** 笔记创建趋势（按天分组） */
    List<Map<String, Object>> selectNoteTrend(@Param("userId") Long userId, @Param("since") LocalDate since);

    /** 待办创建趋势（按天分组） */
    List<Map<String, Object>> selectTodoTrend(@Param("userId") Long userId, @Param("since") LocalDate since);

    /** 阅读记录趋势（按天分组） */
    List<Map<String, Object>> selectReadingTrend(@Param("userId") Long userId, @Param("since") LocalDate since);

    // ====== 全文搜索 ======

    /** 搜索笔记 */
    List<Map<String, Object>> searchNotes(@Param("userId") Long userId, @Param("keyword") String keyword);

    /** 搜索日记 */
    List<Map<String, Object>> searchDiaries(@Param("userId") Long userId, @Param("keyword") String keyword);

    /** 搜索待办 */
    List<Map<String, Object>> searchTodos(@Param("userId") Long userId, @Param("keyword") String keyword);

    /** 搜索学习记录 */
    List<Map<String, Object>> searchStudies(@Param("userId") Long userId, @Param("keyword") String keyword);

    /** 搜索收藏夹 */
    List<Map<String, Object>> searchBookmarks(@Param("userId") Long userId, @Param("keyword") String keyword);

    /** 搜索阅读记录 */
    List<Map<String, Object>> searchReadings(@Param("userId") Long userId, @Param("keyword") String keyword);

    /** 搜索文件 */
    List<Map<String, Object>> searchFiles(@Param("userId") Long userId, @Param("keyword") String keyword);

    /** 搜索学习计划 */
    List<Map<String, Object>> searchPlans(@Param("userId") Long userId, @Param("keyword") String keyword);

    // ====== 统计查询 ======

    /** 学习总时长 */
    Long sumStudyDuration(@Param("userId") Long userId);
}
