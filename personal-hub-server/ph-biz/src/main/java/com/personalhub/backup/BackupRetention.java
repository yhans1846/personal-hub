package com.personalhub.backup;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 备份保留策略（可单测）
 */
public final class BackupRetention {

    public static final int KEEP_OK = 7;
    public static final int KEEP_FAILED = 3;

    private BackupRetention() {
    }

    /**
     * @return 应删除的记录（已按应删顺序）
     */
    public static List<UserBackup> selectToDelete(List<UserBackup> all) {
        List<UserBackup> ok = all.stream()
                .filter(b -> "OK".equals(b.getStatus()))
                .sorted(Comparator.comparing(UserBackup::getCreatedAt).reversed())
                .toList();
        List<UserBackup> failed = all.stream()
                .filter(b -> "FAILED".equals(b.getStatus()))
                .sorted(Comparator.comparing(UserBackup::getCreatedAt).reversed())
                .toList();

        List<UserBackup> remove = new ArrayList<>();
        if (ok.size() > KEEP_OK) {
            remove.addAll(ok.subList(KEEP_OK, ok.size()));
        }
        if (failed.size() > KEEP_FAILED) {
            remove.addAll(failed.subList(KEEP_FAILED, failed.size()));
        }
        return remove;
    }
}
