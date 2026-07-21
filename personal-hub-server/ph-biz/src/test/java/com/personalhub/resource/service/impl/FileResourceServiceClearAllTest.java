package com.personalhub.resource.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.personalhub.resource.entity.FileResource;
import com.personalhub.resource.mapper.FileResourceMapper;
import com.personalhub.resource.vo.FileClearVO;
import com.personalhub.storage.FileAssetService;
import com.personalhub.storage.StorageProperties;
import com.personalhub.knowledge.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileResourceServiceClearAllTest {

    @Mock
    private FileResourceMapper fileResourceMapper;
    @Mock
    private CategoryService categoryService;
    @Mock
    private FileAssetService fileAssetService;
    @Mock
    private StorageProperties storageProperties;

    @InjectMocks
    private FileResourceServiceImpl service;

    @Test
    void clearAll_returnsZeroWhenEmpty() {
        when(fileResourceMapper.selectList(any(Wrapper.class))).thenReturn(List.of());

        FileClearVO result = service.clearAll(7L);

        assertEquals(0, result.getDeleted());
        verify(fileAssetService, never()).delete(any());
        verify(fileResourceMapper, never()).deleteById(org.mockito.ArgumentMatchers.<Long>any());
    }

    @Test
    void clearAll_deletesEachOwnedFile() {
        FileResource a = new FileResource();
        a.setId(1L);
        a.setUserId(7L);
        a.setName("a.png");
        a.setPath("image/a.png");
        FileResource b = new FileResource();
        b.setId(2L);
        b.setUserId(7L);
        b.setName("b.pdf");
        b.setPath("document/b.pdf");

        when(fileResourceMapper.selectList(any(Wrapper.class))).thenReturn(List.of(a, b));
        when(fileResourceMapper.selectById(1L)).thenReturn(a);
        when(fileResourceMapper.selectById(2L)).thenReturn(b);

        FileClearVO result = service.clearAll(7L);

        assertEquals(2, result.getDeleted());
        verify(fileAssetService).delete("image/a.png");
        verify(fileAssetService).delete("document/b.pdf");
        verify(fileResourceMapper, times(2)).deleteById(org.mockito.ArgumentMatchers.<Long>any());
        verify(fileResourceMapper).deleteById(1L);
        verify(fileResourceMapper).deleteById(2L);
    }
}
