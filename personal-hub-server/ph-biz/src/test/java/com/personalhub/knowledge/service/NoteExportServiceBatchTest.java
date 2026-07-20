package com.personalhub.knowledge.service;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 批量导出：校验与目录名冲突处理（纯逻辑）。
 */
class NoteExportServiceBatchTest {

    @Test
    void normalizeIds_rejectsEmpty() {
        assertThrows(IllegalArgumentException.class, () -> NoteExportService.normalizeIds(List.of()));
        assertThrows(IllegalArgumentException.class, () -> NoteExportService.normalizeIds(null));
    }

    @Test
    void normalizeIds_rejectsOverLimit() {
        List<Long> ids = java.util.stream.LongStream.rangeClosed(1, 51).boxed().toList();
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> NoteExportService.normalizeIds(ids));
        assertTrue(ex.getMessage().contains("50"));
    }

    @Test
    void normalizeIds_dedupesAndKeepsOrder() {
        java.util.ArrayList<Long> input = new java.util.ArrayList<>();
        input.add(3L);
        input.add(1L);
        input.add(3L);
        input.add(2L);
        input.add(null);
        assertEquals(List.of(3L, 1L, 2L), NoteExportService.normalizeIds(input));
    }

    @Test
    void uniqueFolderName_appendsIdOnCollision() {
        Set<String> used = new HashSet<>();
        assertEquals("Spring", NoteExportService.uniqueFolderName("Spring", 1L, used));
        assertEquals("Spring (2)", NoteExportService.uniqueFolderName("Spring", 2L, used));
        assertEquals("untitled", NoteExportService.uniqueFolderName(null, 3L, used));
    }

    @Test
    void sanitizeFileName_stripsIllegalChars() {
        assertEquals("a_b_c", NoteExportService.sanitizeFileName("a/b:c"));
        assertEquals("untitled", NoteExportService.sanitizeFileName(null));
    }
}
