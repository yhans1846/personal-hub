package com.personalhub.module.notification.controller;

import com.personalhub.common.result.PageResult;
import com.personalhub.common.result.Result;
import com.personalhub.module.notification.dto.NotificationQueryDTO;
import com.personalhub.module.notification.service.NotificationService;
import com.personalhub.module.notification.vo.NotificationVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "系统通知")
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "通知列表（分页，未读优先）")
    @GetMapping
    public Result<PageResult<NotificationVO>> list(Authentication auth, NotificationQueryDTO query) {
        Long userId = Long.valueOf(auth.getName());
        return Result.success(PageResult.of(notificationService.list(userId, query)));
    }

    @Operation(summary = "未读通知数")
    @GetMapping("/unread-count")
    public Result<Long> getUnreadCount(Authentication auth) {
        Long userId = Long.valueOf(auth.getName());
        return Result.success(notificationService.getUnreadCount(userId));
    }

    @Operation(summary = "标记已读（支持批量）")
    @PutMapping("/read")
    public Result<Void> markAsRead(Authentication auth, @RequestBody List<Long> ids) {
        Long userId = Long.valueOf(auth.getName());
        notificationService.markAsRead(userId, ids);
        return Result.success();
    }

    @Operation(summary = "全部标记已读")
    @PutMapping("/read-all")
    public Result<Void> markAllAsRead(Authentication auth) {
        Long userId = Long.valueOf(auth.getName());
        notificationService.markAllAsRead(userId);
        return Result.success();
    }

    @Operation(summary = "清空所有通知")
    @DeleteMapping
    public Result<Void> clearAll(Authentication auth) {
        Long userId = Long.valueOf(auth.getName());
        notificationService.clearAll(userId);
        return Result.success();
    }

    @Operation(summary = "触发系统通知检测生成")
    @PostMapping("/check")
    public Result<Void> checkNotifications(Authentication auth) {
        Long userId = Long.valueOf(auth.getName());
        notificationService.generateSystemNotifications(userId);
        return Result.success();
    }
}
