package com.personalhub.common.util;

import com.personalhub.common.exception.NotFoundException;

import java.util.Objects;
import java.util.function.Function;

/**
 * 实体归属校验：不存在或不属于当前用户时统一抛 NotFoundException。
 */
public final class EntityGuard {

    private EntityGuard() {
    }

    /**
     * @param entity       查询结果，可为 null
     * @param userId       当前用户 ID
     * @param userIdGetter 从实体取归属用户 ID
     * @param message      对外提示（统一「不存在」语义）
     * @param <T>          实体类型
     * @return 非空且归属正确的实体
     */
    public static <T> T requireOwned(
            T entity,
            Long userId,
            Function<T, Long> userIdGetter,
            String message) {
        if (entity == null || !Objects.equals(userIdGetter.apply(entity), userId)) {
            throw new NotFoundException(message);
        }
        return entity;
    }
}
