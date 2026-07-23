package com.personalhub.knowledge.imports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;

/**
 * Markdown 资源解析器 — 将 Markdown 中各种格式的资源引用解析为真实数据
 * <p>
 * 支持：HTTP(S)（禁内网/本机）、Base64、相对路径（须落在 baseDir 内）。
 * 不支持：file://、本地绝对路径（防 LFI）。
 */
@Slf4j
@Component
public class ResourceResolver {

    private static final int MAX_BYTES = 20 * 1024 * 1024;

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
            return ResolveResult.fail(ref, "不支持 file:// 协议");
        }
        if (isAbsolutePath(ref)) {
            return ResolveResult.fail(ref, "不支持本地绝对路径资源");
        }
        if (ref.startsWith("/")) {
            return ResolveResult.fail(ref, "站点根路径资源需要 baseUrl 支持，暂未实现");
        }
        if (baseDir != null && !baseDir.isEmpty()) {
            return resolveRelativePath(ref, baseDir);
        }

        return ResolveResult.fail(ref, "相对路径资源无法定位，请使用「从 Markdown 文件导入」并提供 baseDir");
    }

    /** HTTP(S) 网络资源（禁 SSRF；遵循系统代理，避免直连被墙/黑洞） */
    private ResolveResult resolveHttp(String ref) {
        try {
            URI uri = new URI(ref);
            String scheme = uri.getScheme();
            if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
                return ResolveResult.fail(ref, "仅支持 http/https 远程资源");
            }
            String host = uri.getHost();
            if (host == null || host.isBlank() || isBlockedHostName(host)) {
                return ResolveResult.fail(ref, "不允许访问该主机");
            }
            if (isBlockedHost(host)) {
                return ResolveResult.fail(ref, "不允许访问内网或本地地址");
            }

            Proxy proxy = selectProxy(uri);
            HttpURLConnection conn = (HttpURLConnection) uri.toURL().openConnection(proxy);
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(30000);
            conn.setInstanceFollowRedirects(false);
            conn.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (compatible; PersonalHub/1.0; +https://github.com/personal-hub)");
            conn.setRequestProperty("Accept", "image/avif,image/webp,image/apng,image/*,*/*;q=0.8");

            int status = conn.getResponseCode();
            if (status >= 300 && status < 400) {
                return ResolveResult.fail(ref, "不支持 HTTP 重定向");
            }
            if (status < 200 || status >= 300) {
                return ResolveResult.fail(ref, "远程资源不可用（HTTP " + status + "）");
            }

            String contentType = conn.getContentType();
            byte[] data;
            try (InputStream in = conn.getInputStream()) {
                data = in.readAllBytes();
            }
            if (data.length > MAX_BYTES) {
                return ResolveResult.fail(ref, "远程资源过大（超过 20MB）");
            }
            String ext = detectExtension(ref, contentType);
            return ResolveResult.success(ref, data, ext);
        } catch (UnknownHostException e) {
            log.warn("网络资源主机无法解析: {}", ref);
            return ResolveResult.fail(ref, "主机无法解析");
        } catch (SocketTimeoutException e) {
            log.warn("网络资源连接超时: {}", ref);
            return ResolveResult.fail(ref, "连接超时（若使用 Clash 等代理，请确认系统代理已开启且 JVM 已启用 useSystemProxies）");
        } catch (IOException | URISyntaxException e) {
            log.warn("网络资源下载失败: {}", ref, e);
            String detail = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
            return ResolveResult.fail(ref, "下载失败: " + detail);
        }
    }

    /** 使用默认 ProxySelector（配合 java.net.useSystemProxies） */
    private static Proxy selectProxy(URI uri) {
        try {
            List<Proxy> proxies = ProxySelector.getDefault().select(uri);
            if (proxies != null && !proxies.isEmpty()) {
                return proxies.get(0);
            }
        } catch (Exception e) {
            log.debug("解析系统代理失败，改用直连: {}", e.toString());
        }
        return Proxy.NO_PROXY;
    }

    private boolean isBlockedHostName(String host) {
        String h = host.toLowerCase();
        return "localhost".equals(h)
                || h.endsWith(".localhost")
                || h.endsWith(".local")
                || h.endsWith(".internal")
                || "metadata.google.internal".equals(h);
    }

    private boolean isBlockedHost(String host) {
        try {
            for (InetAddress addr : InetAddress.getAllByName(host)) {
                if (isBlockedAddress(addr)) {
                    return true;
                }
            }
            return false;
        } catch (UnknownHostException e) {
            return true;
        }
    }

    private boolean isBlockedAddress(InetAddress addr) {
        return addr.isAnyLocalAddress()
                || addr.isLoopbackAddress()
                || addr.isLinkLocalAddress()
                || addr.isSiteLocalAddress()
                || addr.isMulticastAddress()
                || isCarrierGradeNat(addr)
                || isUniqueLocalIpv6(addr);
    }

    /** 100.64.0.0/10 */
    private boolean isCarrierGradeNat(InetAddress addr) {
        byte[] b = addr.getAddress();
        if (b.length != 4) {
            return false;
        }
        int first = b[0] & 0xFF;
        int second = b[1] & 0xFF;
        return first == 100 && second >= 64 && second <= 127;
    }

    private boolean isUniqueLocalIpv6(InetAddress addr) {
        byte[] b = addr.getAddress();
        return b.length == 16 && (b[0] & 0xFE) == 0xFC;
    }

    /** Base64 内嵌资源 */
    private ResolveResult resolveBase64(String ref) {
        try {
            String encoded = ref.substring(ref.indexOf(',') + 1);
            byte[] data = Base64.getDecoder().decode(encoded);
            String ext = detectExtension(ref, null);
            return ResolveResult.success(ref, data, ext);
        } catch (Exception e) {
            log.warn("Base64 解码失败: {}", ref, e);
            return ResolveResult.fail(ref, "Base64 解码失败");
        }
    }

    /** 相对路径（相对于 Markdown 所在目录，禁止越界） */
    private ResolveResult resolveRelativePath(String ref, String baseDir) {
        try {
            Path base = Paths.get(baseDir).toAbsolutePath().normalize();
            Path resolved = base.resolve(ref).normalize();
            if (!resolved.startsWith(base)) {
                return ResolveResult.fail(ref, "相对路径越界");
            }
            if (Files.exists(resolved) && Files.isRegularFile(resolved)) {
                byte[] data = Files.readAllBytes(resolved);
                if (data.length > MAX_BYTES) {
                    return ResolveResult.fail(ref, "本地资源过大（超过 20MB）");
                }
                String ext = detectExtension(ref, null);
                return ResolveResult.success(ref, data, ext);
            }
            return ResolveResult.fail(ref, "相对路径文件不存在");
        } catch (Exception e) {
            log.warn("相对路径资源读取失败: ref={}", ref, e);
            return ResolveResult.fail(ref, "读取失败");
        }
    }

    private boolean isAbsolutePath(String ref) {
        if (ref.matches("^[A-Za-z]:\\\\.*")) {
            return true;
        }
        if (ref.matches("^[A-Za-z]:/.*")) {
            return true;
        }
        return ref.startsWith("/");
    }

    private String detectExtension(String ref, String contentType) {
        String path = ref;
        int qIdx = ref.indexOf('?');
        if (qIdx > 0) {
            path = ref.substring(0, qIdx);
        }
        int hashIdx = path.indexOf('#');
        if (hashIdx > 0) {
            path = path.substring(0, hashIdx);
        }

        int dot = path.lastIndexOf('.');
        if (dot > 0 && dot < path.length() - 1) {
            String ext = path.substring(dot + 1).toLowerCase();
            if (ext.matches("[a-z0-9]+") && ext.length() <= 5) {
                return ext;
            }
        }

        if (ref.startsWith("data:")) {
            int slash = ref.indexOf('/');
            int semi = ref.indexOf(';');
            if (slash > 0 && semi > slash) {
                return ref.substring(slash + 1, semi);
            }
        }

        if (contentType != null) {
            int slash = contentType.indexOf('/');
            if (slash > 0 && slash < contentType.length() - 1) {
                String type = contentType.substring(slash + 1).split(";")[0].trim();
                if (type.matches("[a-z0-9]+")) {
                    return type;
                }
            }
        }

        return "png";
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
