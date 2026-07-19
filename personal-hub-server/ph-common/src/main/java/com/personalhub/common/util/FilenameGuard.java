package com.personalhub.common.util;

import com.personalhub.common.exception.BusinessException;

/**
 * 文件名安全校验：拒绝路径分隔符与穿越片段。
 */
public final class FilenameGuard {

    private FilenameGuard() {
    }

    /**
     * @param filename 裸文件名（不含目录）
     * @return 校验通过的文件名
     */
    public static String requireSafe(String filename) {
        if (filename == null || filename.isBlank()
                || filename.contains("..")
                || filename.contains("/")
                || filename.contains("\\")) {
            throw new BusinessException("非法文件名");
        }
        return filename;
    }
}
