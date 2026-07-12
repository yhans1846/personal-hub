package com.personalhub.knowledge.imports;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Markdown 重写器 — 将 Markdown 中的资源引用替换为导入后的新路径
 */
@Component
public class MarkdownRewriter {

    /**
     * 重写 Markdown 中的资源引用
     *
     * @param content  原始 Markdown 内容
     * @param refs     资源引用列表（含位置信息）
     * @param newPaths 原始引用 → 新路径 的映射
     * @return 重写后的 Markdown 内容
     */
    public String rewrite(String content, List<ResourceRef> refs, Map<String, String> newPaths) {
        // 构建替换列表
        List<Replacement> replacements = new ArrayList<>();
        for (ResourceRef ref : refs) {
            String newPath = newPaths.get(ref.getOriginalRef());
            if (newPath != null) {
                replacements.add(new Replacement(ref.getStartPos(), ref.getEndPos(), newPath));
            }
        }

        if (replacements.isEmpty()) return content;

        // 按位置降序排列，从尾部开始替换，避免位置偏移
        replacements.sort(Comparator.comparingInt(Replacement::getStartPos).reversed());

        StringBuilder sb = new StringBuilder(content);
        for (Replacement r : replacements) {
            sb.replace(r.getStartPos(), r.getEndPos(), r.getNewRef());
        }
        return sb.toString();
    }

    /**
     * 检查内容中是否包含相对路径资源
     */
    public boolean hasRelativePathRefs(String content) {
        return content.matches(".*!\\[.*\\]\\((?!(https?://|data:|file://|/|images/|attachments/))[^)]+\\).*")
                || content.matches(".*(?<!!)\\[.*\\]\\((?!(https?://|data:|file://|/|images/|attachments/))[^)]+\\.(?!md|txt)[^)]*\\).*");
    }

    @Data
    @AllArgsConstructor
    private static class Replacement {
        private int startPos;
        private int endPos;
        private String newRef;
    }
}
