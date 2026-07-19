package com.personalhub.knowledge.imports;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourceResolverTest {

    private final ResourceResolver resolver = new ResourceResolver();

    @TempDir
    Path tempDir;

    @Test
    void fileProtocol_rejected() {
        var result = resolver.resolve("file:///etc/passwd", null);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("不支持"));
    }

    @Test
    void absolutePath_rejected() {
        var result = resolver.resolve("C:\\Windows\\win.ini", null);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("不支持"));
    }

    @Test
    void relativeEscape_rejected() throws Exception {
        Path base = tempDir.resolve("md");
        Files.createDirectories(base);
        Files.writeString(tempDir.resolve("secret.txt"), "secret");
        var result = resolver.resolve("../secret.txt", base.toString());
        assertFalse(result.isSuccess());
    }

    @Test
    void relativeWithinBase_ok() throws Exception {
        Path base = tempDir.resolve("md");
        Files.createDirectories(base);
        Files.writeString(base.resolve("img.png"), "png");
        var result = resolver.resolve("img.png", base.toString());
        assertTrue(result.isSuccess());
    }

    @Test
    void localhostHttp_rejected() {
        var result = resolver.resolve("http://127.0.0.1/x", null);
        assertFalse(result.isSuccess());
        assertTrue(result.getMessage().contains("不允许"));
    }

    @Test
    void dataUri_ok() {
        var result = resolver.resolve("data:image/png;base64,aGVsbG8=", null);
        assertTrue(result.isSuccess());
    }
}
