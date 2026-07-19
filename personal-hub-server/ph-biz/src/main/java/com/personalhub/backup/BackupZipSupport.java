package com.personalhub.backup;

import com.personalhub.common.exception.BusinessException;

/**
 * 备份 ZIP 条目路径校验。
 */
public final class BackupZipSupport {

    private BackupZipSupport() {
    }

    /**
     * 校验 ZIP 条目名：禁止穿越，仅允许清单、data/、files 下约定前缀。
     */
    public static void assertSafeEntryName(String name) {
        if (name == null || name.isBlank()) {
            throw new BusinessException("非法备份条目");
        }
        String n = name.replace('\\', '/');
        if (n.startsWith("/") || n.contains("..")) {
            throw new BusinessException("非法备份路径: " + name);
        }
        if ("manifest.json".equals(n)) {
            return;
        }
        if (n.startsWith("data/") && n.length() > "data/".length()) {
            return;
        }
        if (n.startsWith("files/notes/")
                || n.startsWith("files/diaries/")
                || n.startsWith("files/uploads/")
                || n.startsWith("files/avatars/")) {
            return;
        }
        // 允许目录条目本身
        if ("data/".equals(n) || "files/".equals(n)
                || "files/notes/".equals(n)
                || "files/diaries/".equals(n)
                || "files/uploads/".equals(n)
                || "files/avatars/".equals(n)) {
            return;
        }
        throw new BusinessException("非法备份路径: " + name);
    }

    /**
     * ZIP 内 files/ 前缀映射为 storage 相对路径。
     */
    public static String toStorageRelative(String zipEntryName) {
        assertSafeEntryName(zipEntryName);
        String n = zipEntryName.replace('\\', '/');
        if (!n.startsWith("files/")) {
            throw new BusinessException("非文件条目: " + zipEntryName);
        }
        return n.substring("files/".length());
    }
}
