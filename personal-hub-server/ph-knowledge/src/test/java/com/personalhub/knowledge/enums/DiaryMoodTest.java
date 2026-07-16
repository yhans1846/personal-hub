package com.personalhub.knowledge.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DiaryMoodTest {

    @Test
    void labelOf_null() {
        assertNull(DiaryMood.labelOf(null));
    }

    @Test
    void labelOf_known() {
        assertEquals("很好", DiaryMood.labelOf(1));
    }

    @Test
    void labelOf_unknown() {
        assertEquals("未知", DiaryMood.labelOf(99));
    }
}
