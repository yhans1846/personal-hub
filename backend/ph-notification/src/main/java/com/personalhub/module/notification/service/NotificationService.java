package com.personalhub.module.notification.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.personalhub.module.notification.vo.NotificationVO;
import com.personalhub.module.notification.dto.NotificationQueryDTO;

import java.util.List;

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
