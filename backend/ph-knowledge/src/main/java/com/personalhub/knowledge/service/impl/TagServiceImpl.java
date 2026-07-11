package com.personalhub.knowledge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.personalhub.common.exception.BusinessException;
import com.personalhub.common.exception.NotFoundException;
import com.personalhub.knowledge.entity.Tag;
import com.personalhub.knowledge.entity.TagRel;
import com.personalhub.knowledge.mapper.TagMapper;
import com.personalhub.knowledge.mapper.TagRelMapper;
import com.personalhub.knowledge.service.TagService;
import com.personalhub.knowledge.vo.TagVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
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

    @Override
    public List<TagVO> listByUser(Long userId) {
        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Tag::getUserId, userId)
                .orderByAsc(Tag::getCreatedAt);
        List<Tag> tags = tagMapper.selectList(wrapper);

        // 统计每个标签的使用次数
        List<TagVO> voList = tags.stream().map(TagVO::from).collect(Collectors.toList());
        for (TagVO vo : voList) {
            Long count = tagRelMapper.selectCount(
                    new LambdaQueryWrapper<TagRel>().eq(TagRel::getTagId, vo.getId()));
            vo.setUsageCount(count);
        }
        return voList;
    }

    @Override
    public TagVO create(Long userId, String name, String color) {
        // 检查重复
        LambdaQueryWrapper<Tag> check = new LambdaQueryWrapper<>();
        check.eq(Tag::getUserId, userId).eq(Tag::getName, name);
        if (tagMapper.selectCount(check) > 0) {
            throw new BusinessException("标签名称已存在");
        }

        Tag tag = new Tag();
        tag.setUserId(userId);
        tag.setName(name);
        tag.setColor(color != null ? color : "#409eff");
        tagMapper.insert(tag);
        log.info("创建标签: id={}, userId={}, name={}", tag.getId(), userId, name);
        return TagVO.from(tag);
    }

    @Override
    public void update(Long id, Long userId, String name, String color) {
        Tag tag = tagMapper.selectById(id);
        if (tag == null || !tag.getUserId().equals(userId)) {
            throw new NotFoundException("标签不存在");
        }

        // 检查重复（排除自身）
        LambdaQueryWrapper<Tag> check = new LambdaQueryWrapper<>();
        check.eq(Tag::getUserId, userId).eq(Tag::getName, name).ne(Tag::getId, id);
        if (tagMapper.selectCount(check) > 0) {
            throw new BusinessException("标签名称已存在");
        }

        tag.setName(name);
        if (color != null) tag.setColor(color);
        tagMapper.updateById(tag);
        log.info("更新标签: id={}, userId={}, name={}", id, userId, name);
    }

    @Override
    @Transactional
    public void delete(Long id, Long userId) {
        Tag tag = tagMapper.selectById(id);
        if (tag == null || !tag.getUserId().equals(userId)) {
            throw new NotFoundException("标签不存在");
        }
        // 清除关联
        tagRelMapper.delete(new LambdaQueryWrapper<TagRel>().eq(TagRel::getTagId, id));
        tagMapper.deleteById(id);
        log.info("删除标签: id={}, userId={}, name={}", id, userId, tag.getName());
    }

    @Override
    public void bindTag(Long tagId, String entityType, Long entityId) {
        // 检查是否已绑定
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

    @Override
    public void unbindTag(Long tagId, String entityType, Long entityId) {
        LambdaQueryWrapper<TagRel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TagRel::getTagId, tagId)
                .eq(TagRel::getEntityType, entityType)
                .eq(TagRel::getEntityId, entityId);
        tagRelMapper.delete(wrapper);
    }

    @Override
    public void unbindAll(String entityType, Long entityId) {
        LambdaQueryWrapper<TagRel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TagRel::getEntityType, entityType)
                .eq(TagRel::getEntityId, entityId);
        tagRelMapper.delete(wrapper);
    }

    @Override
    @Transactional
    public void bindTags(Long entityId, String entityType, List<Long> tagIds) {
        // 先清空再绑定
        unbindAll(entityType, entityId);
        if (tagIds != null && !tagIds.isEmpty()) {
            for (Long tagId : tagIds) {
                bindTag(tagId, entityType, entityId);
            }
        }
    }

    @Override
    public List<TagVO> getTags(String entityType, Long entityId) {
        List<TagRel> rels = tagRelMapper.selectList(
                new LambdaQueryWrapper<TagRel>()
                        .eq(TagRel::getEntityType, entityType)
                        .eq(TagRel::getEntityId, entityId));

        if (rels.isEmpty()) return Collections.emptyList();

        List<Long> tagIds = rels.stream().map(TagRel::getTagId).collect(Collectors.toList());
        List<Tag> tags = tagMapper.selectBatchIds(tagIds);
        return tags.stream().map(TagVO::from).collect(Collectors.toList());
    }

    @Override
    public Map<Long, List<TagVO>> getTagsMap(String entityType, List<Long> entityIds) {
        if (entityIds == null || entityIds.isEmpty()) return Collections.emptyMap();

        // 批量查询关联
        List<TagRel> rels = tagRelMapper.selectList(
                new LambdaQueryWrapper<TagRel>()
                        .eq(TagRel::getEntityType, entityType)
                        .in(TagRel::getEntityId, entityIds));

        if (rels.isEmpty()) return Collections.emptyMap();

        // 批量查询标签
        List<Long> tagIds = rels.stream().map(TagRel::getTagId).distinct().collect(Collectors.toList());
        List<Tag> tags = tagMapper.selectBatchIds(tagIds);
        Map<Long, TagVO> tagVOMap = tags.stream().collect(Collectors.toMap(Tag::getId, TagVO::from));

        // 按 entityId 分组
        Map<Long, List<TagRel>> relGroup = rels.stream().collect(Collectors.groupingBy(TagRel::getEntityId));
        return entityIds.stream().collect(Collectors.toMap(
                eid -> eid,
                eid -> {
                    List<TagRel> entityRels = relGroup.get(eid);
                    if (entityRels == null) return Collections.emptyList();
                    return entityRels.stream()
                            .map(rel -> tagVOMap.get(rel.getTagId()))
                            .filter(t -> t != null)
                            .collect(Collectors.toList());
                }));
    }

    @Override
    public List<Long> getEntityIdsByTag(Long tagId, String entityType) {
        LambdaQueryWrapper<TagRel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TagRel::getTagId, tagId)
                .eq(TagRel::getEntityType, entityType);
        return tagRelMapper.selectList(wrapper).stream()
                .map(TagRel::getEntityId)
                .collect(Collectors.toList());
    }
}
