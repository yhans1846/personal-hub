package com.personalhub.knowledge.imports;

import com.personalhub.common.exception.BusinessException;

import com.personalhub.knowledge.dto.NoteCreateDTO;
import com.personalhub.knowledge.imports.ResourceResolver.ResolveResult;
import com.personalhub.knowledge.service.NoteService;
import com.personalhub.knowledge.vo.NoteVO;
import com.personalhub.storage.StorageService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Markdown 导入服务 — 统一入口
 * <p>
 * 负责：方式一（从 Markdown 文件导入）、方式二（粘贴 Markdown 内容）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImportService {

    private final NoteService noteService;
    private final StorageService storageService;
    private final ResourceScanner resourceScanner;
    private final ResourceResolver resourceResolver;
    private final ResourceDownloader resourceDownloader;
    private final MarkdownRewriter markdownRewriter;

    /**
     * 方式一：从 Markdown 文件导入
     * <p>
     * 上传 .md 文件，自动解析所有资源引用（下载网络图片、复制本地文件等）。
     * 如果提供 baseDir，可以解析 Markdown 中的相对路径资源。
     */
    @Transactional
    public ImportReport importFromFile(Long userId, String title, List<Long> categoryIds,
                                       List<Long> tagIds, MultipartFile file, String baseDir) {
        // 1. 读取文件内容
        String content = readFileContent(file);
        if (title == null || title.isBlank()) {
            title = file.getOriginalFilename();
            if (title != null && title.toLowerCase().endsWith(".md")) {
                title = title.substring(0, title.length() - 3);
            }
        }

        // 2. 创建笔记（noteService.create 会写入初始内容到 storage）
        NoteVO createdNote = createNote(userId, title, content, categoryIds, tagIds);
        Long noteId = createdNote.getId();
        String noteDir = "notes/" + noteId;

        // 3. 扫描 + 处理资源（一次性获取 refs 和 report）
        ScanResult scanResult = processResources(content, noteDir, baseDir);

        // 4. 如果有资源变更，覆写 note.md
        if (!scanResult.report.getResources().isEmpty()) {
            String updatedContent = markdownRewriter.rewrite(
                    content, scanResult.refs, scanResult.buildReplacements());
            overwriteMdFile(noteDir, updatedContent);
        }

        scanResult.report.setNoteId(noteId);
        log.info("Markdown 文件导入完成: noteId={}, 资源={}成功/{}失败/{}跳过",
                noteId, scanResult.report.getSuccess(),
                scanResult.report.getFailed(), scanResult.report.getSkipped());
        return scanResult.report;
    }

    /**
     * 方式二：粘贴 Markdown 内容导入
     * <p>
     * 适用于从 AI 对话、网页复制等场景。
     * 支持：网络图片自动下载、Base64 图片解码。
     * 注意：相对路径资源无法解析，会返回警告。
     */
    @Transactional
    public ImportReport importFromContent(Long userId, String title, String content,
                                          List<Long> categoryIds, List<Long> tagIds) {
        // 1. 检测相对路径
        boolean hasRelativePath = resourceScanner.hasRelativePathRefs(content);

        // 2. 创建笔记
        NoteVO createdNote = createNote(userId, title, content, categoryIds, tagIds);
        Long noteId = createdNote.getId();
        String noteDir = "notes/" + noteId;

        // 3. 扫描 + 处理资源
        ScanResult scanResult = processResources(content, noteDir, null);

        // 4. 如果有资源变更，覆写 note.md
        String updatedContent = content;
        if (!scanResult.report.getResources().isEmpty()) {
            updatedContent = markdownRewriter.rewrite(
                    content, scanResult.refs, scanResult.buildReplacements());
            overwriteMdFile(noteDir, updatedContent);
        }

        scanResult.report.setNoteId(noteId);
        scanResult.report.setRewrittenContent(updatedContent);
        if (hasRelativePath) {
            scanResult.report.setWarning("检测到 Markdown 中存在相对路径资源，请使用「从 Markdown 文件导入」以自动导入图片及附件。");
        }

        log.info("Markdown 内容导入完成: noteId={}, 资源={}成功/{}失败/{}跳过",
                noteId, scanResult.report.getSuccess(),
                scanResult.report.getFailed(), scanResult.report.getSkipped());
        return scanResult.report;
    }

    // ========== 内部方法 ==========

    /** 创建笔记并设置初始关联 */
    private NoteVO createNote(Long userId, String title, String content,
                              List<Long> categoryIds, List<Long> tagIds) {
        NoteCreateDTO dto = new NoteCreateDTO();
        dto.setTitle(title != null ? title : "未命名笔记");
        dto.setContent(content);
        dto.setCategoryIds(categoryIds);
        dto.setTagIds(tagIds);
        return noteService.create(userId, dto);
    }

    /** 扫描 + 解析 + 下载资源，一次性返回结果 */
    private ScanResult processResources(String content, String noteDir, String baseDir) {
        ImportReport report = new ImportReport();
        List<ResourceRef> refs = resourceScanner.scan(content);
        if (refs.isEmpty()) {
            return new ScanResult(report, refs);
        }

        for (ResourceRef ref : refs) {
            String originalRef = ref.getOriginalRef();

            if (ref.isLocalResource()) {
                report.addSkipped(originalRef, "已在本地目录，无需导入");
                continue;
            }

            ResolveResult resolveResult = resourceResolver.resolve(originalRef, baseDir);
            if (!resolveResult.isSuccess()) {
                report.addFailed(originalRef, resolveResult.getMessage());
                continue;
            }

            String type = ref.getType() == ResourceRef.ResourceType.IMAGE ? "images" : "attachments";
            var downloadResult = resourceDownloader.download(resolveResult, noteDir, type);
            if (downloadResult.isSuccess()) {
                report.addSuccess(originalRef, downloadResult.getTargetPath());
            } else {
                report.addFailed(originalRef, downloadResult.getMessage());
            }
        }
        return new ScanResult(report, refs);
    }

    /** 覆写笔记的 note.md（使用重写后的内容） */
    private void overwriteMdFile(String noteDir, String content) {
        String mdPath = noteDir + "/note.md";
        storageService.write(mdPath, content);
    }

    /** 读取 MultipartFile 内容为字符串 */
    public static String readFileContent(MultipartFile file) {
        try (var reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            throw new BusinessException("读取 Markdown 文件失败", e);
        }
    }

    /** 内部：扫描结果（report + refs） */
    @Getter
    @AllArgsConstructor
    private static class ScanResult {
        private final ImportReport report;
        private final List<ResourceRef> refs;

        /** 从 report 的成功结果中构建 old → new 映射 */
        Map<String, String> buildReplacements() {
            Map<String, String> map = new HashMap<>();
            for (var res : report.getResources()) {
                if (res.isSuccess() && res.getResolvedPath() != null) {
                    map.put(res.getOriginalRef(), res.getResolvedPath());
                }
            }
            return map;
        }
    }
}
