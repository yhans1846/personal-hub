package com.personalhub.system.service.impl;

import com.personalhub.system.entity.UserLayout;
import com.personalhub.system.mapper.UserLayoutMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserLayoutServiceImplTest {

    @Mock
    private UserLayoutMapper userLayoutMapper;

    @InjectMocks
    private UserLayoutServiceImpl service;

    @Test
    void save_whenSoftDeletedExists_updatesAndRestores() {
        UserLayout softDeleted = UserLayout.builder()
                .id(9L)
                .userId(1L)
                .layoutType("menu")
                .layoutJson("{\"items\":[]}")
                .isDeleted(1)
                .build();
        when(userLayoutMapper.selectByUserAndTypeAny(1L, "menu")).thenReturn(softDeleted);

        service.save(1L, "menu", "{\"items\":[{\"code\":\"notes\"}]}");

        verify(userLayoutMapper, never()).insert(any(UserLayout.class));
        ArgumentCaptor<UserLayout> cap = ArgumentCaptor.forClass(UserLayout.class);
        verify(userLayoutMapper).restoreAndUpdate(cap.capture());
        assertEquals(9L, cap.getValue().getId());
        assertEquals("{\"items\":[{\"code\":\"notes\"}]}", cap.getValue().getLayoutJson());
    }

    @Test
    void save_whenActiveExists_updates() {
        UserLayout active = UserLayout.builder()
                .id(2L)
                .userId(1L)
                .layoutType("menu")
                .layoutJson("old")
                .isDeleted(0)
                .build();
        when(userLayoutMapper.selectByUserAndTypeAny(1L, "menu")).thenReturn(active);

        service.save(1L, "menu", "new-json");

        verify(userLayoutMapper, never()).insert(any(UserLayout.class));
        ArgumentCaptor<UserLayout> cap = ArgumentCaptor.forClass(UserLayout.class);
        verify(userLayoutMapper).restoreAndUpdate(cap.capture());
        assertEquals("new-json", cap.getValue().getLayoutJson());
    }

    @Test
    void save_whenMissing_inserts() {
        when(userLayoutMapper.selectByUserAndTypeAny(1L, "menu")).thenReturn(null);

        service.save(1L, "menu", "{}");

        ArgumentCaptor<UserLayout> cap = ArgumentCaptor.forClass(UserLayout.class);
        verify(userLayoutMapper).insert(cap.capture());
        assertEquals(1L, cap.getValue().getUserId());
        assertEquals("menu", cap.getValue().getLayoutType());
        assertEquals("{}", cap.getValue().getLayoutJson());
    }

    @Test
    void save_whenInsertHitsDuplicate_fallsBackToUpdate() {
        when(userLayoutMapper.selectByUserAndTypeAny(1L, "menu"))
                .thenReturn(null)
                .thenReturn(UserLayout.builder().id(3L).userId(1L).layoutType("menu").isDeleted(1).build());
        when(userLayoutMapper.insert(any(UserLayout.class))).thenThrow(new DuplicateKeyException("uk_user_layout"));

        assertDoesNotThrow(() -> service.save(1L, "menu", "recovered"));

        verify(userLayoutMapper).restoreAndUpdate(any(UserLayout.class));
    }
}
