package com.personalhub.module.note.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.personalhub.common.exception.BusinessException;
import com.personalhub.common.exception.NotFoundException;
import com.personalhub.module.note.entity.NoteTag;
import com.personalhub.module.note.mapper.NoteTagMapper;
import com.personalhub.module.note.service.NoteTagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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
            throw new BusinessException("标签名称已存在");
        }
        NoteTag entity = new NoteTag();
        entity.setUserId(userId);
        entity.setName(name);
        mapper.insert(entity);
        return entity;
    }

    @Override
    public NoteTag update(Long id, Long userId, String name) {
        NoteTag entity = mapper.selectById(id);
        if (entity == null || !entity.getUserId().equals(userId)) {
            throw new NotFoundException("标签不存在");
        }
        Long count = mapper.selectCount(
                new LambdaQueryWrapper<NoteTag>()
                        .eq(NoteTag::getUserId, userId)
                        .eq(NoteTag::getName, name)
                        .ne(NoteTag::getId, id)
        );
        if (count > 0) {
            throw new BusinessException("标签名称已存在");
        }
        entity.setName(name);
        mapper.updateById(entity);
        return entity;
    }

    @Override
    public void delete(Long id, Long userId) {
        NoteTag entity = mapper.selectById(id);
        if (entity == null || !entity.getUserId().equals(userId)) {
            throw new NotFoundException("标签不存在");
        }
        mapper.deleteById(id);
    }
}
