package com.personalhub.knowledge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.personalhub.common.exception.BusinessException;
import com.personalhub.common.exception.NotFoundException;
import com.personalhub.common.util.EntityGuard;
import com.personalhub.knowledge.entity.Note;
import com.personalhub.knowledge.entity.Tag;
import com.personalhub.knowledge.entity.TagRel;
import com.personalhub.knowledge.mapper.NoteMapper;
import com.personalhub.knowledge.mapper.TagMapper;
import com.personalhub.knowledge.mapper.TagRelMapper;
import com.personalhub.knowledge.service.TagService;
import com.personalhub.knowledge.vo.TagVO;
import com.personalhub.planning.entity.StudyPlan;
import com.personalhub.planning.mapper.StudyPlanMapper;
import com.personalhub.resource.entity.BookmarkUrl;
import com.personalhub.resource.mapper.BookmarkUrlMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 标签服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagMapper tagMapper;
    private final TagRelMapper tagRelMapper;
    private final NoteMapper noteMapper;
    private final BookmarkUrlMapper bookmarkUrlMapper;
    private final StudyPlanMapper studyPlanMapper;

    @Override
    @Cacheable(cacheNames = "tags", key = "#userId")
    public List<TagVO> listByUser(Long userId) {
        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Tag::getUserId, userId)
                .orderByAsc(Tag::getCreatedAt);
        List<Tag> tags = tagMapper.selectList(wrapper);
        if (tags.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> tagIds = tags.stream().map(Tag::getId).collect(Collectors.toList());
        List<TagRel> rels = tagRelMapper.selectList(
                new LambdaQueryWrapper<TagRel>().in(TagRel::getTagId, tagIds));
        Map<Long, Long> usageMap = rels.stream()
                .collect(Collectors.groupingBy(TagRel::getTagId, Collectors.counting()));

        return tags.stream().map(tag -> {
            TagVO vo = TagVO.from(tag);
            vo.setUsageCount(usageMap.getOrDefault(tag.getId(), 0L));
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    @CacheEvict(cacheNames = "tags", key = "#userId")
    public TagVO create(Long userId, String name, String color) {
        LambdaQueryWrapper<Tag> check = new LambdaQueryWrapper<>();
        check.eq(Tag::getUserId, userId).eq(Tag::getName, name);
        if (tagMapper.selectCount(check) > 0) {
            throw new BusinessException("标签名称已存在");
        }

        var tag = Tag.builder()
                .userId(userId)
                .name(name)
                .color(color != null ? color : "#409eff")
                .build();
        tagMapper.insert(tag);
        log.info("创建标签: id={}, userId={}, name={}", tag.getId(), userId, name);
        return TagVO.from(tag);
    }

    @Override
    @CacheEvict(cacheNames = "tags", key = "#userId")
    public void update(Long id, Long userId, String name, String color) {
        Tag tag = EntityGuard.requireOwned(
                tagMapper.selectById(id), userId, Tag::getUserId, "标签不存在");

        LambdaQueryWrapper<Tag> check = new LambdaQueryWrapper<>();
        check.eq(Tag::getUserId, userId).eq(Tag::getName, name).ne(Tag::getId, id);
        if (tagMapper.selectCount(check) > 0) {
            throw new BusinessException("标签名称已存在");
        }

        tag.setName(name);
        if (color != null) {
            tag.setColor(color);
        }
        tagMapper.updateById(tag);
        log.info("更新标签: id={}, userId={}, name={}", id, userId, name);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "tags", key = "#userId")
    public void delete(Long id, Long userId) {
        Tag tag = EntityGuard.requireOwned(
                tagMapper.selectById(id), userId, Tag::getUserId, "标签不存在");
        tagRelMapper.delete(new LambdaQueryWrapper<TagRel>().eq(TagRel::getTagId, id));
        tagMapper.deleteById(id);
        log.info("删除标签: id={}, userId={}, name={}", id, userId, tag.getName());
    }

    @Override
    @CacheEvict(cacheNames = "tags", allEntries = true)
    public void bindTag(Long userId, Long tagId, String entityType, Long entityId) {
        requireOwnedTag(tagId, userId);
        requireOwnedEntity(userId, entityType, entityId);
        doBind(tagId, entityType, entityId);
    }

    @Override
    @CacheEvict(cacheNames = "tags", allEntries = true)
    public void unbindTag(Long userId, Long tagId, String entityType, Long entityId) {
        requireOwnedTag(tagId, userId);
        requireOwnedEntity(userId, entityType, entityId);
        tagRelMapper.delete(new LambdaQueryWrapper<TagRel>()
                .eq(TagRel::getTagId, tagId)
                .eq(TagRel::getEntityType, entityType)
                .eq(TagRel::getEntityId, entityId));
    }

    @Override
    @CacheEvict(cacheNames = "tags", allEntries = true)
    public void unbindAll(Long userId, String entityType, Long entityId) {
        requireOwnedEntity(userId, entityType, entityId);
        doUnbindAll(entityType, entityId);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "tags", allEntries = true)
    public void bindTags(Long userId, Long entityId, String entityType, List<Long> tagIds) {
        requireOwnedEntity(userId, entityType, entityId);
        if (tagIds != null) {
            for (Long tagId : tagIds) {
                requireOwnedTag(tagId, userId);
            }
        }
        doUnbindAll(entityType, entityId);
        if (tagIds != null) {
            for (Long tagId : tagIds) {
                doBind(tagId, entityType, entityId);
            }
        }
    }

    @Override
    public List<TagVO> getTags(Long userId, String entityType, Long entityId) {
        requireOwnedEntity(userId, entityType, entityId);
        List<TagRel> rels = tagRelMapper.selectList(
                new LambdaQueryWrapper<TagRel>()
                        .eq(TagRel::getEntityType, entityType)
                        .eq(TagRel::getEntityId, entityId));

        if (rels.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> tagIds = rels.stream().map(TagRel::getTagId).collect(Collectors.toList());
        List<Tag> tags = tagMapper.selectBatchIds(tagIds);
        return tags.stream()
                .filter(t -> Objects.equals(t.getUserId(), userId))
                .map(TagVO::from)
                .collect(Collectors.toList());
    }

    @Override
    public Map<Long, List<TagVO>> getTagsMap(Long userId, String entityType, List<Long> entityIds) {
        if (entityIds == null || entityIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<TagRel> rels = tagRelMapper.selectList(
                new LambdaQueryWrapper<TagRel>()
                        .eq(TagRel::getEntityType, entityType)
                        .in(TagRel::getEntityId, entityIds));

        if (rels.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Long> tagIds = rels.stream().map(TagRel::getTagId).distinct().collect(Collectors.toList());
        List<Tag> tags = tagMapper.selectBatchIds(tagIds);
        Map<Long, TagVO> tagVOMap = tags.stream()
                .filter(t -> Objects.equals(t.getUserId(), userId))
                .collect(Collectors.toMap(Tag::getId, TagVO::from));

        Map<Long, List<TagRel>> relGroup = rels.stream().collect(Collectors.groupingBy(TagRel::getEntityId));
        return entityIds.stream().collect(Collectors.toMap(
                eid -> eid,
                eid -> {
                    List<TagRel> entityRels = relGroup.get(eid);
                    if (entityRels == null) {
                        return Collections.emptyList();
                    }
                    return entityRels.stream()
                            .map(rel -> tagVOMap.get(rel.getTagId()))
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
                }));
    }

    private void requireOwnedTag(Long tagId, Long userId) {
        EntityGuard.requireOwned(
                tagMapper.selectById(tagId), userId, Tag::getUserId, "标签不存在");
    }

    private void requireOwnedEntity(Long userId, String entityType, Long entityId) {
        if (entityType == null || entityType.isBlank() || entityId == null) {
            throw new BusinessException("实体参数无效");
        }
        switch (entityType) {
            case "note" -> EntityGuard.requireOwned(
                    noteMapper.selectById(entityId), userId, Note::getUserId, "笔记不存在");
            case "bookmark" -> EntityGuard.requireOwned(
                    bookmarkUrlMapper.selectById(entityId), userId, BookmarkUrl::getUserId, "收藏不存在");
            case "study_plan" -> EntityGuard.requireOwned(
                    studyPlanMapper.selectById(entityId), userId, StudyPlan::getUserId, "学习计划不存在");
            default -> throw new NotFoundException("不支持的实体类型");
        }
    }

    private void doBind(Long tagId, String entityType, Long entityId) {
        LambdaQueryWrapper<TagRel> check = new LambdaQueryWrapper<>();
        check.eq(TagRel::getTagId, tagId)
                .eq(TagRel::getEntityType, entityType)
                .eq(TagRel::getEntityId, entityId);
        if (tagRelMapper.selectCount(check) == 0) {
            TagRel rel = new TagRel();
            rel.setTagId(tagId);
            rel.setEntityType(entityType);
            rel.setEntityId(entityId);
            tagRelMapper.insert(rel);
        }
    }

    private void doUnbindAll(String entityType, Long entityId) {
        tagRelMapper.delete(new LambdaQueryWrapper<TagRel>()
                .eq(TagRel::getEntityType, entityType)
                .eq(TagRel::getEntityId, entityId));
    }
}
