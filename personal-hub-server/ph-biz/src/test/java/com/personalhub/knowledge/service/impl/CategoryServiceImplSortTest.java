package com.personalhub.knowledge.service.impl;

import com.personalhub.common.exception.NotFoundException;
import com.personalhub.knowledge.dto.SortOrderDTO;
import com.personalhub.knowledge.entity.Category;
import com.personalhub.knowledge.mapper.CategoryMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplSortTest {

    @Mock CategoryMapper categoryMapper;

    CategoryServiceImpl categoryService;

    @BeforeEach
    void setUp() {
        categoryService = new CategoryServiceImpl(categoryMapper);
    }

    @Test
    void updateSortOrder_foreignCategory_rejected() {
        when(categoryMapper.selectById(5L)).thenReturn(
                Category.builder().id(5L).userId(2L).name("x").type("note").build());
        SortOrderDTO dto = new SortOrderDTO();
        dto.setId(5L);
        dto.setSortOrder(1);

        assertThrows(NotFoundException.class,
                () -> categoryService.updateSortOrder(1L, List.of(dto)));
        verify(categoryMapper, never()).updateById(isA(Category.class));
    }
}
