package com.personalhub.system.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 规划域扫描出的待生成通知候选。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanningNotificationCandidate {

    /** 关联业务 ID */
    private Long relatedId;

    /** 标题/名称（用于通知文案） */
    private String title;
}
