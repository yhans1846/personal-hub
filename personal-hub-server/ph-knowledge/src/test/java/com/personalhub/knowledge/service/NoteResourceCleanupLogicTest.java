package com.personalhub.knowledge.service;

import com.personalhub.knowledge.entity.Note;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 清理任务决策：仅当笔记行不存在时删除目录；软删除保留。
 */
class NoteResourceCleanupLogicTest {

    static boolean shouldDeleteDir(Note note) {
        return note == null;
    }

    @Test
    void keepSoftDeletedNoteDirectory() {
        Note softDeleted = new Note();
        softDeleted.setId(1L);
        softDeleted.setIsDeleted(1);
        assertFalse(shouldDeleteDir(softDeleted));
    }

    @Test
    void deleteOnlyWhenRowMissing() {
        assertTrue(shouldDeleteDir(null));
    }
}
