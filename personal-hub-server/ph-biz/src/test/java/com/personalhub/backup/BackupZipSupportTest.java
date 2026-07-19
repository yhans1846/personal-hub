package com.personalhub.backup;

import com.personalhub.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BackupZipSupportTest {

    @Test
    void rejectPathTraversal() {
        assertThrows(BusinessException.class,
                () -> BackupZipSupport.assertSafeEntryName("../etc/passwd"));
        assertThrows(BusinessException.class,
                () -> BackupZipSupport.assertSafeEntryName("files/notes/../../x"));
        assertThrows(BusinessException.class,
                () -> BackupZipSupport.assertSafeEntryName("/absolute"));
    }

    @Test
    void acceptAllowed() {
        assertDoesNotThrow(() -> BackupZipSupport.assertSafeEntryName("manifest.json"));
        assertDoesNotThrow(() -> BackupZipSupport.assertSafeEntryName("data/notes.json"));
        assertDoesNotThrow(() -> BackupZipSupport.assertSafeEntryName("files/notes/1/note.md"));
        assertDoesNotThrow(() -> BackupZipSupport.assertSafeEntryName("files/diaries/2/images/a.png"));
        assertDoesNotThrow(() -> BackupZipSupport.assertSafeEntryName("files/uploads/doc/2026/x.pdf"));
    }

    @Test
    void rejectUnknownPrefix() {
        assertThrows(BusinessException.class,
                () -> BackupZipSupport.assertSafeEntryName("files/secret/a.png"));
        assertThrows(BusinessException.class,
                () -> BackupZipSupport.assertSafeEntryName("secret.bin"));
    }

    @Test
    void acceptAvatar() {
        assertDoesNotThrow(() -> BackupZipSupport.assertSafeEntryName("files/avatars/a.png"));
    }

    @Test
    void toStorageRelative() {
        assertEquals("notes/1/note.md",
                BackupZipSupport.toStorageRelative("files/notes/1/note.md"));
    }
}
