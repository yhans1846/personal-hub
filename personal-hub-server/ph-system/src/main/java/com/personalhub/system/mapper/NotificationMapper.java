package com.personalhub.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.personalhub.system.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户通知 Mapper
 */
@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {

    /**
     * 标记所有通知为已读
     */
    void markAllAsReadXml(@Param("userId") Long userId);

    /**
     * 清空用户通知
     */
    void clearAllXml(@Param("userId") Long userId);

    /**
     * 查询所有用户ID
     */
    List<Long> selectAllUserIds();
}
