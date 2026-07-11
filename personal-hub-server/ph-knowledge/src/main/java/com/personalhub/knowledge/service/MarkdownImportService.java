package com.personalhub.knowledge.service;

import com.personalhub.knowledge.dto.NoteCreateDTO;
import com.personalhub.knowledge.entity.Note;
import com.personalhub.knowledge.mapper.NoteMapper;
import com.personalhub.knowledge.vo.NoteVO;
import com.personalhub.storage.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Markdown 导入服务 — 自动解析并本地化所有资源引用
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MarkdownImportService {

    private final StorageService storageService;
    private final NoteService noteService;
    private final NoteMapper noteMapper;

    private static final Pattern IMG_PATTERN = Pattern.compile("!\\[([^\\]]*)\\]\\(([^)]+)\\)");
    private static final Pattern LINK_PATTERN = Pattern.compile("(?<!!)\\[([^\\]]*)\\]\\(([^)]+)\\)");

    /**
     * 导入 Markdown 文件，自动下载/复制所有引用资源
     */
    @Transactional
    public Long importMarkdown(Long userId, String title, List<Long> categoryIds,
                                List<Long> tagIds, MultipartFile file) {
        String content;
        try (var reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append("\n");
            }
            content = sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("读取 Markdown 文件失败", e);
        }

        // 先创建笔记（获取 noteId）
        NoteCreateDTO dto = new NoteCreateDTO();
        dto.setTitle(title != null ? title : file.getOriginalFilename());
        dto.setContent(content);
        dto.setCategoryIds(categoryIds);
        dto.setTagIds(tagIds);

        NoteVO createdNote = noteService.create(userId, dto);
        Long noteId = createdNote.getId();
        String noteDir = "notes/" + noteId;

        // 解析并处理所有资源
        Map<String, String> replacements = new LinkedHashMap<>();
        int imageCounter = 0;
        int attachCounter = 0;

        Matcher imgMatcher = IMG_PATTERN.matcher(content);
        while (imgMatcher.find()) {
            String src = imgMatcher.group(2).trim();
            if (isLocalResource(src)) continue;
            imageCounter++;
            replacements.put(src, processResource(src, noteDir, "images", imageCounter));
        }

        Matcher linkMatcher = LINK_PATTERN.matcher(content);
        while (linkMatcher.find()) {
            String href = linkMatcher.group(2).trim();
            if (isLocalResource(href) || isTextOrMarkdown(href)) continue;
            attachCounter++;
            replacements.put(href, processResource(href, noteDir, "attachments", attachCounter));
        }

        // 替换 Markdown 内容中的引用
        String updatedContent = content;
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            updatedContent = updatedContent.replace(entry.getKey(), entry.getValue());
        }

        // 写入新的 note.md
        String mdPath = noteDir + "/note.md";
        storageService.write(mdPath, updatedContent);

        // 更新 DB 中的 mdPath（正文只存文件，不同步 DB）
        Note note = noteMapper.selectById(noteId);
        note.setMdPath(mdPath);
        noteMapper.updateById(note);

        log.info("Markdown 导入完成: noteId={}, 资源数量={}", noteId, replacements.size());
        return noteId;
    }

    private String processResource(String src, String noteDir, String type, int counter) {
        String ext = guessExtension(src);
        String prefix = type.equals("images") ? "image" : "attachment";
        String filename = prefix + "-" + String.format("%03d", counter) + "." + ext;
        String targetPath = noteDir + "/" + type + "/" + filename;

        try {
            byte[] data = resolveResource(src);
            if (data != null) {
                storageService.store(data, targetPath);
            }
        } catch (Exception e) {
            log.warn("资源处理失败，跳过: {}", src, e);
        }
        return type + "/" + filename;
    }

    private byte[] resolveResource(String src) throws Exception {
        src = src.trim();
        if (src.startsWith("http://") || src.startsWith("https://")) {
            try {
                return new URI(src).toURL().openStream().readAllBytes();
            } catch (Exception e) {
                log.warn("网络资源下载失败: {}", src);
                return null;
            }
        }
        if (src.startsWith("file://")) {
            return Files.readAllBytes(Paths.get(URI.create(src)));
        }
        if (src.matches("^[A-Za-z]:\\\\.*")) {
            return Files.readAllBytes(Paths.get(src));
        }
        if (src.startsWith("/")) {
            return Files.readAllBytes(Paths.get(src));
        }
        log.warn("相对路径资源无法定位: {}", src);
        return null;
    }

    private boolean isLocalResource(String ref) {
        return ref.startsWith("images/") || ref.startsWith("attachments/");
    }

    private boolean isTextOrMarkdown(String ref) {
        String lower = ref.toLowerCase();
        return lower.endsWith(".md") || lower.endsWith(".txt");
    }

    private String guessExtension(String src) {
        String query = src;
        int qIdx = src.indexOf('?');
        if (qIdx > 0) query = src.substring(0, qIdx);
        int dot = query.lastIndexOf('.');
        if (dot > 0) {
            String ext = query.substring(dot + 1).toLowerCase();
            if (ext.length() <= 5 && ext.matches("[a-z0-9]+")) return ext;
        }
        return "png";
    }
}
