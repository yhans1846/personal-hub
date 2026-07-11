package com.personalhub.knowledge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.personalhub.common.exception.NotFoundException;
import com.personalhub.knowledge.dto.NoteCreateDTO;
import com.personalhub.knowledge.dto.NoteQueryDTO;
import com.personalhub.knowledge.entity.Note;
import com.personalhub.knowledge.entity.Category;
import com.personalhub.knowledge.mapper.CategoryMapper;
import com.personalhub.knowledge.mapper.NoteMapper;
import com.personalhub.knowledge.service.NoteService;
import com.personalhub.knowledge.service.TagService;
import com.personalhub.knowledge.vo.NoteVO;
import com.personalhub.knowledge.vo.TagVO;
import com.personalhub.storage.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 笔记服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {

    private final NoteMapper noteMapper;
    private final CategoryMapper categoryMapper;
    private final TagService tagService;
    private final JdbcTemplate jdbcTemplate;
    private final StorageService storageService;

    @Override
    public IPage<NoteVO> listNotes(Long userId, NoteQueryDTO query) {
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Note::getUserId, userId);

        // 搜索关键词
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.like(Note::getTitle, query.getKeyword());
        }
        // 收藏筛选
        if (query.getIsFavorite() != null && query.getIsFavorite()) {
            wrapper.eq(Note::getIsFavorite, 1);
        }
        // 回收站筛选
        boolean isDeleted = query.getIsDeleted() != null && query.getIsDeleted();
        wrapper.eq(Note::getIsDeleted, isDeleted ? 1 : 0);

        // 分类筛选：通过关联表子查询
        if (query.getCategoryId() != null) {
            wrapper.exists("SELECT 1 FROM note_category_rel WHERE note_id = id AND category_id = " + query.getCategoryId());
        }
        // 标签筛选
        if (query.getTagId() != null) {
            wrapper.exists("SELECT 1 FROM tag_rel WHERE entity_id = id AND entity_type = 'note' AND tag_id = " + query.getTagId());
        }

        wrapper.orderByDesc(Note::getUpdatedAt);

        Page<Note> page = new Page<>(query.getPage(), query.getSize());
        IPage<Note> notePage = noteMapper.selectPage(page, wrapper);

        // 转换为 VO（批量填充分类和标签）
        return notePage.convert(note -> {
            NoteVO vo = NoteVO.from(note);
            vo.setCategories(getCategories(note.getId()));
            vo.setTags(getTags(note.getId()));
            return vo;
        });
    }

    @Override
    public NoteVO getById(Long id, Long userId) {
        Note note = noteMapper.selectById(id);
        if (note == null || !note.getUserId().equals(userId) || note.getIsDeleted() == 1) {
            log.warn("笔记不存在或无权访问: id={}, userId={}", id, userId);
            throw new NotFoundException("笔记不存在");
        }
        NoteVO vo = NoteVO.from(note);
        // 从文件读取 content
        if (note.getMdPath() != null && storageService.exists(note.getMdPath())) {
            try {
                vo.setContent(storageService.read(note.getMdPath()));
            } catch (Exception e) {
                log.warn("读取 note.md 失败: id={}", id);
                vo.setContent("");
            }
        } else {
            vo.setContent("");
        }
        vo.setCategories(getCategories(id));
        vo.setTags(getTags(id));
        return vo;
    }

    @Override
    @Transactional
    public NoteVO create(Long userId, NoteCreateDTO dto) {
        Note note = new Note();
        note.setUserId(userId);
        note.setTitle(dto.getTitle());
        noteMapper.insert(note);
        log.info("新建笔记: id={}, userId={}, title={}", note.getId(), userId, dto.getTitle());

        // 笔记正文存文件，DB 只存路径
        String mdPath = "notes/" + note.getId() + "/note.md";
        if (dto.getContent() != null && !dto.getContent().isBlank()) {
            storageService.write(mdPath, dto.getContent());
        }
        note.setMdPath(mdPath);
        noteMapper.updateById(note);

        // 保存分类关联
        saveCategoryRels(note.getId(), dto.getCategoryIds());
        // 保存标签关联
        saveTagRels(note.getId(), dto.getTagIds());

        return getById(note.getId(), userId);
    }

    @Override
    @Transactional
    public NoteVO update(Long id, Long userId, NoteCreateDTO dto) {
        Note note = noteMapper.selectById(id);
        if (note == null || !note.getUserId().equals(userId) || note.getIsDeleted() == 1) {
            log.warn("编辑笔记不存在或无权访问: id={}, userId={}", id, userId);
            throw new NotFoundException("笔记不存在");
        }
        note.setTitle(dto.getTitle());
        noteMapper.updateById(note);

        // 正文只写文件，不同步 DB
        if (note.getMdPath() != null && dto.getContent() != null) {
            storageService.write(note.getMdPath(), dto.getContent());
        }

        log.info("编辑笔记: id={}, userId={}", id, userId);

        // 重建关联
        jdbcTemplate.update("DELETE FROM note_category_rel WHERE note_id = ?", id);
        saveCategoryRels(id, dto.getCategoryIds());
        tagService.bindTags(id, "note", dto.getTagIds());

        return getById(id, userId);
    }

    @Override
    public void delete(Long id, Long userId) {
        Note note = noteMapper.selectById(id);
        if (note == null || !note.getUserId().equals(userId)) {
            log.warn("删除笔记不存在或无权访问: id={}, userId={}", id, userId);
            throw new NotFoundException("笔记不存在");
        }
        note.setIsDeleted(1);
        noteMapper.updateById(note);
        log.info("笔记移入回收站: id={}, userId={}", id, userId);
    }

    @Override
    public void restore(Long id, Long userId) {
        Note note = noteMapper.selectById(id);
        if (note == null || !note.getUserId().equals(userId)) {
            log.warn("恢复笔记不存在或无权访问: id={}, userId={}", id, userId);
            throw new NotFoundException("笔记不存在");
        }
        note.setIsDeleted(0);
        noteMapper.updateById(note);
        log.info("笔记恢复: id={}, userId={}", id, userId);
    }

    @Override
    public void permanentDelete(Long id, Long userId) {
        Note note = noteMapper.selectById(id);
        if (note == null || !note.getUserId().equals(userId)) {
            log.warn("永久删除笔记不存在或无权访问: id={}, userId={}", id, userId);
            throw new NotFoundException("笔记不存在");
        }
        // 清除关联
        jdbcTemplate.update("DELETE FROM note_category_rel WHERE note_id = ?", id);
        tagService.unbindAll("note", id);
        // 删除笔记资源目录
        if (note.getMdPath() != null) {
            String noteDir = note.getMdPath().substring(0, note.getMdPath().indexOf("/note.md"));
            storageService.delete(noteDir);
        }
        // 物理删除
        noteMapper.deleteById(id);
        log.info("笔记永久删除: id={}, userId={}", id, userId);
    }

    @Override
    public void toggleFavorite(Long id, Long userId) {
        Note note = noteMapper.selectById(id);
        if (note == null || !note.getUserId().equals(userId) || note.getIsDeleted() == 1) {
            log.warn("切换收藏笔记不存在或无权访问: id={}, userId={}", id, userId);
            throw new NotFoundException("笔记不存在");
        }
        note.setIsFavorite(note.getIsFavorite() == 1 ? 0 : 1);
        noteMapper.updateById(note);
        log.info("切换笔记收藏: id={}, userId={}, isFavorite={}", id, userId, note.getIsFavorite());
    }

    @Override
    public IPage<NoteVO> getRecent(Long userId, int page, int size) {
        LambdaQueryWrapper<Note> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Note::getUserId, userId)
                .eq(Note::getIsDeleted, 0)
                .orderByDesc(Note::getUpdatedAt);
        Page<Note> p = new Page<>(page, size);
        IPage<Note> notePage = noteMapper.selectPage(p, wrapper);
        return notePage.convert(note -> {
            NoteVO vo = NoteVO.from(note);
            vo.setCategories(getCategories(note.getId()));
            vo.setTags(getTags(note.getId()));
            return vo;
        });
    }

    // ========== 辅助方法 ==========

    private void saveCategoryRels(Long noteId, List<Long> categoryIds) {
        if (categoryIds != null && !categoryIds.isEmpty()) {
            for (Long cid : categoryIds) {
                jdbcTemplate.update("INSERT INTO note_category_rel (note_id, category_id) VALUES (?, ?)", noteId, cid);
            }
        }
    }

    private void saveTagRels(Long noteId, List<Long> tagIds) {
        tagService.bindTags(noteId, "note", tagIds);
    }

    private List<NoteVO.CategoryItem> getCategories(Long noteId) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT c.id, c.name FROM category c " +
                "INNER JOIN note_category_rel r ON c.id = r.category_id " +
                "WHERE r.note_id = ? AND c.type = 'note'", noteId);
        if (rows.isEmpty()) return Collections.emptyList();
        return rows.stream().map(row -> {
            NoteVO.CategoryItem item = new NoteVO.CategoryItem();
            item.setId((Long) row.get("id"));
            item.setName((String) row.get("name"));
            return item;
        }).collect(Collectors.toList());
    }

    private List<NoteVO.TagItem> getTags(Long noteId) {
        List<TagVO> tagVOs = tagService.getTags("note", noteId);
        if (tagVOs.isEmpty()) return Collections.emptyList();
        return tagVOs.stream().map(vo -> {
            NoteVO.TagItem item = new NoteVO.TagItem();
            item.setId(vo.getId());
            item.setName(vo.getName());
            item.setColor(vo.getColor());
            return item;
        }).collect(Collectors.toList());
    }
}
