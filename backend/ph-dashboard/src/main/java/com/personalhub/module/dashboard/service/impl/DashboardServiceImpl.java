package com.personalhub.module.dashboard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.personalhub.module.bookmark.entity.BookmarkUrl;
import com.personalhub.module.bookmark.mapper.BookmarkUrlMapper;
import com.personalhub.module.dashboard.service.DashboardService;
import com.personalhub.module.dashboard.vo.DashboardStatsVO;
import com.personalhub.module.dashboard.vo.TrendVO;
import com.personalhub.module.dashboard.vo.TrendVO.DataPoint;
import com.personalhub.module.diary.entity.DiaryEntry;
import com.personalhub.module.diary.mapper.DiaryEntryMapper;
import com.personalhub.module.file.entity.FileResource;
import com.personalhub.module.file.mapper.FileResourceMapper;
import com.personalhub.module.note.entity.Note;
import com.personalhub.module.note.mapper.NoteMapper;
import com.personalhub.module.reading.entity.ReadingRecord;
import com.personalhub.module.reading.mapper.ReadingRecordMapper;
import com.personalhub.module.study.entity.StudyRecord;
import com.personalhub.module.study.mapper.StudyRecordMapper;
import com.personalhub.module.studyplan.entity.StudyPlan;
import com.personalhub.module.studyplan.mapper.StudyPlanMapper;
import com.personalhub.module.todo.entity.TodoTask;
import com.personalhub.module.todo.mapper.TodoTaskMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

