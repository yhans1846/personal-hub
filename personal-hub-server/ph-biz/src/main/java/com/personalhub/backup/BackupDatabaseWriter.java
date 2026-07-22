package com.personalhub.backup;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personalhub.knowledge.entity.Category;
import com.personalhub.knowledge.entity.DiaryEntry;
import com.personalhub.knowledge.entity.Note;
import com.personalhub.knowledge.entity.NoteFolder;
import com.personalhub.knowledge.entity.ReadingRecord;
import com.personalhub.knowledge.entity.StudyRecord;
import com.personalhub.knowledge.entity.Tag;
import com.personalhub.knowledge.entity.TagRel;
import com.personalhub.knowledge.mapper.CategoryMapper;
import com.personalhub.knowledge.mapper.DiaryEntryMapper;
import com.personalhub.knowledge.mapper.NoteFolderMapper;
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
import com.personalhub.system.entity.User;
import com.personalhub.system.entity.UserLayout;
import com.personalhub.system.mapper.UserLayoutMapper;
import com.personalhub.system.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 备份导入的库表写入（独立 Bean，保证事务生效）。
 */
@Service
@RequiredArgsConstructor
public class BackupDatabaseWriter {

    private final ObjectMapper objectMapper;
    private final BackupDataMapper backupDataMapper;
    private final NoteMapper noteMapper;
    private final NoteFolderMapper noteFolderMapper;
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
    private final UserMapper userMapper;

    @Transactional
    public void replaceUserData(Long userId, Map<String, byte[]> dataJson) throws IOException {
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
        backupDataMapper.deleteLayouts(userId);

        insertAll(dataJson, "categories.json", new TypeReference<List<Category>>() {
        }, list -> {
            for (Category c : list) {
                c.setUserId(userId);
                categoryMapper.insert(c);
            }
        });
        insertAll(dataJson, "tags.json", new TypeReference<List<Tag>>() {
        }, list -> {
            for (Tag t : list) {
                t.setUserId(userId);
                tagMapper.insert(t);
            }
        });
        insertAll(dataJson, "note_folders.json", new TypeReference<List<NoteFolder>>() {
        }, list -> {
            for (NoteFolder f : list) {
                f.setUserId(userId);
                noteFolderMapper.insert(f);
            }
        });
        insertAll(dataJson, "notes.json", new TypeReference<List<Note>>() {
        }, list -> {
            for (Note n : list) {
                n.setUserId(userId);
                noteMapper.insert(n);
            }
        });
        insertAll(dataJson, "diaries.json", new TypeReference<List<DiaryEntry>>() {
        }, list -> {
            for (DiaryEntry d : list) {
                d.setUserId(userId);
                diaryEntryMapper.insert(d);
            }
        });
        insertAll(dataJson, "todos.json", new TypeReference<List<TodoTask>>() {
        }, list -> {
            for (TodoTask t : list) {
                t.setUserId(userId);
                todoTaskMapper.insert(t);
            }
        });
        insertAll(dataJson, "bookmarks.json", new TypeReference<List<BookmarkUrl>>() {
        }, list -> {
            for (BookmarkUrl b : list) {
                b.setUserId(userId);
                bookmarkUrlMapper.insert(b);
            }
        });
        insertAll(dataJson, "study_records.json", new TypeReference<List<StudyRecord>>() {
        }, list -> {
            for (StudyRecord s : list) {
                s.setUserId(userId);
                studyRecordMapper.insert(s);
            }
        });
        insertAll(dataJson, "study_plans.json", new TypeReference<List<StudyPlan>>() {
        }, list -> {
            for (StudyPlan s : list) {
                s.setUserId(userId);
                studyPlanMapper.insert(s);
            }
        });
        insertAll(dataJson, "readings.json", new TypeReference<List<ReadingRecord>>() {
        }, list -> {
            for (ReadingRecord r : list) {
                r.setUserId(userId);
                readingRecordMapper.insert(r);
            }
        });
        insertAll(dataJson, "files.json", new TypeReference<List<FileResource>>() {
        }, list -> {
            for (FileResource f : list) {
                f.setUserId(userId);
                fileResourceMapper.insert(f);
            }
        });
        insertAll(dataJson, "note_category_rels.json", new TypeReference<List<NoteCategoryRelRow>>() {
        }, list -> {
            for (NoteCategoryRelRow r : list) {
                backupDataMapper.insertNoteCategoryRel(r.getNoteId(), r.getCategoryId());
            }
        });
        insertAll(dataJson, "tag_rels.json", new TypeReference<List<TagRel>>() {
        }, list -> {
            for (TagRel r : list) {
                tagRelMapper.insert(r);
            }
        });
        insertAll(dataJson, "layouts.json", new TypeReference<List<UserLayout>>() {
        }, list -> {
            for (UserLayout layout : list) {
                layout.setUserId(userId);
                layout.setIsDeleted(0);
                userLayoutMapper.insert(layout);
            }
        });

        byte[] profileRaw = dataJson.get("data/profile.json");
        if (profileRaw == null) {
            profileRaw = dataJson.get("profile.json");
        }
        if (profileRaw != null) {
            UserProfileBackup profile = objectMapper.readValue(profileRaw, UserProfileBackup.class);
            if (profile != null) {
                userMapper.update(null, new LambdaUpdateWrapper<User>()
                        .eq(User::getId, userId)
                        .set(User::getNickname, profile.getNickname())
                        .set(User::getAvatar, profile.getAvatar())
                        .set(User::getEmail, profile.getEmail())
                        .set(User::getGender, profile.getGender())
                        .set(User::getBirthday, profile.getBirthday())
                        .set(User::getPhone, profile.getPhone())
                        .set(User::getCountry, profile.getCountry())
                        .set(User::getProvince, profile.getProvince())
                        .set(User::getCity, profile.getCity())
                        .set(User::getDistrict, profile.getDistrict())
                        .set(User::getWebsite, profile.getWebsite())
                        .set(User::getGithub, profile.getGithub())
                        .set(User::getBio, profile.getBio()));
            }
        }
    }

    private <T> void insertAll(Map<String, byte[]> dataJson, String key,
                               TypeReference<List<T>> type, ThrowingConsumer<List<T>> consumer)
            throws IOException {
        byte[] raw = dataJson.get("data/" + key);
        if (raw == null) {
            raw = dataJson.get(key);
        }
        if (raw == null) {
            return;
        }
        List<T> list = objectMapper.readValue(raw, type);
        if (list == null || list.isEmpty()) {
            return;
        }
        consumer.accept(list);
    }

    @FunctionalInterface
    private interface ThrowingConsumer<T> {
        void accept(T t) throws IOException;
    }
}
