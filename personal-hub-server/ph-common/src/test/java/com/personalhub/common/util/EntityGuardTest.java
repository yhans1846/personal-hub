package com.personalhub.common.util;

import com.personalhub.common.exception.NotFoundException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EntityGuardTest {

    static class Item {
        Long userId;

        Item(Long userId) {
            this.userId = userId;
        }

        Long getUserId() {
            return userId;
        }
    }

    @Test
    void requireOwned_null_throws() {
        assertThrows(NotFoundException.class,
                () -> EntityGuard.requireOwned(null, 1L, Item::getUserId, "不存在"));
    }

    @Test
    void requireOwned_wrongUser_throws() {
        assertThrows(NotFoundException.class,
                () -> EntityGuard.requireOwned(new Item(2L), 1L, Item::getUserId, "不存在"));
    }

    @Test
    void requireOwned_ok_returnsEntity() {
        Item item = new Item(1L);
        assertSame(item, EntityGuard.requireOwned(item, 1L, Item::getUserId, "不存在"));
    }
}