/**
 * Dashboard 服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final NoteMapper noteMapper;
    private final StudyRecordMapper studyRecordMapper;
    private final TodoTaskMapper todoTaskMapper;
    private final FileResourceMapper fileResourceMapper;
    private final DiaryEntryMapper diaryEntryMapper;
    private final BookmarkUrlMapper bookmarkUrlMapper;
    private final ReadingRecordMapper readingRecordMapper;
    private final StudyPlanMapper studyPlanMapper;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public DashboardStatsVO getStats(Long userId) {
        DashboardStatsVO stats = new DashboardStatsVO();
        LocalDate now = LocalDate.now();

        // 笔记
        stats.setNoteCount(countNotes(userId));

        // 学习记录
        stats.setStudyCount(countStudies(userId));
        stats.setStudyDurationTotal(sumDuration(userId));
        stats.setStudyDurationThisWeek(sumDurationThisWeek(userId, now));

        // 待办
        stats.setTodoTotal(countTodos(userId, null));
        stats.setTodoDone(countTodos(userId, 1));
        stats.setTodoPending(countTodos(userId, 0));
        stats.setTodoOverdue(countOverdueTodos(userId));

        // 文件
        stats.setFileCount(countFiles(userId));

        // 日记
        stats.setDiaryCount(countDiaries(userId));
        stats.setDiaryCountThisMonth(countDiariesThisMonth(userId, now));

        // 收藏
        stats.setBookmarkCount(countBookmarks(userId));

        // 阅读
        stats.setReadingCount(countReadings(userId));

        // 学习计划
        stats.setStudyPlanCount(countStudyPlans(userId));

        return stats;
    }

    @Override
    public TrendVO getTrends(Long userId, int days) {
        LocalDate since = LocalDate.now().minusDays(days);
        TrendVO trends = new TrendVO();

        // 学习趋势（按日期分组时长）
        trends.setStudyTrend(queryTrend(
                "SELECT date, COALESCE(SUM(duration), 0) FROM study_record " +
                "WHERE user_id = ? AND date >= ? AND is_deleted = 0 GROUP BY date ORDER BY date",
                userId, since));

        // 笔记趋势（按创建日期分组）
        trends.setNoteTrend(queryTrend(
                "SELECT DATE(created_at), COUNT(*) FROM note_note " +
                "WHERE user_id = ? AND created_at >= ? AND is_deleted = 0 GROUP BY DATE(created_at) ORDER BY DATE(created_at)",
                userId, since));

        // 待办趋势（按创建日期分组）
        trends.setTodoTrend(queryTrend(
                "SELECT DATE(created_at), COUNT(*) FROM todo_task " +
                "WHERE user_id = ? AND created_at >= ? GROUP BY DATE(created_at) ORDER BY DATE(created_at)",
                userId, since));

        // 阅读趋势（按创建日期分组）
        trends.setReadingTrend(queryTrend(
                "SELECT DATE(created_at), COUNT(*) FROM reading_record " +
                "WHERE user_id = ? AND created_at >= ? AND is_deleted = 0 GROUP BY DATE(created_at) ORDER BY DATE(created_at)",
                userId, since));

        return trends;
    }

    private List<DataPoint> queryTrend(String sql, Long userId, LocalDate since) {
        return jdbcTemplate.query(sql,
                new Object[]{userId, java.sql.Date.valueOf(since)},
                (rs, rowNum) -> new DataPoint(
                        rs.getString(1),
                        rs.getLong(2)));
    }

    private Long countNotes(Long userId) {
        return noteMapper.selectCount(
                new LambdaQueryWrapper<Note>()
                        .eq(Note::getUserId, userId)
                        .eq(Note::getIsDeleted, 0));
    }

    private Long countStudies(Long userId) {
        return studyRecordMapper.selectCount(
                new LambdaQueryWrapper<StudyRecord>()
                        .eq(StudyRecord::getUserId, userId));
    }

    private Long sumDuration(Long userId) {
        Long result = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(duration), 0) FROM study_record WHERE user_id = ? AND is_deleted = 0",
                Long.class, userId);
        return result != null ? result : 0L;
    }

    private Long sumDurationThisWeek(Long userId, LocalDate now) {
        LocalDate weekStart = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LambdaQueryWrapper<StudyRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudyRecord::getUserId, userId)
                .ge(StudyRecord::getDate, weekStart)
                .le(StudyRecord::getDate, now);
        return studyRecordMapper.selectList(wrapper).stream()
                .mapToLong(StudyRecord::getDuration)
                .sum();
    }

    private Long countTodos(Long userId, Integer isDone) {
        LambdaQueryWrapper<TodoTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TodoTask::getUserId, userId);
        if (isDone != null) {
            wrapper.eq(TodoTask::getIsDone, isDone);
        }
        return todoTaskMapper.selectCount(wrapper);
    }

    private Long countOverdueTodos(Long userId) {
        return todoTaskMapper.selectCount(
                new LambdaQueryWrapper<TodoTask>()
                        .eq(TodoTask::getUserId, userId)
                        .eq(TodoTask::getIsDone, 0)
                        .lt(TodoTask::getDueDate, LocalDate.now()));
    }

    private Long countFiles(Long userId) {
        return fileResourceMapper.selectCount(
                new LambdaQueryWrapper<FileResource>()
                        .eq(FileResource::getUserId, userId));
    }

    private Long countDiaries(Long userId) {
        return diaryEntryMapper.selectCount(
                new LambdaQueryWrapper<DiaryEntry>()
                        .eq(DiaryEntry::getUserId, userId));
    }

    private Long countDiariesThisMonth(Long userId, LocalDate now) {
        LocalDate monthStart = now.withDayOfMonth(1);
        LocalDate monthEnd = now.withDayOfMonth(now.lengthOfMonth());
        return diaryEntryMapper.selectCount(
                new LambdaQueryWrapper<DiaryEntry>()
                        .eq(DiaryEntry::getUserId, userId)
                        .ge(DiaryEntry::getDate, monthStart)
                        .le(DiaryEntry::getDate, monthEnd));
    }

    private Long countBookmarks(Long userId) {
        return bookmarkUrlMapper.selectCount(
                new LambdaQueryWrapper<BookmarkUrl>()
                        .eq(BookmarkUrl::getUserId, userId));
    }

    private Long countReadings(Long userId) {
        return readingRecordMapper.selectCount(
                new LambdaQueryWrapper<ReadingRecord>()
                        .eq(ReadingRecord::getUserId, userId));
    }

    private Long countStudyPlans(Long userId) {
        return studyPlanMapper.selectCount(
                new LambdaQueryWrapper<StudyPlan>()
                        .eq(StudyPlan::getUserId, userId));
    }
}
