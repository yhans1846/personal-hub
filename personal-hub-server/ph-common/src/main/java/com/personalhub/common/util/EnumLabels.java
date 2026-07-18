package com.personalhub.common.util;

import java.util.function.Function;
import java.util.function.ToIntFunction;

/**
 * 带整型 code + 中文 label 的枚举查找。
 */
public final class EnumLabels {

    private EnumLabels() {
    }

    /**
     * @param code    可为 null
     * @param values  枚举 values()
     * @param codeFn  取 code
     * @param labelFn 取 label
     * @return null → null；匹配 → label；未知 →「未知」
     */
    public static <E> String labelOf(
            Integer code,
            E[] values,
            ToIntFunction<E> codeFn,
            Function<E, String> labelFn) {
        if (code == null) {
            return null;
        }
        for (E value : values) {
            if (codeFn.applyAsInt(value) == code) {
                return labelFn.apply(value);
            }
        }
        return "未知";
    }
}
