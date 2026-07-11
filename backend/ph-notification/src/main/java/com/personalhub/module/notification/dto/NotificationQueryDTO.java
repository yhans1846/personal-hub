package com.personalhub.module.notification.dto;

import com.personalhub.common.result.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class NotificationQueryDTO extends PageParam {
    // 暂时无额外筛选条件，未读优先排序在 Service 中固定
}
