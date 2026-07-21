package com.personalhub.knowledge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.personalhub.common.constant.EntityType;
import com.personalhub.common.constant.Flags;
import com.personalhub.common.exception.BusinessException;
import com.personalhub.common.exception.NotFoundException;
import com.personalhub.common.util.EntityGuard;
import com.personalhub.knowledge.dto.NoteCreateDTO;
import com.personalhub.knowledge.dto.NoteQueryDTO;
import com.personalhub.knowledge.entity.Category;
import com.personalhub.knowledge.entity.Note;
import com.personalhub.knowledge.enums.NoteDeleteReason;
import com.personalhub.knowledge.mapper.CategoryMapper;
import com.personalhub.knowledge.mapper.NoteMapper;
import com.personalhub.knowledge.service.NoteService;
import com.personalhub.knowledge.service.TagService;
import com.personalhub.knowledge.vo.NoteBacklinkVO;
import com.personalhub.knowledge.vo.NoteVO;
import com.personalhub.knowledge.vo.RecycleEmptyVO;
import com.personalhub.knowledge.vo.TagVO;
import com.personalhub.storage.StorageService;
import com.personalhub.system.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 笔记服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {

    private static final int EXCERPT_MAX_LEN = 200;
    /** 回链扫描最多读取的笔记篇数 */
    private static final int BACKLINK_SCAN_LIMIT = 500;

    private final NoteMapper noteMapper;
    private final CategoryMapper categoryMapper;
    private final TagService tagService;
    private final StorageService storageService;
    private final AuditLogService auditLogService;

    @Override
    public IPage<NoteVO> listNotes(Long userId, NoteQueryDTO query) {
        Page<Note> page = new Page<>(query.getPage(), query.getSize());
        IPage<Note> notePage = noteMapper.selectNotePage(page, userId, query);
        return toVoPage(notePage);
    }

    @Override
    public NoteVO getById(Long id, Long userId) {
        Note note = EntityGuard.requireOwned(
                noteMapper.selectById(id), userId, Note::getUserId, "笔记不存在");
        if (Integer.valueOf(Flags.YES).equals(note.getIsDeleted())) {
            log.warn("笔记不存在或无权访问: id={}, userId={}", id, userId);
            throw new NotFoundException("笔记不存在");
        }
        NoteVO vo = NoteVO.from(note);
        vo.setContent(readContent(note));
        vo.setCategories(getCategories(id));
        vo.setTags(getTags(id, userId));
        return vo;
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "categories", allEntries = true)
    public NoteVO create(Long userId, NoteCreateDTO dto) {
        var note = Note.builder()
                .userId(userId)
                .title(dto.getTitle())
                .excerpt(buildExcerpt(dto.getContent()))
                .build();
        noteMapper.insert(note);
        log.info("新建笔记: id={}, userId={}, title={}", note.getId(), userId, dto.getTitle());

        String mdPath = "notes/" + note.getId() + "/note.md";
        if (dto.getContent() != null && !dto.getContent().isBlank()) {
            storageService.write(mdPath, dto.getContent());
        }
        note.setMdPath(mdPath);
        noteMapper.updateById(note);

        saveCategoryRels(userId, note.getId(), dto.getCategoryIds());
        saveTagRels(userId, note.getId(), dto.getTagIds());

        return getById(note.getId(), userId);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "categories", allEntries = true)
    public NoteVO update(Long id, Long userId, NoteCreateDTO dto) {
        Note note = EntityGuard.requireOwned(
                noteMapper.selectById(id), userId, Note::getUserId, "笔记不存在");
        if (Integer.valueOf(Flags.YES).equals(note.getIsDeleted())) {
            log.warn("编辑笔记不存在或无权访问: id={}, userId={}", id, userId);
            throw new NotFoundException("笔记不存在");
        }
        note.setTitle(dto.getTitle());
        if (dto.getContent() != null) {
            note.setExcerpt(buildExcerpt(dto.getContent()));
        }
        noteMapper.updateById(note);

        if (note.getMdPath() != null && dto.getContent() != null) {
            storageService.write(note.getMdPath(), dto.getContent());
        }

        log.info("编辑笔记: id={}, userId={}", id, userId);

        noteMapper.deleteCategoryRelsByNoteId(id);
        saveCategoryRels(userId, id, dto.getCategoryIds());
        tagService.bindTags(userId, id, EntityType.NOTE, dto.getTagIds());

        return getById(id, userId);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "categories", allEntries = true)
    public void delete(Long id, Long userId) {
        softDelete(id, userId, NoteDeleteReason.USER_DELETE, "DELETE", "删除笔记");
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "categories", allEntries = true)
    public void archive(Long id, Long userId) {
        softDelete(id, userId, NoteDeleteReason.AUTO_ARCHIVE, "ARCHIVE", "归档笔记");
    }

    private void softDelete(Long id, Long userId, NoteDeleteReason reason, String auditAction, String auditVerb) {
        Note note = EntityGuard.requireOwned(
                noteMapper.selectById(id), userId, Note::getUserId, "笔记不存在");
        if (Integer.valueOf(Flags.YES).equals(note.getIsDeleted())) {
            throw new NotFoundException("笔记不存在");
        }
        LocalDateTime now = LocalDateTime.now();
        note.setIsDeleted(Flags.YES);
        note.setDeletedAt(now);
        note.setDeleteReason(reason.getCode());
        noteMapper.updateById(note);
        auditLogService.log("NOTE", id, auditAction,
                auditVerb + "：《" + note.getTitle() + "》", userId);
        log.info("笔记移入回收站: id={}, userId={}, reason={}, deletedAt={}", id, userId, reason.getCode(), now);
    }

    @Override
    @Transactional
    public void restore(Long id, Long userId) {
        Note note = EntityGuard.requireOwned(
                noteMapper.selectById(id), userId, Note::getUserId, "笔记不存在");
        noteMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Note>()
                .eq(Note::getId, id)
                .set(Note::getIsDeleted, Flags.NO)
                .set(Note::getDeletedAt, null)
                .set(Note::getDeleteReason, null));
        auditLogService.log("NOTE", id, "RESTORE",
                "恢复笔记：《" + note.getTitle() + "》", userId);
        log.info("笔记恢复: id={}, userId={}", id, userId);
    }

    @Override
    @CacheEvict(cacheNames = "categories", allEntries = true)
    public void permanentDelete(Long id, Long userId) {
        Note note = EntityGuard.requireOwned(
                noteMapper.selectById(id), userId, Note::getUserId, "笔记不存在");
        noteMapper.deleteCategoryRelsByNoteId(id);
        tagService.unbindAll(userId, EntityType.NOTE, id);
        if (note.getMdPath() != null) {
            String noteDir = note.getMdPath().substring(0, note.getMdPath().indexOf("/note.md"));
            storageService.delete(noteDir);
        }
        noteMapper.deleteById(id);
        log.info("笔记永久删除: id={}, userId={}", id, userId);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "categories", allEntries = true)
    public RecycleEmptyVO emptyRecycleBin(Long userId) {
        List<Note> deleted = noteMapper.selectList(new LambdaQueryWrapper<Note>()
                .eq(Note::getUserId, userId)
                .eq(Note::getIsDeleted, Flags.YES)
                .select(Note::getId));
        int count = 0;
        for (Note note : deleted) {
            permanentDelete(note.getId(), userId);
            count++;
        }
        log.info("清空回收站: userId={}, deleted={}", userId, count);
        return new RecycleEmptyVO(count);
    }

    @Override
    public void toggleFavorite(Long id, Long userId) {
        Note note = EntityGuard.requireOwned(
                noteMapper.selectById(id), userId, Note::getUserId, "笔记不存在");
        if (Integer.valueOf(Flags.YES).equals(note.getIsDeleted())) {
            log.warn("切换收藏笔记不存在或无权访问: id={}, userId={}", id, userId);
            throw new NotFoundException("笔记不存在");
        }
        note.setIsFavorite(Integer.valueOf(Flags.YES).equals(note.getIsFavorite()) ? Flags.NO : Flags.YES);
        noteMapper.updateById(note);
        log.info("切换笔记收藏: id={}, userId={}, isFavorite={}", id, userId, note.getIsFavorite());
    }

    @Override
    public IPage<NoteVO> getRecycleList(Long userId, NoteQueryDTO query) {
        Page<Note> page = new Page<>(query.getPage(), query.getSize());
        IPage<Note> notePage = noteMapper.selectRecyclePage(page, userId, query);
        return toVoPage(notePage);
    }

    @Override
    public NoteVO getPreview(Long id, Long userId) {
        Note note = EntityGuard.requireOwned(
                noteMapper.selectById(id), userId, Note::getUserId, "笔记不存在");
        NoteVO vo = NoteVO.from(note);
        vo.setContent(readContent(note));
        vo.setCategories(getCategories(id));
        vo.setTags(getTags(id, userId));
        return vo;
    }

    @Override
    public List<NoteBacklinkVO> listBacklinks(Long id, Long userId) {
        Note target = EntityGuard.requireOwned(
                noteMapper.selectById(id), userId, Note::getUserId, "笔记不存在");
        if (Integer.valueOf(Flags.YES).equals(target.getIsDeleted())) {
            throw new NotFoundException("笔记不存在");
        }
        String targetTitle = target.getTitle() != null ? target.getTitle().trim() : "";
        if (targetTitle.isEmpty()) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Note::getUserId, userId)
                .eq(Note::getIsDeleted, Flags.NO)
                .ne(Note::getId, id)
                .orderByDesc(Note::getUpdatedAt)
                .last("LIMIT " + BACKLINK_SCAN_LIMIT);
        List<Note> candidates = noteMapper.selectList(wrapper);
        if (candidates == null || candidates.isEmpty()) {
            return Collections.emptyList();
        }

        Pattern linkPattern = wikiLinkPattern(targetTitle);
        List<NoteBacklinkVO> result = new ArrayList<>();
        for (Note note : candidates) {
            String content = readContent(note);
            if (content == null || content.isEmpty()) {
                continue;
            }
            if (linkPattern.matcher(content).find()) {
                result.add(new NoteBacklinkVO(note.getId(), note.getTitle()));
            }
        }
        result.sort(Comparator.comparing(
                NoteBacklinkVO::getTitle,
                Comparator.nullsLast(String::compareToIgnoreCase)));
        return result;
    }

    /** 匹配 [[title]] 或 [[title|alias]]（标题精确） */
    static Pattern wikiLinkPattern(String title) {
        String quoted = Pattern.quote(title.trim());
        return Pattern.compile("\\[\\[\\s*" + quoted + "\\s*(?:\\|[^\\]]*)?\\]\\]");
    }

    @Override
    public IPage<NoteVO> getRecent(Long userId, int page, int size) {
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Note::getUserId, userId)
                .eq(Note::getIsDeleted, Flags.NO)
                .orderByDesc(Note::getUpdatedAt);
        Page<Note> p = new Page<>(page, size);
        IPage<Note> notePage = noteMapper.selectPage(p, wrapper);
        return toVoPage(notePage);
    }

    // ========== 辅助方法 ==========

    private IPage<NoteVO> toVoPage(IPage<Note> notePage) {
        List<Note> notes = notePage.getRecords();
        if (notes == null || notes.isEmpty()) {
            return notePage.convert(NoteVO::from);
        }

        List<Long> noteIds = notes.stream().map(Note::getId).collect(Collectors.toList());
        Map<Long, List<NoteVO.CategoryItem>> categoriesMap = batchCategories(noteIds);
        Long userId = notes.get(0).getUserId();
        Map<Long, List<TagVO>> tagsMap = tagService.getTagsMap(userId, EntityType.NOTE, noteIds);

        return notePage.convert(note -> {
            NoteVO vo = NoteVO.from(note);
            vo.setCategories(categoriesMap.getOrDefault(note.getId(), Collections.emptyList()));
            List<TagVO> tagVOs = tagsMap.getOrDefault(note.getId(), Collections.emptyList());
            vo.setTags(toTagItems(tagVOs));
            return vo;
        });
    }

    private Map<Long, List<NoteVO.CategoryItem>> batchCategories(List<Long> noteIds) {
        List<Map<String, Object>> rows = noteMapper.selectCategoriesByNoteIds(noteIds);
        Map<Long, List<NoteVO.CategoryItem>> map = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Long noteId = toLong(row.get("noteId"));
            if (noteId == null) {
                noteId = toLong(row.get("noteid"));
            }
            if (noteId == null) {
                continue;
            }
            NoteVO.CategoryItem item = new NoteVO.CategoryItem();
            item.setId(toLong(row.get("id")));
            Object name = row.get("name");
            item.setName(name != null ? name.toString() : null);
            map.computeIfAbsent(noteId, k -> new ArrayList<>()).add(item);
        }
        return map;
    }

    private static Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Long l) {
            return l;
        }
        if (value instanceof Number n) {
            return n.longValue();
        }
        return Long.parseLong(value.toString());
    }

    private String readContent(Note note) {
        if (note.getMdPath() != null && storageService.exists(note.getMdPath())) {
            try {
                return storageService.read(note.getMdPath());
            } catch (Exception e) {
                log.warn("读取 note.md 失败: id={}", note.getId());
            }
        }
        return "";
    }

    /** 从 Markdown 生成列表摘要（纯文本截断） */
    static String buildExcerpt(String content) {
        if (content == null || content.isBlank()) {
            return "";
        }
        String plain = content
                .replaceAll("(?m)^#{1,6}\\s+", "")
                .replaceAll("[*`_~\\[\\]#>-]", " ")
                .replaceAll("\\s+", " ")
                .trim();
        if (plain.length() <= EXCERPT_MAX_LEN) {
            return plain;
        }
        return plain.substring(0, EXCERPT_MAX_LEN);
    }

    private void saveCategoryRels(Long userId, Long noteId, List<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return;
        }
        for (Long categoryId : categoryIds) {
            Category category = EntityGuard.requireOwned(
                    categoryMapper.selectById(categoryId), userId, Category::getUserId, "分类不存在");
            if (!EntityType.NOTE.equals(category.getType())) {
                throw new BusinessException("分类类型不匹配");
            }
        }
        noteMapper.insertCategoryRels(noteId, categoryIds);
    }

    private void saveTagRels(Long userId, Long noteId, List<Long> tagIds) {
        tagService.bindTags(userId, noteId, EntityType.NOTE, tagIds);
    }

    private List<NoteVO.CategoryItem> getCategories(Long noteId) {
        List<Map<String, Object>> rows = noteMapper.selectCategoriesByNoteId(noteId);
        if (rows.isEmpty()) return Collections.emptyList();
        return rows.stream().map(row -> {
            NoteVO.CategoryItem item = new NoteVO.CategoryItem();
            item.setId(toLong(row.get("id")));
            Object name = row.get("name");
            item.setName(name != null ? name.toString() : null);
            return item;
        }).collect(Collectors.toList());
    }

    private List<NoteVO.TagItem> getTags(Long noteId, Long userId) {
        return toTagItems(tagService.getTags(userId, EntityType.NOTE, noteId));
    }

    private List<NoteVO.TagItem> toTagItems(List<TagVO> tagVOs) {
        if (tagVOs == null || tagVOs.isEmpty()) return Collections.emptyList();
        return tagVOs.stream().map(vo -> {
            NoteVO.TagItem item = new NoteVO.TagItem();
            item.setId(vo.getId());
            item.setName(vo.getName());
            item.setColor(vo.getColor());
            return item;
        }).collect(Collectors.toList());
    }
}
