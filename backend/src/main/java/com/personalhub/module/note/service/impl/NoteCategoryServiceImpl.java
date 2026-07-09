package com.personalhub.module.note.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.personalhub.common.exception.BusinessException;
import com.personalhub.common.exception.NotFoundException;
import com.personalhub.module.note.entity.NoteCategory;
import com.personalhub.module.note.mapper.NoteCategoryMapper;
import com.personalhub.module.note.service.NoteCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoteCategoryServiceImpl implements NoteCategoryService {

    private final NoteCategoryMapper mapper;

    @Override
    public List<NoteCategory> listByUser(Long userId) {
        return mapper.selectList(
                new LambdaQueryWrapper<NoteCategory>()
                        .eq(NoteCategory::getUserId, userId)
                        .orderByAsc(NoteCategory::getSortOrder)
        );
    }

    @Override
    public NoteCategory create(Long userId, String name, Integer sortOrder) {
        // 检查同名
        Long count = mapper.selectCount(
                new LambdaQueryWrapper<NoteCategory>()
                        .eq(NoteCategory::getUserId, userId)
                        .eq(NoteCategory::getName, name)
        );
        if (count > 0) {
            throw new BusinessException("分类名称已存在");
        }
        NoteCategory entity = new NoteCategory();
        entity.setUserId(userId);
        entity.setName(name);
        entity.setSortOrder(sortOrder != null ? sortOrder : 0);
        mapper.insert(entity);
        return entity;
    }

    @Override
    public NoteCategory update(Long id, Long userId, String name, Integer sortOrder) {
        NoteCategory entity = mapper.selectById(id);
        if (entity == null || !entity.getUserId().equals(userId)) {
            throw new NotFoundException("分类不存在");
        }
        // 检查同名
        Long count = mapper.selectCount(
                new LambdaQueryWrapper<NoteCategory>()
                        .eq(NoteCategory::getUserId, userId)
                        .eq(NoteCategory::getName, name)
                        .ne(NoteCategory::getId, id)
        );
        if (count > 0) {
            throw new BusinessException("分类名称已存在");
        }
        entity.setName(name);
        if (sortOrder != null) entity.setSortOrder(sortOrder);
        mapper.updateById(entity);
        return entity;
    }

    @Override
    public void delete(Long id, Long userId) {
        NoteCategory entity = mapper.selectById(id);
        if (entity == null || !entity.getUserId().equals(userId)) {
            throw new NotFoundException("分类不存在");
        }
        mapper.deleteById(id);
    }
}
