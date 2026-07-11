package com.personalhub.knowledge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.personalhub.common.exception.BusinessException;
import com.personalhub.common.exception.NotFoundException;
import com.personalhub.knowledge.entity.NoteTag;
import com.personalhub.knowledge.mapper.NoteTagMapper;
import com.personalhub.knowledge.service.NoteTagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoteTagServiceImpl implements NoteTagService {

    private final NoteTagMapper mapper;

    @Override
    public List<NoteTag> listByUser(Long userId) {
        return mapper.selectList(
                new LambdaQueryWrapper<NoteTag>()
                        .eq(NoteTag::getUserId, userId)
                        .orderByAsc(NoteTag::getCreatedAt)
        );
    }

    @Override
    public NoteTag create(Long userId, String name) {
        Long count = mapper.selectCount(
                new LambdaQueryWrapper<NoteTag>()
                        .eq(NoteTag::getUserId, userId)
                        .eq(NoteTag::getName, name)
        );
        if (count > 0) {
            log.warn("标签名称已存在: userId={}, name={}", userId, name);
            throw new BusinessException("标签名称已存在");
        }
        NoteTag entity = new NoteTag();
        entity.setUserId(userId);
        entity.setName(name);
        mapper.insert(entity);
        log.info("新建标签: id={}, userId={}, name={}", entity.getId(), userId, name);
        return entity;
    }

    @Override
    public NoteTag update(Long id, Long userId, String name) {
        NoteTag entity = mapper.selectById(id);
        if (entity == null || !entity.getUserId().equals(userId)) {
            log.warn("标签不存在或无权访问: id={}, userId={}", id, userId);
            throw new NotFoundException("标签不存在");
        }
        Long count = mapper.selectCount(
                new LambdaQueryWrapper<NoteTag>()
                        .eq(NoteTag::getUserId, userId)
                        .eq(NoteTag::getName, name)
                        .ne(NoteTag::getId, id)
        );
        if (count > 0) {
            log.warn("标签名称已存在: userId={}, name={}", userId, name);
            throw new BusinessException("标签名称已存在");
        }
        entity.setName(name);
        mapper.updateById(entity);
        log.info("编辑标签: id={}, userId={}", id, userId);
        return entity;
    }

    @Override
    public void delete(Long id, Long userId) {
        NoteTag entity = mapper.selectById(id);
        if (entity == null || !entity.getUserId().equals(userId)) {
            log.warn("删除标签不存在或无权访问: id={}, userId={}", id, userId);
            throw new NotFoundException("标签不存在");
        }
        mapper.deleteById(id);
        log.info("删除标签: id={}, userId={}", id, userId);
    }
}
