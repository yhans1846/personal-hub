package com.personalhub.storage;

import com.personalhub.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalStorageServiceImplTest {

    @TempDir
    Path tempDir;

    private LocalStorageServiceImpl storage;

    @BeforeEach
    void setUp() {
        StorageProperties props = new StorageProperties();
        props.setLocation(tempDir);
        storage = new LocalStorageServiceImpl(props);
    }

    @Test
    void storeAndRead_withinRoot_ok() {
        storage.write("notes/1/note.md", "hello");
        assertEquals("hello", storage.read("notes/1/note.md"));
        assertTrue(storage.exists("notes/1/note.md"));
    }

    @Test
    void load_pathTraversal_rejected() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> storage.load("../outside.txt"));
        assertEquals("非法存储路径", ex.getMessage());
    }

    @Test
    void write_pathTraversal_rejected() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> storage.write("notes/../../outside.txt", "x"));
        assertEquals("非法存储路径", ex.getMessage());
    }

    @Test
    void delete_pathTraversal_rejected() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> storage.delete("avatars/../../../etc/passwd"));
        assertEquals("非法存储路径", ex.getMessage());
    }

    @Test
    void store_bytes_pathTraversal_rejected() {
        BusinessException ex = assertThrows(BusinessException.class,
                () -> storage.store("x".getBytes(StandardCharsets.UTF_8), "a/../../b.txt"));
        assertEquals("非法存储路径", ex.getMessage());
    }

    @Test
    void load_absoluteEscape_rejected() throws Exception {
        Path outside = tempDir.getParent().resolve("escape-target.txt");
        Files.writeString(outside, "secret");
        String relative = tempDir.relativize(outside).toString().replace('\\', '/');
        BusinessException ex = assertThrows(BusinessException.class, () -> storage.load(relative));
        assertEquals("非法存储路径", ex.getMessage());
    }
}
