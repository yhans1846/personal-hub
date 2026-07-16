package com.personalhub.knowledge.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ReadingStatusTest {

    @Test
    void labelOf_null() {
        assertNull(ReadingStatus.labelOf(null));
    }

    @Test
    void labelOf_known() {
        assertEquals("在读", ReadingStatus.labelOf(1));
    }

    @Test
    void labelOf_unknown() {
        assertEquals("未知", ReadingStatus.labelOf(99));
    }
}
