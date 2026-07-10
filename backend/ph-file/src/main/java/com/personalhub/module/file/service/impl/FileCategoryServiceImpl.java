package com.personalhub.module.file.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.personalhub.common.exception.NotFoundException;
import com.personalhub.module.file.dto.FileCategoryCreateDTO;
import com.personalhub.module.file.entity.FileCategory;
import com.personalhub.module.file.mapper.FileCategoryMapper;
import com.personalhub.module.file.service.FileCategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 文件分类服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileCategoryServiceImpl implements FileCategoryService {

    private final FileCategoryMapper categoryMapper;

    @Override
    public List<FileCategory> list(Long userId) {
        LambdaQueryWrapper<FileCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileCategory::getUserId, userId)
               .orderByAsc(FileCategory::getSortOrder)
               .orderByAsc(FileCategory::getId);
        return categoryMapper.selectList(wrapper);
    }

    @Override
    @Transactional
    public FileCategory create(Long userId, FileCategoryCreateDTO dto) {
        FileCategory category = new FileCategory();
        category.setUserId(userId);
        category.setName(dto.getName());
        category.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        categoryMapper.insert(category);
        log.info("新建文件分类: id={}, userId={}, name={}", category.getId(), userId, dto.getName());
        return category;
    }

    @Override
    @Transactional
    public FileCategory update(Long id, Long userId, FileCategoryCreateDTO dto) {
        FileCategory category = categoryMapper.selectById(id);
        if (category == null || !category.getUserId().equals(userId)) {
            log.warn("文件分类不存在或无权访问: id={}, userId={}", id, userId);
            throw new NotFoundException("分类不存在");
        }
        category.setName(dto.getName());
        category.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        categoryMapper.updateById(category);
        log.info("更新文件分类: id={}, userId={}, name={}", id, userId, dto.getName());
        return category;
    }

    @Override
    @Transactional
    public void delete(Long id, Long userId) {
        FileCategory category = categoryMapper.selectById(id);
        if (category == null || !category.getUserId().equals(userId)) {
            log.warn("删除文件分类不存在或无权访问: id={}, userId={}", id, userId);
            throw new NotFoundException("分类不存在");
        }
        categoryMapper.deleteById(id);
        log.info("删除文件分类: id={}, userId={}, name={}", id, userId, category.getName());
    }
}
