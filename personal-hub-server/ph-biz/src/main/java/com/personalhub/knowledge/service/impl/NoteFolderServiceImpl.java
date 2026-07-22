package com.personalhub.knowledge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.personalhub.common.constant.Flags;
import com.personalhub.common.exception.BusinessException;
import com.personalhub.common.util.EntityGuard;
import com.personalhub.knowledge.dto.NoteFolderCreateDTO;
import com.personalhub.knowledge.dto.NoteFolderMoveDTO;
import com.personalhub.knowledge.dto.NoteFolderUpdateDTO;
import com.personalhub.knowledge.entity.Note;
import com.personalhub.knowledge.entity.NoteFolder;
import com.personalhub.knowledge.mapper.NoteFolderMapper;
import com.personalhub.knowledge.mapper.NoteMapper;
import com.personalhub.knowledge.service.NoteFolderService;
import com.personalhub.knowledge.vo.NoteFolderTreeVO;
import com.personalhub.knowledge.vo.NoteFolderVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.personalhub.knowledge.vo.NoteFolderNoteItem;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoteFolderServiceImpl implements NoteFolderService {

    private final NoteFolderMapper noteFolderMapper;
    private final NoteMapper noteMapper;

    @Override
    public NoteFolderTreeVO tree(Long userId) {
        List<NoteFolder> all = noteFolderMapper.selectList(new LambdaQueryWrapper<NoteFolder>()
                .eq(NoteFolder::getUserId, userId)
                .orderByAsc(NoteFolder::getSortOrder)
                .orderByAsc(NoteFolder::getId));

        List<Note> notes = noteMapper.selectList(new LambdaQueryWrapper<Note>()
                .eq(Note::getUserId, userId)
                .eq(Note::getIsDeleted, Flags.NO)
                .select(Note::getId, Note::getTitle, Note::getFolderId, Note::getUpdatedAt)
                .orderByDesc(Note::getUpdatedAt));

        Map<Long, Long> countByFolder = new HashMap<>();
        Map<Long, List<NoteFolderNoteItem>> notesByFolder = new HashMap<>();
        List<NoteFolderNoteItem> uncategorizedNotes = new ArrayList<>();
        for (Note n : notes) {
            NoteFolderNoteItem item = toNoteItem(n);
            if (n.getFolderId() == null) {
                uncategorizedNotes.add(item);
            } else {
                countByFolder.merge(n.getFolderId(), 1L, Long::sum);
                notesByFolder.computeIfAbsent(n.getFolderId(), k -> new ArrayList<>()).add(item);
            }
        }

        List<NoteFolderVO> folders = buildTree(all);
        applyNoteCounts(folders, countByFolder);
        attachNotes(folders, notesByFolder);

        NoteFolderTreeVO vo = new NoteFolderTreeVO();
        vo.setFolders(folders);
        vo.setTotalCount(notes.size());
        vo.setUncategorizedCount(uncategorizedNotes.size());
        vo.setUncategorizedNotes(uncategorizedNotes);
        return vo;
    }

    private static NoteFolderNoteItem toNoteItem(Note n) {
        NoteFolderNoteItem item = new NoteFolderNoteItem();
        item.setId(n.getId());
        item.setTitle(n.getTitle() != null && !n.getTitle().isBlank() ? n.getTitle() : "无标题笔记");
        item.setFolderId(n.getFolderId());
        item.setUpdatedAt(n.getUpdatedAt());
        return item;
    }

    private void attachNotes(List<NoteFolderVO> nodes, Map<Long, List<NoteFolderNoteItem>> notesByFolder) {
        for (NoteFolderVO node : nodes) {
            List<NoteFolderNoteItem> list = notesByFolder.getOrDefault(node.getId(), List.of());
            node.setNotes(new ArrayList<>(list));
            if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                attachNotes(node.getChildren(), notesByFolder);
            }
        }
    }

    private void applyNoteCounts(List<NoteFolderVO> nodes, Map<Long, Long> countByFolder) {
        for (NoteFolderVO node : nodes) {
            node.setNoteCount(countByFolder.getOrDefault(node.getId(), 0L));
            if (node.getChildren() != null && !node.getChildren().isEmpty()) {
                applyNoteCounts(node.getChildren(), countByFolder);
            }
        }
    }

    @Override
    @Transactional
    public NoteFolderVO create(Long userId, NoteFolderCreateDTO dto) {
        String name = dto.getName().trim();
        Long parentId = dto.getParentId();
        if (parentId != null) {
            requireOwnedFolder(userId, parentId);
            int parentDepth = depthOf(userId, parentId);
            if (parentDepth >= MAX_DEPTH) {
                throw new BusinessException("文件夹层级不能超过 " + MAX_DEPTH + " 层");
            }
        }
        assertUniqueName(userId, parentId, name, null);
        int sort = nextSortOrder(userId, parentId);
        NoteFolder folder = NoteFolder.builder()
                .userId(userId)
                .parentId(parentId)
                .name(name)
                .sortOrder(sort)
                .build();
        noteFolderMapper.insert(folder);
        log.info("创建笔记文件夹: id={}, name={}, parentId={}, userId={}", folder.getId(), name, parentId, userId);
        return NoteFolderVO.from(folder);
    }

    @Override
    @Transactional
    public NoteFolderVO rename(Long userId, Long id, NoteFolderUpdateDTO dto) {
        NoteFolder folder = requireFolder(userId, id);
        String name = dto.getName().trim();
        assertUniqueName(userId, folder.getParentId(), name, id);
        folder.setName(name);
        noteFolderMapper.updateById(folder);
        return NoteFolderVO.from(folder);
    }

    @Override
    @Transactional
    public NoteFolderVO move(Long userId, Long id, NoteFolderMoveDTO dto) {
        NoteFolder folder = requireFolder(userId, id);
        Long newParentId = dto.getParentId();
        if (Objects.equals(id, newParentId)) {
            throw new BusinessException("不能将文件夹移动到自身下");
        }
        if (newParentId != null) {
            requireOwnedFolder(userId, newParentId);
            Set<Long> descendants = collectSubtreeIds(userId, id);
            if (descendants.contains(newParentId)) {
                throw new BusinessException("不能将文件夹移动到其子文件夹下");
            }
            int newDepth = depthOf(userId, newParentId) + 1 + maxDescendantDepth(userId, id);
            if (newDepth > MAX_DEPTH) {
                throw new BusinessException("移动后层级将超过 " + MAX_DEPTH + " 层");
            }
        } else {
            int newDepth = 1 + maxDescendantDepth(userId, id);
            if (newDepth > MAX_DEPTH) {
                throw new BusinessException("移动后层级将超过 " + MAX_DEPTH + " 层");
            }
        }
        assertUniqueName(userId, newParentId, folder.getName(), id);
        // 使用 LambdaUpdateWrapper 显式 set，避免 MyBatis-Plus updateById 默认忽略 null 字段
        // 导致 parentId 无法更新为 null（向上移动/平级移动到根级）
        noteFolderMapper.update(null,
                new LambdaUpdateWrapper<NoteFolder>()
                        .eq(NoteFolder::getId, id)
                        .set(NoteFolder::getParentId, newParentId)
                        .set(NoteFolder::getSortOrder, dto.getSortOrder()));
        log.info("移动笔记文件夹: id={}, parentId={}, sortOrder={}", id, newParentId, dto.getSortOrder());
        return NoteFolderVO.from(folder);
    }

    @Override
    @Transactional
    public void delete(Long userId, Long id) {
        requireFolder(userId, id);
        Set<Long> ids = collectSubtreeIds(userId, id);
        if (ids.isEmpty()) {
            return;
        }
        noteMapper.update(null, new LambdaUpdateWrapper<Note>()
                .eq(Note::getUserId, userId)
                .in(Note::getFolderId, ids)
                .set(Note::getFolderId, null));
        noteFolderMapper.delete(new LambdaQueryWrapper<NoteFolder>()
                .eq(NoteFolder::getUserId, userId)
                .in(NoteFolder::getId, ids));
        log.info("删除笔记文件夹子树: userId={}, rootId={}, count={}", userId, id, ids.size());
    }

    @Override
    public void requireOwnedFolder(Long userId, Long folderId) {
        if (folderId == null) {
            return;
        }
        requireFolder(userId, folderId);
    }

    private NoteFolder requireFolder(Long userId, Long id) {
        return EntityGuard.requireOwned(
                noteFolderMapper.selectById(id), userId, NoteFolder::getUserId, "文件夹不存在");
    }

    private void assertUniqueName(Long userId, Long parentId, String name, Long excludeId) {
        LambdaQueryWrapper<NoteFolder> qw = new LambdaQueryWrapper<NoteFolder>()
                .eq(NoteFolder::getUserId, userId)
                .eq(NoteFolder::getName, name);
        if (parentId == null) {
            qw.isNull(NoteFolder::getParentId);
        } else {
            qw.eq(NoteFolder::getParentId, parentId);
        }
        if (excludeId != null) {
            qw.ne(NoteFolder::getId, excludeId);
        }
        if (noteFolderMapper.selectCount(qw) > 0) {
            throw new BusinessException("同级下已存在同名文件夹");
        }
    }

    private int nextSortOrder(Long userId, Long parentId) {
        LambdaQueryWrapper<NoteFolder> qw = new LambdaQueryWrapper<NoteFolder>()
                .eq(NoteFolder::getUserId, userId)
                .orderByDesc(NoteFolder::getSortOrder)
                .last("LIMIT 1");
        if (parentId == null) {
            qw.isNull(NoteFolder::getParentId);
        } else {
            qw.eq(NoteFolder::getParentId, parentId);
        }
        NoteFolder last = noteFolderMapper.selectOne(qw);
        return last == null || last.getSortOrder() == null ? 0 : last.getSortOrder() + 1;
    }

    /** 节点自身深度：根下第一层 = 1 */
    private int depthOf(Long userId, Long folderId) {
        int depth = 0;
        Long cur = folderId;
        Set<Long> seen = new HashSet<>();
        while (cur != null) {
            if (!seen.add(cur)) {
                throw new BusinessException("文件夹数据异常：检测到环");
            }
            depth++;
            NoteFolder f = noteFolderMapper.selectById(cur);
            if (f == null || !Objects.equals(f.getUserId(), userId)) {
                throw new BusinessException("文件夹不存在");
            }
            cur = f.getParentId();
        }
        return depth;
    }

    /** 以 folderId 为根的子树中，相对根的最大额外深度（仅子孙，不含自身 0） */
    private int maxDescendantDepth(Long userId, Long rootId) {
        List<NoteFolder> all = noteFolderMapper.selectList(new LambdaQueryWrapper<NoteFolder>()
                .eq(NoteFolder::getUserId, userId));
        Map<Long, List<Long>> children = new HashMap<>();
        for (NoteFolder f : all) {
            if (f.getParentId() == null) continue;
            children.computeIfAbsent(f.getParentId(), k -> new ArrayList<>()).add(f.getId());
        }
        return dfsMaxExtra(rootId, children, 0);
    }

    private int dfsMaxExtra(Long id, Map<Long, List<Long>> children, int extra) {
        List<Long> kids = children.getOrDefault(id, List.of());
        int max = extra;
        for (Long kid : kids) {
            max = Math.max(max, dfsMaxExtra(kid, children, extra + 1));
        }
        return max;
    }

    private Set<Long> collectSubtreeIds(Long userId, Long rootId) {
        List<NoteFolder> all = noteFolderMapper.selectList(new LambdaQueryWrapper<NoteFolder>()
                .eq(NoteFolder::getUserId, userId));
        Map<Long, List<Long>> children = new HashMap<>();
        for (NoteFolder f : all) {
            if (f.getParentId() == null) continue;
            children.computeIfAbsent(f.getParentId(), k -> new ArrayList<>()).add(f.getId());
        }
        Set<Long> ids = new HashSet<>();
        collect(rootId, children, ids);
        return ids;
    }

    private void collect(Long id, Map<Long, List<Long>> children, Set<Long> out) {
        out.add(id);
        for (Long kid : children.getOrDefault(id, List.of())) {
            collect(kid, children, out);
        }
    }

    private List<NoteFolderVO> buildTree(List<NoteFolder> all) {
        Map<Long, NoteFolderVO> map = all.stream()
                .collect(Collectors.toMap(NoteFolder::getId, NoteFolderVO::from, (a, b) -> a));
        List<NoteFolderVO> roots = new ArrayList<>();
        for (NoteFolder f : all) {
            NoteFolderVO vo = map.get(f.getId());
            if (f.getParentId() == null) {
                roots.add(vo);
            } else {
                NoteFolderVO parent = map.get(f.getParentId());
                if (parent != null) {
                    parent.getChildren().add(vo);
                } else {
                    roots.add(vo);
                }
            }
        }
        return roots;
    }
}
