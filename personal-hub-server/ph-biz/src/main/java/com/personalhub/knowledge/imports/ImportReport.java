package com.personalhub.knowledge.imports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Markdown 导入报告 — 记录导入结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportReport {

    /** 资源总数 */
    private int total;

    /** 成功数 */
    private int success;

    /** 跳过数 */
    private int skipped;

    /** 失败数 */
    private int failed;

    /** 详细资源结果 */
    @Builder.Default
    private List<ResourceResult> resources = new ArrayList<>();

    /** 导入后的笔记 ID（仅方式一/方式二成功时） */
    private Long noteId;

    /** 重写后的 Markdown 内容（仅方式二返回） */
    private String rewrittenContent;

    /** 相对路径警告（方式二检测到相对路径时） */
    private String warning;

    /** 添加一条资源结果 */
    public void addResult(ResourceResult result) {
        resources.add(result);
        total++;
        if (result.isSuccess()) success++;
        else if (result.isSkipped()) skipped++;
        else failed++;
    }

    /** 添加一条成功结果 */
    public void addSuccess(String originalRef, String resolvedPath) {
        addResult(ResourceResult.builder()
                .originalRef(originalRef)
                .resolvedPath(resolvedPath)
                .success(true)
                .message("导入成功")
                .build());
    }

    /** 添加一条跳过结果 */
    public void addSkipped(String originalRef, String message) {
        addResult(ResourceResult.builder()
                .originalRef(originalRef)
                .success(false)
                .skipped(true)
                .message(message)
                .build());
    }

    /** 添加一条失败结果 */
    public void addFailed(String originalRef, String message) {
        addResult(ResourceResult.builder()
                .originalRef(originalRef)
                .success(false)
                .skipped(false)
                .message(message)
                .build());
    }

    /**
     * 单条资源导入结果
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResourceResult {
        /** 原始引用（如 img/demo.png） */
        private String originalRef;

        /** 导入后的路径（如 images/uuid.png） */
        private String resolvedPath;

        /** 是否成功 */
        private boolean success;

        /** 是否跳过（非错误，如资源已在本地目录） */
        private boolean skipped;

        /** 描述信息 */
        private String message;
    }
}
