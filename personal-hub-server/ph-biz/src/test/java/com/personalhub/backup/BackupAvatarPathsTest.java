package com.personalhub.backup;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class BackupAvatarPathsTest {

    @Test
    void parseApiUrl() {
        assertEquals("avatars/abc.png",
                BackupAvatarPaths.toStoragePath("/api/files/avatar/abc.png"));
    }

    @Test
    void rejectBad() {
        assertNull(BackupAvatarPaths.toStoragePath(null));
        assertNull(BackupAvatarPaths.toStoragePath(""));
        assertNull(BackupAvatarPaths.toStoragePath("/api/files/other/x.png"));
        assertNull(BackupAvatarPaths.toStoragePath("/api/files/avatar/../x.png"));
        assertNull(BackupAvatarPaths.toStoragePath("/api/files/avatar/a/b.png"));
    }
}
