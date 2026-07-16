package com.personalhub.knowledge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.personalhub.common.exception.BusinessException;
import com.personalhub.common.exception.NotFoundException;
import com.personalhub.knowledge.dto.CategoryCreateDTO;
import com.personalhub.knowledge.dto.CategoryUpdateDTO;
import com.personalhub.knowledge.entity.Category;
import com.personalhub.knowledge.mapper.CategoryMapper;
import com.personalhub.knowledge.service.CategoryService;
import com.personalhub.knowledge.vo.CategoryVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import com.personalhub.knowledge.dto.SortOrderDTO;

/**
 * 统一分类服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;

    @Override
    @Cacheable(cacheNames = "categories", key = "#userId + ':' + #type")
    public List<CategoryVO> listByType(Long userId, String type) {
        LambdaQueryWrapper<Category> qw = new LambdaQueryWrapper<Category>()
                .eq(Category::getUserId, userId)
                .eq(Category::getType, type)
                .orderByAsc(Category::getSortOrder);
        return categoryMapper.selectList(qw).stream()
                .map(c -> CategoryVO.from(c, getCount(c.getId(), type)))
                .collect(Collectors.toList());
    }

    /**
     * 统计某分类下关联的实体数量
     */
    private Integer getCount(Long categoryId, String type) {
        Integer count = categoryMapper.countByType(categoryId, type);
        return count != null ? count : 0;
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "categories", allEntries = true)
    public CategoryVO create(Long userId, CategoryCreateDTO dto) {
        // 检查唯一性
        LambdaQueryWrapper<Category> qw = new LambdaQueryWrapper<Category>()
                .eq(Category::getUserId, userId)
                .eq(Category::getType, dto.getType())
                .eq(Category::getName, dto.getName());
        if (categoryMapper.selectCount(qw) > 0) {
            throw new BusinessException("分类名称已存在");
        }

        Category category = new Category();
        category.setUserId(userId);
        category.setName(dto.getName());
        category.setType(dto.getType());
        if (dto.getSortOrder() != null) {
            category.setSortOrder(dto.getSortOrder());
        }
        categoryMapper.insert(category);

        log.info("创建分类: type={}, name={}, userId={}", dto.getType(), dto.getName(), userId);
        return CategoryVO.from(category, 0);
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "categories", allEntries = true)
    public CategoryVO update(Long id, Long userId, CategoryUpdateDTO dto) {
        Category category = categoryMapper.selectById(id);
        if (category == null || !category.getUserId().equals(userId)) {
            throw new NotFoundException("分类不存在");
        }

        // 检查名称唯一性（排除自身）
        LambdaQueryWrapper<Category> qw = new LambdaQueryWrapper<Category>()
                .eq(Category::getUserId, userId)
                .eq(Category::getType, category.getType())
                .eq(Category::getName, dto.getName())
                .ne(Category::getId, id);
        if (categoryMapper.selectCount(qw) > 0) {
            throw new BusinessException("分类名称已存在");
        }

        category.setName(dto.getName());
        category.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : category.getSortOrder());
        categoryMapper.updateById(category);

        log.info("更新分类: id={}, name={}", id, dto.getName());
        return CategoryVO.from(category, getCount(id, category.getType()));
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "categories", allEntries = true)
    public void delete(Long id, Long userId) {
        Category category = categoryMapper.selectById(id);
        if (category == null || !category.getUserId().equals(userId)) {
            throw new NotFoundException("分类不存在");
        }

        // 清理关联（note 类型有关联表）
        if ("note".equals(category.getType())) {
            categoryMapper.deleteNoteCategoryRels(id);
        }

        categoryMapper.deleteById(id);
        log.info("删除分类: id={}, type={}, name={}", id, category.getType(), category.getName());
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = "categories", allEntries = true)
    public void updateSortOrder(List<SortOrderDTO> list) {
        for (SortOrderDTO dto : list) {
            Category category = new Category();
            category.setId(dto.getId());
            category.setSortOrder(dto.getSortOrder());
            categoryMapper.updateById(category);
        }
    }
}
