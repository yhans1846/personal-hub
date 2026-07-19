package com.personalhub.backup;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personalhub.common.exception.BusinessException;
import com.personalhub.knowledge.entity.Category;
import com.personalhub.knowledge.entity.DiaryEntry;
import com.personalhub.knowledge.entity.Note;
import com.personalhub.knowledge.entity.ReadingRecord;
import com.personalhub.knowledge.entity.StudyRecord;
import com.personalhub.knowledge.entity.Tag;
import com.personalhub.knowledge.entity.TagRel;
import com.personalhub.knowledge.mapper.CategoryMapper;
import com.personalhub.knowledge.mapper.DiaryEntryMapper;
import com.personalhub.knowledge.mapper.NoteMapper;
import com.personalhub.knowledge.mapper.ReadingRecordMapper;
import com.personalhub.knowledge.mapper.StudyRecordMapper;
import com.personalhub.knowledge.mapper.TagMapper;
import com.personalhub.knowledge.mapper.TagRelMapper;
import com.personalhub.planning.entity.StudyPlan;
import com.personalhub.planning.entity.TodoTask;
import com.personalhub.planning.mapper.StudyPlanMapper;
import com.personalhub.planning.mapper.TodoTaskMapper;
import com.personalhub.resource.entity.BookmarkUrl;
import com.personalhub.resource.entity.FileResource;
import com.personalhub.resource.mapper.BookmarkUrlMapper;
import com.personalhub.resource.mapper.FileResourceMapper;
import com.personalhub.storage.StorageProperties;
import com.personalhub.storage.StorageService;
import com.personalhub.system.entity.UserLayout;
import com.personalhub.system.mapper.UserLayoutMapper;
import com.personalhub.system.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * 用户数据 ZIP 备份 / 覆盖恢复。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DataBackupServiceImpl implements DataBackupService {

    private static final DateTimeFormatter FILE_TS = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private final ObjectMapper objectMapper;
    private final StorageService storageService;
    private final StorageProperties storageProperties;
    private final BackupDataMapper backupDataMapper;
    private final BackupDatabaseWriter backupDatabaseWriter;
    private final NoteMapper noteMapper;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;
    private final TagRelMapper tagRelMapper;
    private final DiaryEntryMapper diaryEntryMapper;
    private final TodoTaskMapper todoTaskMapper;
    private final BookmarkUrlMapper bookmarkUrlMapper;
    private final StudyRecordMapper studyRecordMapper;
    private final StudyPlanMapper studyPlanMapper;
    private final ReadingRecordMapper readingRecordMapper;
    private final FileResourceMapper fileResourceMapper;
    private final UserLayoutMapper userLayoutMapper;
    private final AuditLogService auditLogService;

    @Override
    public byte[] exportZip(Long userId) {
        List<String> warnings = new ArrayList<>();
        List<String> modules = new ArrayList<>();
        Map<String, Object> dataFiles = new LinkedHashMap<>();

        List<Note> notes = noteMapper.selectList(new LambdaQueryWrapper<Note>().eq(Note::getUserId, userId));
        dataFiles.put("notes.json", notes);
        modules.add("notes");

        dataFiles.put("note_category_rels.json", backupDataMapper.selectNoteCategoryRels(userId));
        modules.add("note_category_rels");

        List<Category> categories = categoryMapper.selectList(
                new LambdaQueryWrapper<Category>().eq(Category::getUserId, userId));
        dataFiles.put("categories.json", categories);
        modules.add("categories");

        List<Tag> tags = tagMapper.selectList(new LambdaQueryWrapper<Tag>().eq(Tag::getUserId, userId));
        dataFiles.put("tags.json", tags);
        modules.add("tags");

        List<Long> tagIds = tags.stream().map(Tag::getId).toList();
        List<TagRel> tagRels = tagIds.isEmpty() ? List.of()
                : tagRelMapper.selectList(new LambdaQueryWrapper<TagRel>().in(TagRel::getTagId, tagIds));
        dataFiles.put("tag_rels.json", tagRels);
        modules.add("tag_rels");

        List<DiaryEntry> diaries = diaryEntryMapper.selectList(
                new LambdaQueryWrapper<DiaryEntry>().eq(DiaryEntry::getUserId, userId));
        dataFiles.put("diaries.json", diaries);
        modules.add("diaries");

        List<TodoTask> todos = todoTaskMapper.selectList(
                new LambdaQueryWrapper<TodoTask>().eq(TodoTask::getUserId, userId));
        dataFiles.put("todos.json", todos);
        modules.add("todos");

        List<BookmarkUrl> bookmarks = bookmarkUrlMapper.selectList(
                new LambdaQueryWrapper<BookmarkUrl>().eq(BookmarkUrl::getUserId, userId));
        dataFiles.put("bookmarks.json", bookmarks);
        modules.add("bookmarks");

        List<StudyRecord> studyRecords = studyRecordMapper.selectList(
                new LambdaQueryWrapper<StudyRecord>().eq(StudyRecord::getUserId, userId));
        dataFiles.put("study_records.json", studyRecords);
        modules.add("study_records");

        List<StudyPlan> studyPlans = studyPlanMapper.selectList(
                new LambdaQueryWrapper<StudyPlan>().eq(StudyPlan::getUserId, userId));
        dataFiles.put("study_plans.json", studyPlans);
        modules.add("study_plans");

        List<ReadingRecord> readings = readingRecordMapper.selectList(
                new LambdaQueryWrapper<ReadingRecord>().eq(ReadingRecord::getUserId, userId));
        dataFiles.put("readings.json", readings);
        modules.add("readings");

        List<FileResource> files = fileResourceMapper.selectList(
                new LambdaQueryWrapper<FileResource>().eq(FileResource::getUserId, userId));
        dataFiles.put("files.json", files);
        modules.add("files");

        List<UserLayout> layouts = userLayoutMapper.selectList(
                new LambdaQueryWrapper<UserLayout>().eq(UserLayout::getUserId, userId));
        dataFiles.put("layouts.json", layouts);
        modules.add("layouts");

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos, StandardCharsets.UTF_8)) {

            for (Map.Entry<String, Object> e : dataFiles.entrySet()) {
                putJson(zos, "data/" + e.getKey(), e.getValue());
            }

            for (Note n : notes) {
                addTreeToZip(zos, "notes/" + n.getId(), warnings);
            }
            for (DiaryEntry d : diaries) {
                addTreeToZip(zos, "diaries/" + d.getId(), warnings);
            }
            for (FileResource f : files) {
                if (f.getPath() == null || f.getPath().isBlank()) {
                    continue;
                }
                if (storageService.exists(f.getPath())) {
                    addFileToZip(zos, f.getPath(), warnings);
                } else {
                    warnings.add("missing file: " + f.getPath());
                }
            }

            BackupManifest manifest = BackupManifest.builder()
                    .schemaVersion(BackupManifest.SCHEMA_VERSION)
                    .app(BackupManifest.APP)
                    .createdAt(LocalDateTime.now().toString())
                    .sourceUserId(userId)
                    .modules(modules)
                    .warnings(warnings)
                    .build();
            putJson(zos, "manifest.json", manifest);

            zos.finish();
            byte[] zip = baos.toByteArray();
            if (zip.length > storageProperties.getMaxBackupSize()) {
                throw new BusinessException("备份包超过大小上限");
            }
            auditLogService.log("BACKUP", userId, "EXPORT",
                    "导出数据备份，约 " + zip.length + " 字节", userId);
            return zip;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("导出备份失败", e);
        }
    }

    @Override
    public void importZip(Long userId, byte[] zipBytes) {
        if (zipBytes == null || zipBytes.length == 0) {
            throw new BusinessException("备份文件为空");
        }
        if (zipBytes.length > storageProperties.getMaxBackupSize()) {
            throw new BusinessException("备份包超过大小上限");
        }

        Path staging = null;
        try {
            staging = Files.createTempDirectory("ph-backup-");
            Map<String, byte[]> dataJson = new LinkedHashMap<>();
            extractZip(zipBytes, staging, dataJson);

            byte[] manifestBytes = dataJson.get("manifest.json");
            if (manifestBytes == null) {
                throw new BusinessException("缺少 manifest.json");
            }
            BackupManifest manifest = objectMapper.readValue(manifestBytes, BackupManifest.class);
            if (manifest.getSchemaVersion() != BackupManifest.SCHEMA_VERSION
                    || !BackupManifest.APP.equals(manifest.getApp())) {
                throw new BusinessException("不支持的备份格式");
            }

            List<String> oldNoteDirs = noteMapper.selectList(
                            new LambdaQueryWrapper<Note>().eq(Note::getUserId, userId).select(Note::getId))
                    .stream().map(n -> "notes/" + n.getId()).toList();
            List<String> oldDiaryDirs = diaryEntryMapper.selectList(
                            new LambdaQueryWrapper<DiaryEntry>().eq(DiaryEntry::getUserId, userId)
                                    .select(DiaryEntry::getId))
                    .stream().map(d -> "diaries/" + d.getId()).toList();
            List<String> oldFilePaths = fileResourceMapper.selectList(
                            new LambdaQueryWrapper<FileResource>().eq(FileResource::getUserId, userId)
                                    .select(FileResource::getPath))
                    .stream().map(FileResource::getPath).filter(p -> p != null && !p.isBlank()).toList();

            backupDatabaseWriter.replaceUserData(userId, dataJson);

            for (String dir : oldNoteDirs) {
                storageService.delete(dir);
            }
            for (String dir : oldDiaryDirs) {
                storageService.delete(dir);
            }
            for (String path : oldFilePaths) {
                storageService.delete(path);
            }

            Path filesRoot = staging.resolve("files");
            if (Files.isDirectory(filesRoot)) {
                copyTreeIntoStorage(filesRoot);
            }

            auditLogService.log("BACKUP", userId, "RESTORE",
                    "从备份恢复数据（覆盖）", userId);
            log.info("用户数据恢复完成: userId={}", userId);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("导入备份失败", e);
        } finally {
            if (staging != null) {
                deleteRecursively(staging);
            }
        }
    }

    private void extractZip(byte[] zipBytes, Path staging, Map<String, byte[]> dataJson) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes), StandardCharsets.UTF_8)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName().replace('\\', '/');
                if (entry.isDirectory()) {
                    String dirName = name.endsWith("/") ? name : name + "/";
                    BackupZipSupport.assertSafeEntryName(dirName);
                    continue;
                }
                BackupZipSupport.assertSafeEntryName(name);
                byte[] content = zis.readAllBytes();
                if ("manifest.json".equals(name)) {
                    dataJson.put("manifest.json", content);
                } else if (name.startsWith("data/")) {
                    dataJson.put(name, content);
                    dataJson.put(name.substring("data/".length()), content);
                } else if (name.startsWith("files/")) {
                    Path target = staging.resolve(name).normalize();
                    if (!target.startsWith(staging.normalize())) {
                        throw new BusinessException("非法备份路径: " + name);
                    }
                    Files.createDirectories(target.getParent());
                    Files.write(target, content);
                }
                zis.closeEntry();
            }
        }
    }

    private void copyTreeIntoStorage(Path filesRoot) throws IOException {
        Path root = storageRoot();
        Files.walkFileTree(filesRoot, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path rel = filesRoot.relativize(file);
                String relative = rel.toString().replace('\\', '/');
                BackupZipSupport.assertSafeEntryName("files/" + relative);
                Path dest = root.resolve(relative).normalize();
                if (!dest.startsWith(root)) {
                    throw new BusinessException("非法存储路径: " + relative);
                }
                Files.createDirectories(dest.getParent());
                Files.copy(file, dest, StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private void putJson(ZipOutputStream zos, String name, Object value) throws IOException {
        zos.putNextEntry(new ZipEntry(name));
        zos.write(objectMapper.writeValueAsBytes(value));
        zos.closeEntry();
    }

    private void addTreeToZip(ZipOutputStream zos, String relativeDir, List<String> warnings) {
        Path dir = storageRoot().resolve(relativeDir).normalize();
        if (!dir.startsWith(storageRoot())) {
            throw new BusinessException("非法存储路径");
        }
        if (!Files.isDirectory(dir)) {
            return;
        }
        try {
            Files.walkFileTree(dir, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    String rel = storageRoot().relativize(file).toString().replace('\\', '/');
                    addFileToZip(zos, rel, warnings);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            warnings.add("walk failed: " + relativeDir);
            log.warn("遍历目录失败: {}", relativeDir, e);
        }
    }

    private void addFileToZip(ZipOutputStream zos, String relativePath, List<String> warnings) {
        try {
            if (!storageService.exists(relativePath)) {
                warnings.add("missing file: " + relativePath);
                return;
            }
            try (InputStream in = storageService.load(relativePath).getInputStream()) {
                zos.putNextEntry(new ZipEntry("files/" + relativePath));
                in.transferTo(zos);
                zos.closeEntry();
            }
        } catch (Exception e) {
            warnings.add("read failed: " + relativePath);
            log.warn("打包文件失败: {}", relativePath, e);
        }
    }

    private Path storageRoot() {
        return storageProperties.getLocation().toAbsolutePath().normalize();
    }

    private static void deleteRecursively(Path root) {
        if (root == null || !Files.exists(root)) {
            return;
        }
        try {
            Files.walkFileTree(root, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.deleteIfExists(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.deleteIfExists(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            log.debug("清理临时目录失败: {}", root, e);
        }
    }

    public static String suggestFileName() {
        return "personal-hub-backup-" + LocalDateTime.now().format(FILE_TS) + ".zip";
    }
}
