package com.personalhub.backup;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BackupRetentionTest {

    @Test
    void keepsSevenOkAndThreeFailed() {
        List<UserBackup> all = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            all.add(UserBackup.builder().id((long) i).status("OK")
                    .createdAt(LocalDateTime.now().minusHours(i)).build());
        }
        for (int i = 0; i < 5; i++) {
            all.add(UserBackup.builder().id(100L + i).status("FAILED")
                    .createdAt(LocalDateTime.now().minusHours(i)).build());
        }
        List<UserBackup> remove = BackupRetention.selectToDelete(all);
        assertEquals(3 + 2, remove.size());
        assertTrue(remove.stream().filter(b -> "OK".equals(b.getStatus())).count() == 3);
        assertTrue(remove.stream().filter(b -> "FAILED".equals(b.getStatus())).count() == 2);
    }
}
