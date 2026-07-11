package com.personalhub.module.dashboard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.personalhub.module.dashboard.mapper.DashboardMapper;
import com.personalhub.module.dashboard.service.DashboardService;
import com.personalhub.module.dashboard.vo.DashboardStatsVO;
import com.personalhub.module.dashboard.vo.SearchVO;
import com.personalhub.module.dashboard.vo.TrendVO;
import com.personalhub.module.dashboard.vo.TrendVO.DataPoint;
import com.personalhub.resource.entity.BookmarkUrl;
import com.personalhub.resource.mapper.BookmarkUrlMapper;
import com.personalhub.resource.entity.FileResource;
import com.personalhub.resource.mapper.FileResourceMapper;
import com.personalhub.knowledge.entity.DiaryEntry;
import com.personalhub.knowledge.mapper.DiaryEntryMapper;
import com.personalhub.knowledge.entity.Note;
import com.personalhub.knowledge.mapper.NoteMapper;
import com.personalhub.knowledge.entity.ReadingRecord;
import com.personalhub.knowledge.mapper.ReadingRecordMapper;
import com.personalhub.knowledge.entity.StudyRecord;
import com.personalhub.knowledge.mapper.StudyRecordMapper;
import com.personalhub.planning.entity.StudyPlan;
import com.personalhub.planning.mapper.StudyPlanMapper;
import com.personalhub.planning.entity.TodoTask;
import com.personalhub.planning.mapper.TodoTaskMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    private final DashboardMapper dashboardMapper;

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
        stats.setReadingInProgress(countReadingsByStatus(userId, 1));
        stats.setReadingCompleted(countReadingsByStatus(userId, 2));
        stats.setReadingDurationTotal(sumReadingDuration(userId));

        // 学习计划
        stats.setStudyPlanCount(countStudyPlans(userId));

        return stats;
    }

    @Override
    public TrendVO getTrends(Long userId, int days) {
        LocalDate since = LocalDate.now().minusDays(days);
        TrendVO trends = new TrendVO();

        trends.setStudyTrend(queryTrend(dashboardMapper.selectStudyTrend(userId, since)));
        trends.setNoteTrend(queryTrend(dashboardMapper.selectNoteTrend(userId, since)));
        trends.setTodoTrend(queryTrend(dashboardMapper.selectTodoTrend(userId, since)));
        trends.setReadingTrend(queryTrend(dashboardMapper.selectReadingTrend(userId, since)));

        return trends;
    }

    @Override
    public SearchVO search(Long userId, String keyword) {
        String like = "%" + keyword + "%";
        List<SearchVO.SearchGroup> groups = new ArrayList<>();
        int total = 0;

        List<SearchVO.SearchItem> notes = mapSearchItems(
                dashboardMapper.searchNotes(userId, keyword));
        if (!notes.isEmpty()) {
            notes.forEach(i -> i.setUrl("/notes/" + i.getId() + "/edit"));
            groups.add(new SearchVO.SearchGroup("note", "笔记", "FileText", notes.size(), notes));
            total += notes.size();
        }

        List<SearchVO.SearchItem> diaries = mapSearchItems(
                dashboardMapper.searchDiaries(userId, keyword));
        if (!diaries.isEmpty()) {
            diaries.forEach(i -> i.setUrl("/diaries/" + i.getId() + "/edit"));
            groups.add(new SearchVO.SearchGroup("diary", "日记", "PenLine", diaries.size(), diaries));
            total += diaries.size();
        }

        List<SearchVO.SearchItem> todos = mapSearchItems(
                dashboardMapper.searchTodos(userId, keyword));
        if (!todos.isEmpty()) {
            todos.forEach(i -> i.setUrl("/todos/" + i.getId() + "/edit"));
            groups.add(new SearchVO.SearchGroup("todo", "待办", "CheckSquare", todos.size(), todos));
            total += todos.size();
        }

        List<SearchVO.SearchItem> studies = mapSearchItems(
                dashboardMapper.searchStudies(userId, keyword));
        if (!studies.isEmpty()) {
            studies.forEach(i -> i.setUrl("/study-records/" + i.getId() + "/edit"));
            groups.add(new SearchVO.SearchGroup("study", "学习记录", "BookOpen", studies.size(), studies));
            total += studies.size();
        }

        List<SearchVO.SearchItem> bookmarks = mapSearchItems(
                dashboardMapper.searchBookmarks(userId, keyword));
        if (!bookmarks.isEmpty()) {
            bookmarks.forEach(i -> i.setUrl("/bookmarks/" + i.getId() + "/edit"));
            groups.add(new SearchVO.SearchGroup("bookmark", "收藏夹", "Bookmark", bookmarks.size(), bookmarks));
            total += bookmarks.size();
        }

        List<SearchVO.SearchItem> readings = mapSearchItems(
                dashboardMapper.searchReadings(userId, keyword));
        if (!readings.isEmpty()) {
            readings.forEach(i -> i.setUrl("/readings/" + i.getId() + "/edit"));
            groups.add(new SearchVO.SearchGroup("reading", "阅读记录", "BookMarked", readings.size(), readings));
            total += readings.size();
        }

        List<SearchVO.SearchItem> files = mapSearchItems(
                dashboardMapper.searchFiles(userId, keyword));
        if (!files.isEmpty()) {
            groups.add(new SearchVO.SearchGroup("file", "文件", "FolderOpen", files.size(), files));
            total += files.size();
        }

        List<SearchVO.SearchItem> plans = mapSearchItems(
                dashboardMapper.searchPlans(userId, keyword));
        if (!plans.isEmpty()) {
            plans.forEach(i -> i.setUrl("/study-plans/" + i.getId() + "/edit"));
            groups.add(new SearchVO.SearchGroup("study_plan", "学习计划", "Target", plans.size(), plans));
            total += plans.size();
        }

        SearchVO result = new SearchVO();
        result.setGroups(groups);
        result.setTotal(total);
        return result;
    }

    private List<SearchVO.SearchItem> mapSearchItems(List<Map<String, Object>> rows) {
        return rows.stream().map(row -> {
            SearchVO.SearchItem item = new SearchVO.SearchItem();
            item.setId(((Number) row.get("id")).longValue());
            item.setTitle((String) row.get("title"));
            item.setSnippet((String) row.get("snippet"));
            Object dateObj = row.get("date_str");
            item.setDate(dateObj != null ? dateObj.toString().substring(0, 10) : "");
            return item;
        }).toList();
    }

    private List<DataPoint> queryTrend(List<Map<String, Object>> rows) {
        return rows.stream().map(row -> new DataPoint(
                (String) row.get("date_str"),
                row.get("value") instanceof Number ? ((Number) row.get("value")).longValue() : 0L))
                .toList();
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
        Long result = dashboardMapper.sumStudyDuration(userId);
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

    private Long countReadingsByStatus(Long userId, Integer status) {
        return readingRecordMapper.selectCount(
                new LambdaQueryWrapper<ReadingRecord>()
                        .eq(ReadingRecord::getUserId, userId)
                        .eq(ReadingRecord::getStatus, status));
    }

    private Long sumReadingDuration(Long userId) {
        var list = readingRecordMapper.selectList(
                new LambdaQueryWrapper<ReadingRecord>()
                        .eq(ReadingRecord::getUserId, userId)
                        .select(ReadingRecord::getTotalDuration));
        return list.stream()
                .mapToLong(r -> r.getTotalDuration() != null ? r.getTotalDuration() : 0L)
                .sum();
    }

    private Long countStudyPlans(Long userId) {
        return studyPlanMapper.selectCount(
                new LambdaQueryWrapper<StudyPlan>()
                        .eq(StudyPlan::getUserId, userId));
    }
}
