package com.personalhub.knowledge.imports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

/**
 * Markdown 资源解析器 — 将 Markdown 中各种格式的资源引用解析为真实数据
 * <p>
 * 支持：HTTP(S)、Base64、file://、本地绝对路径、相对路径（配合 baseDir）
 */
@Slf4j
@Component
public class ResourceResolver {

    /**
     * 解析资源引用为实际可读数据
     *
     * @param ref     原始引用字符串
     * @param baseDir Markdown 文件所在目录（用于解析相对路径，可为 null）
     * @return 解析结果
     */
    public ResolveResult resolve(String ref, String baseDir) {
        ref = ref.trim();

        if (ref.startsWith("http://") || ref.startsWith("https://")) {
            return resolveHttp(ref);
        }
        if (ref.startsWith("data:")) {
            return resolveBase64(ref);
        }
        if (ref.startsWith("file://")) {
            return resolveFileProtocol(ref);
        }
        if (isAbsolutePath(ref)) {
            return resolveAbsolutePath(ref);
        }
        if (ref.startsWith("/")) {
            return ResolveResult.fail(ref, "站点根路径资源需要 baseUrl 支持，暂未实现");
        }
        if (baseDir != null && !baseDir.isEmpty()) {
            return resolveRelativePath(ref, baseDir);
        }

        return ResolveResult.fail(ref, "相对路径资源无法定位，请使用「从 Markdown 文件导入」并提供 baseDir");
    }

    /** HTTP(S) 网络资源 */
    private ResolveResult resolveHttp(String ref) {
        try {
            URI uri = new URI(ref);
            URLConnection conn = uri.toURL().openConnection();
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(15000);
            byte[] data = conn.getInputStream().readAllBytes();
            String contentType = conn.getContentType();
            String ext = detectExtension(ref, contentType);
            return ResolveResult.success(ref, data, ext);
        } catch (IOException | URISyntaxException e) {
            log.warn("网络资源下载失败: {}", ref, e);
            return ResolveResult.fail(ref, "下载失败: " + e.getMessage());
        }
    }

    /** Base64 内嵌资源 */
    private ResolveResult resolveBase64(String ref) {
        try {
            // data:image/png;base64,iVBORw0KGgo...
            String encoded = ref.substring(ref.indexOf(',') + 1);
            byte[] data = Base64.getDecoder().decode(encoded);
            String ext = detectExtension(ref, null);
            return ResolveResult.success(ref, data, ext);
        } catch (Exception e) {
            log.warn("Base64 解码失败: {}", ref, e);
            return ResolveResult.fail(ref, "Base64 解码失败");
        }
    }

    /** file:// 协议资源 */
    private ResolveResult resolveFileProtocol(String ref) {
        try {
            Path path = Paths.get(URI.create(ref));
            if (Files.exists(path)) {
                byte[] data = Files.readAllBytes(path);
                String ext = detectExtension(ref, null);
                return ResolveResult.success(ref, data, ext);
            }
            return ResolveResult.fail(ref, "文件不存在: " + path);
        } catch (Exception e) {
            log.warn("file:// 资源读取失败: {}", ref, e);
            return ResolveResult.fail(ref, "读取失败: " + e.getMessage());
        }
    }

    /** 本地绝对路径（Windows / Unix / macOS） */
    private ResolveResult resolveAbsolutePath(String ref) {
        try {
            Path path = Paths.get(ref);
            if (Files.exists(path)) {
                byte[] data = Files.readAllBytes(path);
                String ext = detectExtension(ref, null);
                return ResolveResult.success(ref, data, ext);
            }
            return ResolveResult.fail(ref, "文件不存在: " + path);
        } catch (Exception e) {
            log.warn("本地绝对路径资源读取失败: {}", ref, e);
            return ResolveResult.fail(ref, "读取失败: " + e.getMessage());
        }
    }

    /** 相对路径（相对于 Markdown 所在目录） */
    private ResolveResult resolveRelativePath(String ref, String baseDir) {
        try {
            Path base = Paths.get(baseDir).normalize();
            Path resolved = base.resolve(ref).normalize();
            if (Files.exists(resolved)) {
                byte[] data = Files.readAllBytes(resolved);
                String ext = detectExtension(ref, null);
                return ResolveResult.success(ref, data, ext);
            }
            return ResolveResult.fail(ref, "相对路径文件不存在: " + resolved);
        } catch (Exception e) {
            log.warn("相对路径资源读取失败: ref={}, baseDir={}", ref, baseDir, e);
            return ResolveResult.fail(ref, "读取失败: " + e.getMessage());
        }
    }

    /** 判断是否为本地绝对路径 */
    private boolean isAbsolutePath(String ref) {
        if (ref.matches("^[A-Za-z]:\\\\.*")) return true;  // Windows
        if (ref.matches("^[A-Za-z]:/.*")) return true;     // Windows (mixed)
        if (ref.startsWith("/")) return true;                // Unix / macOS
        return false;
    }

    /** 根据 URL/Content-Type 检测文件扩展名 */
    private String detectExtension(String ref, String contentType) {
        // 从 URL 路径中提取
        String path = ref;
        int qIdx = ref.indexOf('?');
        if (qIdx > 0) path = ref.substring(0, qIdx);
        int hashIdx = path.indexOf('#');
        if (hashIdx > 0) path = path.substring(0, hashIdx);

        int dot = path.lastIndexOf('.');
        if (dot > 0 && dot < path.length() - 1) {
            String ext = path.substring(dot + 1).toLowerCase();
            if (ext.matches("[a-z0-9]+") && ext.length() <= 5) return ext;
        }

        // 从 data URI 中提取
        if (ref.startsWith("data:")) {
            int slash = ref.indexOf('/');
            int semi = ref.indexOf(';');
            if (slash > 0 && semi > slash) {
                return ref.substring(slash + 1, semi);
            }
        }

        // 从 Content-Type 中提取
        if (contentType != null) {
            int slash = contentType.indexOf('/');
            if (slash > 0 && slash < contentType.length() - 1) {
                String type = contentType.substring(slash + 1).split(";")[0].trim();
                if (type.matches("[a-z0-9]+")) return type;
            }
        }

        return "png"; // 默认
    }

    /** 解析结果 */
    @Data
    @Builder
    @AllArgsConstructor
    public static class ResolveResult {
        private String originalRef;
        private byte[] data;
        private String extension;
        private boolean success;
        private String message;

        public static ResolveResult success(String ref, byte[] data, String ext) {
            return ResolveResult.builder()
                    .originalRef(ref)
                    .data(data)
                    .extension(ext)
                    .success(true)
                    .message("解析成功")
                    .build();
        }

        public static ResolveResult fail(String ref, String message) {
            return ResolveResult.builder()
                    .originalRef(ref)
                    .success(false)
                    .message(message)
                    .build();
        }
    }
}
