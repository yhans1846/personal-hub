package com.personalhub.planning.notification;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.personalhub.common.constant.Flags;
import com.personalhub.planning.entity.StudyPlan;
import com.personalhub.planning.entity.TodoTask;
import com.personalhub.planning.enums.StudyPlanStatus;
import com.personalhub.planning.mapper.StudyPlanMapper;
import com.personalhub.planning.mapper.TodoTaskMapper;
import com.personalhub.system.notification.PlanningNotificationCandidate;
import com.personalhub.system.notification.PlanningNotificationSource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * 规划域通知扫描实现
 */
@Service
@RequiredArgsConstructor
public class PlanningNotificationSourceImpl implements PlanningNotificationSource {

    private final TodoTaskMapper todoTaskMapper;
    private final StudyPlanMapper studyPlanMapper;

    @Override
    public List<PlanningNotificationCandidate> findOverdueTodos(Long userId, LocalDate today) {
        return todoTaskMapper.selectList(new LambdaQueryWrapper<TodoTask>()
                        .eq(TodoTask::getUserId, userId)
                        .eq(TodoTask::getIsDone, Flags.NO)
                        .lt(TodoTask::getDueDate, today))
                .stream()
                .map(t -> PlanningNotificationCandidate.builder()
                        .relatedId(t.getId())
                        .title(t.getTitle())
                        .build())
                .toList();
    }

    @Override
    public List<PlanningNotificationCandidate> findDeadlinePlans(Long userId, LocalDate today, LocalDate deadline) {
        return studyPlanMapper.selectList(new LambdaQueryWrapper<StudyPlan>()
                        .eq(StudyPlan::getUserId, userId)
                        .notIn(StudyPlan::getStatus,
                                StudyPlanStatus.COMPLETED.getCode(),
                                StudyPlanStatus.PAUSED.getCode())
                        .between(StudyPlan::getEndDate, today, deadline))
                .stream()
                .map(p -> PlanningNotificationCandidate.builder()
                        .relatedId(p.getId())
                        .title(p.getName())
                        .build())
                .toList();
    }

    @Override
    public List<PlanningNotificationCandidate> findCompletedPlans(Long userId) {
        return studyPlanMapper.selectList(new LambdaQueryWrapper<StudyPlan>()
                        .eq(StudyPlan::getUserId, userId)
                        .eq(StudyPlan::getStatus, StudyPlanStatus.COMPLETED.getCode()))
                .stream()
                .map(p -> PlanningNotificationCandidate.builder()
                        .relatedId(p.getId())
                        .title(p.getName())
                        .build())
                .toList();
    }
}
