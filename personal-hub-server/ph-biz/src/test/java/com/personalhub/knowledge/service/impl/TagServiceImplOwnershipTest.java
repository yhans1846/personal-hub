package com.personalhub.knowledge.service.impl;

import com.personalhub.common.exception.NotFoundException;
import com.personalhub.knowledge.entity.Note;
import com.personalhub.knowledge.entity.Tag;
import com.personalhub.knowledge.entity.TagRel;
import com.personalhub.knowledge.mapper.NoteMapper;
import com.personalhub.knowledge.mapper.TagMapper;
import com.personalhub.knowledge.mapper.TagRelMapper;
import com.personalhub.planning.mapper.StudyPlanMapper;
import com.personalhub.resource.mapper.BookmarkUrlMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TagServiceImplOwnershipTest {

    @Mock TagMapper tagMapper;
    @Mock TagRelMapper tagRelMapper;
    @Mock NoteMapper noteMapper;
    @Mock BookmarkUrlMapper bookmarkUrlMapper;
    @Mock StudyPlanMapper studyPlanMapper;

    TagServiceImpl tagService;

    @BeforeEach
    void setUp() {
        tagService = new TagServiceImpl(
                tagMapper, tagRelMapper, noteMapper, bookmarkUrlMapper, studyPlanMapper);
    }

    @Test
    void bindTag_foreignTag_rejected() {
        Tag foreign = Tag.builder().id(9L).userId(2L).name("x").build();
        when(tagMapper.selectById(9L)).thenReturn(foreign);

        assertThrows(NotFoundException.class,
                () -> tagService.bindTag(1L, 9L, "note", 100L));
        verify(tagRelMapper, never()).insert(isA(TagRel.class));
    }

    @Test
    void bindTag_foreignNote_rejected() {
        Tag own = Tag.builder().id(9L).userId(1L).name("x").build();
        when(tagMapper.selectById(9L)).thenReturn(own);
        when(noteMapper.selectById(100L)).thenReturn(Note.builder().id(100L).userId(2L).build());

        assertThrows(NotFoundException.class,
                () -> tagService.bindTag(1L, 9L, "note", 100L));
        verify(tagRelMapper, never()).insert(isA(TagRel.class));
    }
}
