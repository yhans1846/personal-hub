package com.personalhub.planning.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TodoPriorityTest {

    @Test
    void labelOf_null() {
        assertNull(TodoPriority.labelOf(null));
    }

    @Test
    void labelOf_known() {
        assertEquals("高", TodoPriority.labelOf(1));
    }

    @Test
    void labelOf_unknown() {
        assertEquals("未知", TodoPriority.labelOf(99));
    }
}
