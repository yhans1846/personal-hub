package com.personalhub.planning.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class StudyPlanStatusTest {

    @Test
    void labelOf_null() {
        assertNull(StudyPlanStatus.labelOf(null));
    }

    @Test
    void labelOf_known() {
        assertEquals("学习中", StudyPlanStatus.labelOf(1));
    }

    @Test
    void labelOf_unknown() {
        assertEquals("未知", StudyPlanStatus.labelOf(99));
    }
}
