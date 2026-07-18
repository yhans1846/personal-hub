package com.personalhub.system.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.personalhub.system.dto.NotificationQueryDTO;
import com.personalhub.system.vo.NotificationVO;

import java.util.List;


/**
 * 用户通知服务
 */
public interface NotificationService {

    IPage<NotificationVO> list(Long userId, NotificationQueryDTO query);

    long getUnreadCount(Long userId);

    void markAsRead(Long userId, List<Long> ids);

    void markAllAsRead(Long userId);

    void clearAll(Long userId);

    void create(Long userId, String type, String title, String content,
                Long relatedId, String relatedType);

    void generateSystemNotifications(Long userId);
}
