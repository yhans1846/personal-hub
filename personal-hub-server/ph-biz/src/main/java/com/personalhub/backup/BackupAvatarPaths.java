package com.personalhub.backup;

import com.personalhub.storage.StoragePaths;

/**
 * 头像 URL ↔ 存储相对路径。
 */
public final class BackupAvatarPaths {

    private BackupAvatarPaths() {
    }

    /**
     * @param avatarUrl 如 {@code /api/files/avatar/xxx.png}
     * @return {@code avatars/xxx.png}，无法解析则 null
     */
    public static String toStoragePath(String avatarUrl) {
        if (avatarUrl == null || avatarUrl.isBlank()) {
            return null;
        }
        String marker = "/avatar/";
        int idx = avatarUrl.lastIndexOf(marker);
        if (idx < 0) {
            return null;
        }
        String filename = avatarUrl.substring(idx + marker.length()).trim();
        if (filename.isEmpty() || filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            return null;
        }
        return StoragePaths.avatar(filename);
    }
}
