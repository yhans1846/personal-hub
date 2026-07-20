package com.personalhub.knowledge.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.personalhub.common.constant.Flags;
import com.personalhub.common.exception.BusinessException;
import com.personalhub.common.exception.NotFoundException;
import com.personalhub.common.util.EntityGuard;
import com.personalhub.knowledge.entity.Note;
import com.personalhub.knowledge.mapper.NoteMapper;
import com.personalhub.storage.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 笔记导出服务（ZIP 打包）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NoteExportService {

    public static final int MAX_BATCH_SIZE = 50;

    private final NoteMapper noteMapper;
    private final StorageService storageService;

    public byte[] exportNote(Long noteId, Long userId) {
        Note note = EntityGuard.requireOwned(
                noteMapper.selectById(noteId), userId, Note::getUserId, "笔记不存在");

        String noteDir = "notes/" + noteId;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (ZipOutputStream zos = new ZipOutputStream(baos, StandardCharsets.UTF_8)) {
            String mdContent = readNoteMarkdown(noteDir);
            String mdFileName = sanitizeFileName(note.getTitle()) + ".md";
            zos.putNextEntry(new ZipEntry(mdFileName));
            zos.write(mdContent.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();

            addToZip(zos, noteDir + "/images/", "images/");
            addToZip(zos, noteDir + "/attachments/", "attachments/");

        } catch (BusinessException | NotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("笔记导出失败");
        }

        return baos.toByteArray();
    }

    /**
     * 批量导出：每篇独立目录 note.md + images/ + attachments/
     *
     * @param ids    笔记 ID（1–50，去重）
     * @param userId 当前用户
     * @return ZIP 字节
     */
    public byte[] exportNotes(List<Long> ids, Long userId) {
        List<Long> normalized;
        try {
            normalized = normalizeIds(ids);
        } catch (IllegalArgumentException e) {
            throw new BusinessException(e.getMessage());
        }

        List<Note> found = noteMapper.selectList(new LambdaQueryWrapper<Note>()
                .eq(Note::getUserId, userId)
                .eq(Note::getIsDeleted, Flags.NO)
                .in(Note::getId, normalized));
        Map<Long, Note> byId = found.stream()
                .collect(Collectors.toMap(Note::getId, Function.identity(), (a, b) -> a));

        List<Note> ordered = new ArrayList<>();
        for (Long id : normalized) {
            Note note = byId.get(id);
            if (note != null) {
                ordered.add(note);
            }
        }
        if (ordered.isEmpty()) {
            throw new BusinessException("没有可导出的笔记");
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Set<String> usedFolders = new HashSet<>();
        try (ZipOutputStream zos = new ZipOutputStream(baos, StandardCharsets.UTF_8)) {
            for (Note note : ordered) {
                String folder = uniqueFolderName(note.getTitle(), note.getId(), usedFolders);
                String noteDir = "notes/" + note.getId();
                String mdContent = readNoteMarkdown(noteDir);
                zos.putNextEntry(new ZipEntry(folder + "/note.md"));
                zos.write(mdContent.getBytes(StandardCharsets.UTF_8));
                zos.closeEntry();
                addToZip(zos, noteDir + "/images/", folder + "/images/");
                addToZip(zos, noteDir + "/attachments/", folder + "/attachments/");
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("批量导出失败: userId={}, count={}", userId, ordered.size(), e);
            throw new BusinessException("笔记导出失败");
        }
        return baos.toByteArray();
    }

    /**
     * 去重并校验数量（包内可测）。
     */
    static List<Long> normalizeIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("请选择要导出的笔记");
        }
        List<Long> normalized = ids.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.collectingAndThen(
                        Collectors.toCollection(LinkedHashSet::new),
                        ArrayList::new));
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("请选择要导出的笔记");
        }
        if (normalized.size() > MAX_BATCH_SIZE) {
            throw new IllegalArgumentException("单次最多导出" + MAX_BATCH_SIZE + "篇笔记");
        }
        return normalized;
    }

    static String sanitizeFileName(String name) {
        return name != null ? name.replaceAll("[\\\\/:*?\"<>|]", "_") : "untitled";
    }

    static String uniqueFolderName(String title, Long id, Set<String> used) {
        String base = sanitizeFileName(title);
        String folder = base;
        if (used.contains(folder)) {
            folder = base + " (" + id + ")";
        }
        used.add(folder);
        return folder;
    }

    private String readNoteMarkdown(String noteDir) {
        String mdFilePath = noteDir + "/note.md";
        if (storageService.exists(mdFilePath)) {
            return storageService.read(mdFilePath);
        }
        return "";
    }

    private void addToZip(ZipOutputStream zos, String dirPath, String prefix) {
        var files = storageService.listFiles(dirPath);
        for (String name : files) {
            try {
                Resource resource = storageService.load(dirPath + "/" + name);
                try (InputStream in = resource.getInputStream()) {
                    byte[] data = in.readAllBytes();
                    zos.putNextEntry(new ZipEntry(prefix + name));
                    zos.write(data);
                    zos.closeEntry();
                }
            } catch (Exception e) {
                log.warn("导出文件失败: {}/{}", dirPath, name, e);
            }
        }
    }
}
