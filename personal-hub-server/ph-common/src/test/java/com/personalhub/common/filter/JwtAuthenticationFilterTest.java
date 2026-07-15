package com.personalhub.common.filter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtAuthenticationFilterTest {

    @Test
    void allowQueryToken_forNoteImagesAndAttachments() {
        assertTrue(JwtAuthenticationFilter.allowQueryToken("/api/notes/12/images/a.png"));
        assertTrue(JwtAuthenticationFilter.allowQueryToken("/api/notes/12/attachments/doc.pdf"));
    }

    @Test
    void denyQueryToken_forNoteApisAndOthers() {
        assertFalse(JwtAuthenticationFilter.allowQueryToken("/api/notes/12"));
        assertFalse(JwtAuthenticationFilter.allowQueryToken("/api/notes/12/preview"));
        assertFalse(JwtAuthenticationFilter.allowQueryToken("/api/notes/12/export"));
        assertFalse(JwtAuthenticationFilter.allowQueryToken("/api/auth/login"));
        assertFalse(JwtAuthenticationFilter.allowQueryToken(null));
    }
}
