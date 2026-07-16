package com.personalhub.knowledge.imports;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Markdown 资源扫描器 — 扫描 Markdown 中所有图片和链接引用
 */
@Component
public class ResourceScanner {

    /** 图片: ![alt](src) */
    private static final Pattern IMG_PATTERN = Pattern.compile("!\\[([^\\]]*)\\]\\(([^)]+)\\)");

    /** 链接: [text](href) — 排除图片标记的 ! 前缀 */
    private static final Pattern LINK_PATTERN = Pattern.compile("(?<!!)\\[([^\\]]*)\\]\\(([^)]+)\\)");

    /**
     * 扫描 Markdown 内容，返回所有非本地资源引用
     *
     * @param content Markdown 原文
     * @return 资源引用列表（已过滤本地资源、.md/.txt 链接）
     */
    public List<ResourceRef> scan(String content) {
        List<ResourceRef> refs = new ArrayList<>();
        scanPattern(content, IMG_PATTERN, ResourceRef.ResourceType.IMAGE, refs);
        scanPattern(content, LINK_PATTERN, ResourceRef.ResourceType.LINK, refs);
        return refs;
    }

    /**
     * 扫描 Markdown 内容，检测是否包含相对路径资源
     *
     * @param content Markdown 原文
     * @return 是否包含相对路径引用
     */
    public boolean hasRelativePathRefs(String content) {
        Matcher imgMatcher = IMG_PATTERN.matcher(content);
        while (imgMatcher.find()) {
            String ref = imgMatcher.group(2).trim();
            if (isRelativePath(ref)) return true;
        }
        Matcher linkMatcher = LINK_PATTERN.matcher(content);
        while (linkMatcher.find()) {
            String ref = linkMatcher.group(2).trim();
            if (isRelativePath(ref)) return true;
        }
        return false;
    }

    private void scanPattern(String content, Pattern pattern, ResourceRef.ResourceType type, List<ResourceRef> refs) {
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            String ref = matcher.group(2).trim();
            ResourceRef resourceRef = new ResourceRef(type, ref, matcher.start(2), matcher.end(2));
            // 跳过已导入的本地资源
            if (resourceRef.isLocalResource()) continue;
            // 跳过 .md / .txt 链接
            if (type == ResourceRef.ResourceType.LINK && resourceRef.isTextOrMarkdown()) continue;
            refs.add(resourceRef);
        }
    }

    /**
     * 判断是否为相对路径（相对于 Markdown 文件所在目录）
     */
    private boolean isRelativePath(String ref) {
        if (ref.startsWith("http://") || ref.startsWith("https://")) return false;
        if (ref.startsWith("data:")) return false;
        if (ref.startsWith("file://")) return false;
        if (ref.startsWith("/")) return false; // 站点根路径，非相对路径
        if (ref.startsWith("images/") || ref.startsWith("attachments/")) return false;
        if (isAbsolutePath(ref)) return false;
        return true;
    }

    /** 判断是否为本地绝对路径 */
    private boolean isAbsolutePath(String ref) {
        if (ref.matches("^[A-Za-z]:\\\\.*")) return true;  // Windows
        if (ref.startsWith("/")) return true;                // Unix / macOS
        return false;
    }
}
