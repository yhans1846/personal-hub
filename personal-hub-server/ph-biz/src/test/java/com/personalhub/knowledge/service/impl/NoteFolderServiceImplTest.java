package com.personalhub.knowledge.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.personalhub.common.exception.BusinessException;
import com.personalhub.knowledge.dto.NoteFolderMoveDTO;
import com.personalhub.knowledge.entity.NoteFolder;
import com.personalhub.knowledge.mapper.NoteFolderMapper;
import com.personalhub.knowledge.mapper.NoteMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoteFolderServiceImplTest {

    @Mock
    private NoteFolderMapper noteFolderMapper;
    @Mock
    private NoteMapper noteMapper;

    @InjectMocks
    private NoteFolderServiceImpl service;

    @Test
    void move_rejectsCycle() {
        NoteFolder a = NoteFolder.builder().id(1L).userId(7L).parentId(null).name("A").sortOrder(0).build();
        NoteFolder b = NoteFolder.builder().id(2L).userId(7L).parentId(1L).name("B").sortOrder(0).build();

        when(noteFolderMapper.selectById(1L)).thenReturn(a);
        when(noteFolderMapper.selectById(2L)).thenReturn(b);
        when(noteFolderMapper.selectList(any(Wrapper.class))).thenReturn(java.util.List.of(a, b));

        NoteFolderMoveDTO dto = new NoteFolderMoveDTO();
        dto.setParentId(2L);
        dto.setSortOrder(0);

        BusinessException ex = assertThrows(BusinessException.class, () -> service.move(7L, 1L, dto));
        assertTrue(ex.getMessage().contains("子文件夹"));
    }
}
