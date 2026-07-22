package com.personalhub.backup;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.personalhub.common.exception.BusinessException;
import com.personalhub.knowledge.entity.DiaryEntry;
import com.personalhub.knowledge.entity.Note;
import com.personalhub.knowledge.mapper.DiaryEntryMapper;
import com.personalhub.knowledge.mapper.NoteMapper;
import com.personalhub.resource.entity.FileResource;
import com.personalhub.resource.mapper.FileResourceMapper;
import com.personalhub.storage.StorageService;
import com.personalhub.system.service.ImageCaptchaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 验证码通过 → 备份 → 清业务（保留 layout）→ 只留快照备份。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataPurgeServiceImpl implements DataPurgeService {

    private final ImageCaptchaService imageCaptchaService;
    private final DataBackupService dataBackupService;
    private final BackupDataMapper backupDataMapper;
    private final NoteMapper noteMapper;
    private final DiaryEntryMapper diaryEntryMapper;
    private final FileResourceMapper fileResourceMapper;
    private final StorageService storageService;
    private final UserBackupMapper userBackupMapper;

    @Override
    public DataPurgeVO purge(Long userId, String captchaId, String captchaCode) {
        imageCaptchaService.verifyAndConsume(captchaId, captchaCode);

        dataBackupService.createStoredBackup(userId, "MANUAL");
        List<UserBackupVO> afterBackup = dataBackupService.listBackups(userId);
        UserBackupVO snapshot = afterBackup.stream()
                .filter(b -> "OK".equals(b.getStatus()))
                .findFirst()
                .orElseThrow(() -> new BusinessException("备份未生成，已取消清空"));

        log.info("清空数据：快照已生成 backupId={}, userId={}", snapshot.getId(), userId);

        List<String> noteDirs = noteMapper.selectList(
                        new LambdaQueryWrapper<Note>().eq(Note::getUserId, userId).select(Note::getId))
                .stream().map(n -> "notes/" + n.getId()).toList();
        List<String> diaryDirs = diaryEntryMapper.selectList(
                        new LambdaQueryWrapper<DiaryEntry>().eq(DiaryEntry::getUserId, userId)
                                .select(DiaryEntry::getId))
                .stream().map(d -> "diaries/" + d.getId()).toList();
        List<String> filePaths = fileResourceMapper.selectList(
                        new LambdaQueryWrapper<FileResource>().eq(FileResource::getUserId, userId)
                                .select(FileResource::getPath))
                .stream().map(FileResource::getPath).filter(p -> p != null && !p.isBlank()).toList();

        try {
            wipeBusinessData(userId);
        } catch (Exception e) {
            log.error("清空业务数据失败（快照已保留）: userId={}, backupId={}", userId, snapshot.getId(), e);
            if (e instanceof BusinessException be) {
                throw be;
            }
            throw new BusinessException("清空失败，可从备份历史恢复: " + snapshot.getId(), e);
        }

        for (String dir : noteDirs) {
            storageService.delete(dir);
        }
        for (String dir : diaryDirs) {
            storageService.delete(dir);
        }
        for (String path : filePaths) {
            storageService.delete(path);
        }

        for (UserBackupVO b : dataBackupService.listBackups(userId)) {
            if (!b.getId().equals(snapshot.getId())) {
                try {
                    dataBackupService.deleteBackup(userId, b.getId());
                } catch (Exception e) {
                    log.warn("删除旧备份失败: backupId={}, userId={}", b.getId(), userId, e);
                }
            }
        }

        UserBackup kept = userBackupMapper.selectById(snapshot.getId());
        if (kept == null || !"OK".equals(kept.getStatus())) {
            throw new BusinessException("快照备份异常，请检查备份历史");
        }

        log.info("清空数据完成: userId={}, keptBackupId={}", userId, snapshot.getId());
        return DataPurgeVO.builder()
                .backupId(snapshot.getId())
                .fileSize(snapshot.getFileSize())
                .createdAt(snapshot.getCreatedAt())
                .build();
    }

    /** 与备份导入 wipe 顺序一致，但保留 user_layout；并清通知与审计 */
    private void wipeBusinessData(Long userId) {
        backupDataMapper.deleteTagRelsByUser(userId);
        backupDataMapper.deleteNoteCategoryRelsByUser(userId);
        backupDataMapper.deleteNotes(userId);
        backupDataMapper.deleteNoteFolders(userId);
        backupDataMapper.deleteDiaries(userId);
        backupDataMapper.deleteTodos(userId);
        backupDataMapper.deleteBookmarks(userId);
        backupDataMapper.deleteStudyRecords(userId);
        backupDataMapper.deleteStudyPlans(userId);
        backupDataMapper.deleteReadings(userId);
        backupDataMapper.deleteFiles(userId);
        backupDataMapper.deleteCategories(userId);
        backupDataMapper.deleteTags(userId);
        // 刻意不调用 deleteLayouts
        backupDataMapper.deleteNotifications(userId);
        backupDataMapper.deleteAuditLogs(userId);
    }
}
