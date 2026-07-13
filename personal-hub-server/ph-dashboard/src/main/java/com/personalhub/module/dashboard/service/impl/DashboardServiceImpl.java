package com.personalhub.module.dashboard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.personalhub.module.dashboard.mapper.DashboardMapper;
import com.personalhub.module.dashboard.service.DashboardService;
import com.personalhub.module.dashboard.vo.DashboardStatsVO;
import com.personalhub.module.dashboard.vo.SearchVO;
import com.personalhub.module.dashboard.vo.StatsVO;
import com.personalhub.module.dashboard.vo.StatsVO.ActivityItem;
import com.personalhub.module.dashboard.vo.StatsVO.DataPoint;
import com.personalhub.module.dashboard.vo.StatsVO.InsightItem;
import com.personalhub.module.dashboard.vo.StatsVO.NamedStat;
import com.personalhub.module.dashboard.vo.TrendVO;
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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

        trends.setStudyTrend(toTrendPoints(dashboardMapper.selectStudyTrend(userId, since)));
        trends.setNoteTrend(toTrendPoints(dashboardMapper.selectNoteTrend(userId, since)));
        trends.setTodoTrend(toTrendPoints(dashboardMapper.selectTodoTrend(userId, since)));
        trends.setReadingTrend(toTrendPoints(dashboardMapper.selectReadingTrend(userId, since)));

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

    @Override
    public StatsVO getDetailedStats(Long userId, int days) {
        StatsVO vo = new StatsVO();
        LocalDate now = LocalDate.now();
        LocalDate since = now.minusDays(days);

        // ========== 1. KPI 数据 ==========
        long noteCount = countNotes(userId);
        long readingMinutes = sumReadingDuration(userId);
        long todoDone = countTodos(userId, 1);
        long todoTotal = countTodos(userId, null);
        long todoPend = countTodos(userId, 0);
        long todoOver = countOverdueTodos(userId);

        vo.setNoteCount(noteCount);
        vo.setReadingHours(Math.round(readingMinutes / 6.0) / 10.0); // 分钟转小时，保留1位小数
        vo.setTodoCompletionRate(todoTotal > 0 ? Math.round((double) todoDone / todoTotal * 1000) / 10.0 : 0);
        vo.setTodoDone(todoDone);
        vo.setTodoPending(todoPend);
        vo.setTodoOverdue(todoOver);

        // 连续天数
        List<LocalDate> activeDates = dashboardMapper.selectActiveDates(userId);
        vo.setStreakDays(calcStreak(activeDates, now));
        vo.setBestStreakDays(calcBestStreak(activeDates));

        // ========== 2. 周环比 ==========
        LocalDateTime weekStart = now.atStartOfDay();
        LocalDateTime prevWeekStart = weekStart.minusDays(7);

        long noteThisWeek = getCountOrZero(dashboardMapper.countCreatedInRange(userId, prevWeekStart, weekStart));
        long noteLastWeek = getCountOrZero(dashboardMapper.countCreatedInRange(userId, prevWeekStart.minusDays(7), prevWeekStart));
        vo.setNoteCountChange(calcPercentChange(noteThisWeek, noteLastWeek));

        long readingMinThisWeek = getCountOrZero(dashboardMapper.sumReadingDurationInRange(userId, prevWeekStart, weekStart));
        long readingMinLastWeek = getCountOrZero(dashboardMapper.sumReadingDurationInRange(userId, prevWeekStart.minusDays(7), prevWeekStart));
        vo.setReadingHoursChange(calcPercentChange(readingMinThisWeek, readingMinLastWeek));

        long todoDoneThisWeek = getCountOrZero(dashboardMapper.countTodosDoneInRange(userId, prevWeekStart, weekStart));
        long todoTotalThisWeek = getCountOrZero(dashboardMapper.countTodosTotalInRange(userId, prevWeekStart, weekStart));
        long todoDoneLastWeek = getCountOrZero(dashboardMapper.countTodosDoneInRange(userId, prevWeekStart.minusDays(7), prevWeekStart));
        long todoTotalLastWeek = getCountOrZero(dashboardMapper.countTodosTotalInRange(userId, prevWeekStart.minusDays(7), prevWeekStart));
        double rateThis = todoTotalThisWeek > 0 ? (double) todoDoneThisWeek / todoTotalThisWeek : 0;
        double rateLast = todoTotalLastWeek > 0 ? (double) todoDoneLastWeek / todoTotalLastWeek : 0;
        vo.setTodoCompletionChange(Math.round((rateThis - rateLast) * 1000) / 10.0);

        // ========== 3. 趋势数据 ==========
        vo.setStudyTrend(queryDataPoints(dashboardMapper.selectStudyTrend(userId, since)));
        vo.setNoteTrend(queryDataPoints(dashboardMapper.selectNoteTrend(userId, since)));

        // 笔记辅助统计
        List<DataPoint> noteTrend = vo.getNoteTrend();
        if (!noteTrend.isEmpty()) {
            long totalNotes = noteTrend.stream().mapToLong(DataPoint::getValue).sum();
            vo.setAvgDailyNotes(Math.round((double) totalNotes / Math.max(noteTrend.size(), 1) * 100) / 100.0);
            vo.setMaxDailyNotes(noteTrend.stream().mapToLong(DataPoint::getValue).max().orElse(0));
            vo.setMinDailyNotes(noteTrend.stream().mapToLong(DataPoint::getValue).filter(v -> v > 0).min().orElse(0));
        }

        // ========== 5. 分类/标签统计 ==========
        vo.setCategoryStats(queryNamedStats(dashboardMapper.selectCategoryStats(userId, 8)));
        vo.setTagStats(queryTagStats(dashboardMapper.selectTagStats(userId, 8)));

        // ========== 6. 最近活动 ==========
        List<Map<String, Object>> activityRows = dashboardMapper.selectRecentActivity(userId, 20);
        vo.setRecentActivity(activityRows.stream().map(row -> {
            ActivityItem item = new ActivityItem();
            item.setId(((Number) row.get("id")).longValue());
            item.setModule((String) row.get("module"));
            item.setAction((String) row.get("action"));
            item.setContent((String) row.get("content"));
            Object createdAt = row.get("created_at");
            if (createdAt != null) {
                LocalDateTime dt = (LocalDateTime) createdAt;
                item.setCreatedAt(dt);
                item.setTimeLabel(formatRelativeTime(dt));
            }
            return item;
        }).collect(Collectors.toList()));

        // ========== 7. 学习洞察 ==========
        vo.setInsights(buildInsights(userId, vo, activeDates, now));

        return vo;
    }

    /** 计算连续学习天数 */
    private int calcStreak(List<LocalDate> activeDates, LocalDate today) {
        if (activeDates == null || activeDates.isEmpty()) return 0;
        int streak = 0;
        LocalDate expected = today;
        for (LocalDate date : activeDates) {
            if (date.equals(expected) || date.equals(expected.minusDays(1))) {
                streak++;
                expected = date;
            } else if (date.isBefore(expected.minusDays(1))) {
                break;
            }
        }
        return streak;
    }

    /** 计算最长连续天数 */
    private int calcBestStreak(List<LocalDate> activeDates) {
        if (activeDates == null || activeDates.isEmpty()) return 0;
        List<LocalDate> asc = new ArrayList<>(activeDates);
        asc.sort(Comparator.naturalOrder());
        int best = 1;
        int current = 1;
        for (int i = 1; i < asc.size(); i++) {
            if (asc.get(i).minusDays(1).equals(asc.get(i - 1))) {
                current++;
                best = Math.max(best, current);
            } else {
                current = 1;
            }
        }
        return best;
    }

    /** 构建洞察列表 */
    private List<InsightItem> buildInsights(Long userId, StatsVO vo, List<LocalDate> activeDates, LocalDate now) {
        List<InsightItem> insights = new ArrayList<>();

        // 效率洞察
        Long noteChange = vo.getNoteCountChange();
        if (noteChange != null && Math.abs(noteChange) >= 5) {
            InsightItem item = new InsightItem();
            item.setType("efficiency");
            item.setIcon(noteChange > 0 ? "📈" : "📉");
            item.setTitle("本周学习效率" + (noteChange > 0 ? "提升" : "下降") + " " + Math.abs(noteChange) + "%");
            item.setDescription(noteChange > 0 ? "笔记新增量较上周明显增长" : "笔记新增量较上周有所减少");
            insights.add(item);
        }

        // 连续学习
        if (vo.getStreakDays() >= 3) {
            InsightItem item = new InsightItem();
            item.setType("streak");
            item.setIcon("🔥");
            item.setTitle("连续学习 " + vo.getStreakDays() + " 天");
            item.setDescription(vo.getBestStreakDays() > vo.getStreakDays()
                    ? "离最长纪录 " + vo.getBestStreakDays() + " 天还差 " + (vo.getBestStreakDays() - vo.getStreakDays()) + " 天"
                    : "已追平历史最长纪录！");
            insights.add(item);
        }

        // 分类占比
        List<NamedStat> cats = vo.getCategoryStats();
        if (cats != null && !cats.isEmpty()) {
            InsightItem item = new InsightItem();
            item.setType("category");
            item.setIcon("📖");
            long total = cats.stream().mapToLong(NamedStat::getCount).sum();
            NamedStat top = cats.get(0);
            int percent = total > 0 ? (int) Math.round((double) top.getCount() / total * 100) : 0;
            item.setTitle(top.getName() + " 占学习内容 " + percent + "%");
            item.setDescription("分类「" + top.getName() + "」笔记最多，共 " + top.getCount() + " 篇");
            insights.add(item);
        }

        // 星期效率
        List<Map<String, Object>> weekdayDist = dashboardMapper.selectStudyWeekdayDistribution(userId);
        if (weekdayDist != null && !weekdayDist.isEmpty()) {
            String[] weekdays = {"", "周日", "周一", "周二", "周三", "周四", "周五", "周六"};
            int bestDay = ((Number) weekdayDist.get(0).get("weekday")).intValue();
            String dayName = bestDay >= 1 && bestDay <= 7 ? weekdays[bestDay] : "某天";
            InsightItem item = new InsightItem();
            item.setType("day");
            item.setIcon("⭐");
            item.setTitle(dayName + " 效率最高");
            item.setDescription(dayName + "的学习记录最多，是高效学习日");
            insights.add(item);
        }

        // Todo 完成率
        double rate = vo.getTodoCompletionRate();
        if (rate > 0) {
            InsightItem item = new InsightItem();
            item.setType("todo");
            item.setIcon("🎯");
            item.setTitle("Todo 完成率 " + (int) Math.round(rate) + "%");
            if (rate >= 80) {
                item.setDescription("执行力很强，继续保持！");
            } else if (rate >= 50) {
                item.setDescription("多数任务已完成，继续推进剩余任务");
            } else {
                item.setDescription("还有不少待办未完成，加油！");
            }
            insights.add(item);
        }

        // 阅读洞察
        double readingHours = vo.getReadingHours() != null ? vo.getReadingHours() : 0;
        if (readingHours > 0) {
            InsightItem item = new InsightItem();
            item.setType("reading");
            item.setIcon("📚");
            String hoursStr = readingHours >= 10 ? String.valueOf((int) Math.round(readingHours)) : String.valueOf(readingHours);
            item.setTitle("阅读总时长 " + hoursStr + " 小时");
            item.setDescription("累计阅读 " + readingHours + " 小时，拓展知识边界");
            insights.add(item);
        }

        return insights;
    }

    /** 计算百分比变化 */
    private Long calcPercentChange(long current, long previous) {
        if (previous == 0 && current == 0) return 0L;
        if (previous == 0) return 100L;
        return Math.round((double) (current - previous) / previous * 100);
    }

    /** 安全获取计数 */
    private long getCountOrZero(Long value) {
        return value != null ? value : 0L;
    }

    /** Map → DataPoint */
    private List<DataPoint> queryDataPoints(List<Map<String, Object>> rows) {
        return rows.stream().map(row -> new DataPoint(
                String.valueOf(row.get("date_str")),
                row.get("value") instanceof Number ? ((Number) row.get("value")).longValue() : 0L))
                .collect(Collectors.toList());
    }

    /** Map → NamedStat（分类） */
    private List<NamedStat> queryNamedStats(List<Map<String, Object>> rows) {
        return rows.stream().map(row -> {
            NamedStat stat = new NamedStat();
            stat.setName((String) row.get("name"));
            stat.setCount(row.get("cnt") instanceof Number ? ((Number) row.get("cnt")).longValue() : 0L);
            return stat;
        }).collect(Collectors.toList());
    }

    /** Map → NamedStat（标签，带颜色） */
    private List<NamedStat> queryTagStats(List<Map<String, Object>> rows) {
        return rows.stream().map(row -> {
            NamedStat stat = new NamedStat();
            stat.setName((String) row.get("name"));
            stat.setCount(row.get("cnt") instanceof Number ? ((Number) row.get("cnt")).longValue() : 0L);
            stat.setColor((String) row.get("color"));
            return stat;
        }).collect(Collectors.toList());
    }

    /** 格式化相对时间 */
    private String formatRelativeTime(LocalDateTime dt) {
        LocalDateTime now = LocalDateTime.now();
        long diffMinutes = java.time.Duration.between(dt, now).toMinutes();
        if (diffMinutes < 1) return "刚刚";
        if (diffMinutes < 60) return diffMinutes + " 分钟前";
        long diffHours = diffMinutes / 60;
        if (diffHours < 24) return diffHours + " 小时前";
        long diffDays = diffHours / 24;
        if (diffDays < 7) return diffDays + " 天前";
        return dt.toLocalDate().toString();
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

    private List<TrendVO.DataPoint> toTrendPoints(List<Map<String, Object>> rows) {
        return rows.stream().map(row -> new TrendVO.DataPoint(
                String.valueOf(row.get("date_str")),
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
