package com.personalhub.system.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CaptchaServiceTest {

    @Test
    void withinTolerance_acceptsExactSlot() {
        assertTrue(CaptchaService.withinTolerance(2, 2));
        assertTrue(CaptchaService.withinTolerance(0, 0));
    }

    @Test
    void withinTolerance_rejectsWrongSlot() {
        assertFalse(CaptchaService.withinTolerance(2, 3));
        assertFalse(CaptchaService.withinTolerance(1, 0));
    }
}
