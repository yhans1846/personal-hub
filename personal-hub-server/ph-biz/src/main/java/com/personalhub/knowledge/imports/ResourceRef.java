package com.personalhub.knowledge.imports;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Markdown 中扫描到的资源引用
 */
@Data
@AllArgsConstructor
public class ResourceRef {

    /** 资源类型 */
    private ResourceType type;

    /** Markdown 中的原始引用字符串（如 img/demo.png） */
    private String originalRef;

    /** 在 Markdown 中的起始偏移 */
    private int startPos;

    /** 在 Markdown 中的结束偏移 */
    private int endPos;

    public enum ResourceType {
        IMAGE, LINK
    }

    /** 是否属于可直接跳过的本地资源引用（已导入过的） */
    public boolean isLocalResource() {
        return originalRef.startsWith("images/") || originalRef.startsWith("attachments/");
    }

    /** 是否属于文本/ Markdown 链接（应跳过导入） */
    public boolean isTextOrMarkdown() {
        String lower = originalRef.toLowerCase();
        return lower.endsWith(".md") || lower.endsWith(".txt");
    }
}
