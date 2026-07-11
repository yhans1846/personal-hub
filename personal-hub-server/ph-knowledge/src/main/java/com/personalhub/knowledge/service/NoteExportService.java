package com.personalhub.knowledge.service;

import com.personalhub.common.exception.NotFoundException;
import com.personalhub.knowledge.entity.Note;
import com.personalhub.knowledge.mapper.NoteMapper;
import com.personalhub.storage.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 笔记导出服务（ZIP 打包）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NoteExportService {

    private final NoteMapper noteMapper;
    private final StorageService storageService;

    public byte[] exportNote(Long noteId, Long userId) {
        Note note = noteMapper.selectById(noteId);
        if (note == null || !note.getUserId().equals(userId)) {
            throw new NotFoundException("笔记不存在");
        }

        String noteDir = "notes/" + noteId;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (ZipOutputStream zos = new ZipOutputStream(baos, StandardCharsets.UTF_8)) {
            String mdContent;
            String mdPath = noteDir + "/note.md";
            if (storageService.exists(mdPath)) {
                mdContent = storageService.read(mdPath);
            } else {
                mdContent = note.getContent() != null ? note.getContent() : "";
            }

            // 写入 note.md（使用标题作为文件名）
            String mdFileName = sanitizeFileName(note.getTitle()) + ".md";
            zos.putNextEntry(new ZipEntry(mdFileName));
            zos.write(mdContent.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();

            // 写入 images/
            addToZip(zos, noteDir + "/images/", "images/");
            // 写入 attachments/
            addToZip(zos, noteDir + "/attachments/", "attachments/");

        } catch (Exception e) {
            throw new RuntimeException("笔记导出失败", e);
        }

        return baos.toByteArray();
    }

    private void addToZip(ZipOutputStream zos, String dirPath, String prefix) {
        var files = storageService.listFiles(dirPath);
        for (String name : files) {
            try {
                Resource resource = storageService.load(dirPath + "/" + name);
                byte[] data = resource.getInputStream().readAllBytes();
                zos.putNextEntry(new ZipEntry(prefix + name));
                zos.write(data);
                zos.closeEntry();
            } catch (Exception e) {
                log.warn("导出文件失败: {}/{}", dirPath, name, e);
            }
        }
    }

    private String sanitizeFileName(String name) {
        return name != null ? name.replaceAll("[\\\\/:*?\"<>|]", "_") : "untitled";
    }
}
