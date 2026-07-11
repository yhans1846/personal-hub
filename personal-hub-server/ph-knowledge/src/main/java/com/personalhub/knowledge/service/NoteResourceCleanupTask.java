package com.personalhub.knowledge.service;

import com.personalhub.knowledge.mapper.NoteMapper;
import com.personalhub.storage.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 定时清理孤立笔记资源
 * 删除 DB 中已不存在的笔记对应的 notes/{id}/ 目录
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NoteResourceCleanupTask {

    private final StorageService storageService;
    private final NoteMapper noteMapper;

    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanupOrphanNoteResources() {
        log.info("开始清理孤立笔记资源...");
        var noteDirs = storageService.listFiles("notes/");
        int cleaned = 0;
        for (String dirName : noteDirs) {
            try {
                Long noteId = Long.parseLong(dirName);
                var note = noteMapper.selectById(noteId);
                if (note == null || note.getIsDeleted() == 1) {
                    storageService.delete("notes/" + noteId);
                    log.info("清理孤立笔记资源目录: notes/{}", noteId);
                    cleaned++;
                }
            } catch (NumberFormatException ignored) {
                // 非数字目录名，跳过
            }
        }
        log.info("孤立资源清理完成，共清理 {} 个目录", cleaned);
    }
}
