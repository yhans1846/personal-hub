package com.personalhub.knowledge.service.impl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NoteServiceExcerptTest {

    @Test
    void buildExcerpt_stripsMarkdownAndKeepsText() {
        String content = "# Title\n\n**Bold** text with `code`";
        String excerpt = NoteServiceImpl.buildExcerpt(content);
        assertTrue(excerpt.contains("Title"));
        assertTrue(excerpt.contains("Bold"));
        assertFalse(excerpt.contains("**"));
    }

    @Test
    void buildExcerpt_emptyContent() {
        assertEquals("", NoteServiceImpl.buildExcerpt(null));
        assertEquals("", NoteServiceImpl.buildExcerpt("   "));
    }
}
