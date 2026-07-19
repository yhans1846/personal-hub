package com.personalhub.common.util;

import com.personalhub.common.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FilenameGuardTest {

    @Test
    void requireSafe_ok() {
        assertEquals("a.png", FilenameGuard.requireSafe("a.png"));
    }

    @Test
    void requireSafe_null_rejected() {
        assertThrows(BusinessException.class, () -> FilenameGuard.requireSafe(null));
    }

    @Test
    void requireSafe_dotDot_rejected() {
        assertThrows(BusinessException.class, () -> FilenameGuard.requireSafe("../x.png"));
    }

    @Test
    void requireSafe_slash_rejected() {
        assertThrows(BusinessException.class, () -> FilenameGuard.requireSafe("a/b.png"));
        assertThrows(BusinessException.class, () -> FilenameGuard.requireSafe("a\\b.png"));
    }
}
