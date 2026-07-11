package com.personalhub.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.personalhub.system.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {

    /** 标记所有通知为已读 */
    int markAllAsReadXml(@Param("userId") Long userId);

    /** 清空用户通知 */
    int clearAllXml(@Param("userId") Long userId);

    /** 查询过期未完成的待办 */
    List<Map<String, Object>> selectOverdueTodos(@Param("userId") Long userId, @Param("today") LocalDate today);

    /** 查询即将截止的学习计划 */
    List<Map<String, Object>> selectDeadlinePlans(@Param("userId") Long userId, @Param("today") LocalDate today, @Param("deadline") LocalDate deadline);

    /** 查询已完成的学习计划 */
    List<Map<String, Object>> selectCompletedPlans(@Param("userId") Long userId);

    /** 查询所有用户ID */
    List<Long> selectAllUserIds();
}
