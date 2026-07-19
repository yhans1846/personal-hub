package com.personalhub.knowledge.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class NoteDeleteReasonTest {

    @Test
    void labelOf_known() {
        assertEquals("用户删除", NoteDeleteReason.labelOf(NoteDeleteReason.USER_DELETE.getCode()));
        assertEquals("归档", NoteDeleteReason.labelOf(NoteDeleteReason.AUTO_ARCHIVE.getCode()));
    }

    @Test
    void labelOf_null() {
        assertNull(NoteDeleteReason.labelOf(null));
    }

    @Test
    void labelOf_unknown_passthrough() {
        assertEquals("OTHER", NoteDeleteReason.labelOf("OTHER"));
    }
}
