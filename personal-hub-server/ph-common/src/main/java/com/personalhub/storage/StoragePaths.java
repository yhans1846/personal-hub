package com.personalhub.storage;

/**
 * 业务存储相对路径约定。
 */
public final class StoragePaths {

    public static final String NOTES = "notes";
    public static final String DIARIES = "diaries";
    public static final String AVATARS = "avatars";
    public static final String UPLOADS = "uploads";

    private StoragePaths() {
    }

    public static String noteDir(Long noteId) {
        return NOTES + "/" + noteId;
    }

    public static String noteMarkdown(Long noteId) {
        return noteDir(noteId) + "/note.md";
    }

    public static String noteImage(Long noteId, String filename) {
        return noteDir(noteId) + "/images/" + filename;
    }

    public static String noteAttachment(Long noteId, String filename) {
        return noteDir(noteId) + "/attachments/" + filename;
    }

    public static String diaryImage(Long diaryId, String filename) {
        return DIARIES + "/" + diaryId + "/images/" + filename;
    }

    public static String avatar(String filename) {
        return AVATARS + "/" + filename;
    }

    public static String upload(String dirPrefix, String yearMonth, String storedName) {
        return UPLOADS + "/" + dirPrefix + "/" + yearMonth + "/" + storedName;
    }
}
