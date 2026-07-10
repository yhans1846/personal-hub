package com.personalhub.module.bookmark.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.personalhub.common.exception.NotFoundException;
import com.personalhub.module.bookmark.dto.BookmarkCategoryDTO;
import com.personalhub.module.bookmark.entity.BookmarkCategory;
import com.personalhub.module.bookmark.mapper.BookmarkCategoryMapper;
import com.personalhub.module.bookmark.service.BookmarkCategoryService;
import com.personalhub.module.bookmark.vo.BookmarkCategoryVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 收藏夹分类服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BookmarkCategoryServiceImpl implements BookmarkCategoryService {

    private final BookmarkCategoryMapper bookmarkCategoryMapper;

    @Override
    public List<BookmarkCategoryVO> list(Long userId) {
        LambdaQueryWrapper<BookmarkCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BookmarkCategory::getUserId, userId);
        wrapper.orderByAsc(BookmarkCategory::getSortOrder);
        return bookmarkCategoryMapper.selectList(wrapper).stream().map(c -> {
            BookmarkCategoryVO vo = new BookmarkCategoryVO();
            vo.setId(c.getId());
            vo.setName(c.getName());
            vo.setSortOrder(c.getSortOrder());
            vo.setCreatedAt(c.getCreatedAt());
            return vo;
        }).toList();
    }

    @Override
    @Transactional
    public BookmarkCategoryVO create(Long userId, BookmarkCategoryDTO dto) {
        BookmarkCategory category = new BookmarkCategory();
        category.setUserId(userId);
        category.setName(dto.getName());
        category.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        bookmarkCategoryMapper.insert(category);
        log.info("新建收藏夹分类: id={}, userId={}, name={}", category.getId(), userId, dto.getName());

        BookmarkCategoryVO vo = new BookmarkCategoryVO();
        vo.setId(category.getId());
        vo.setName(category.getName());
        vo.setSortOrder(category.getSortOrder());
        vo.setCreatedAt(category.getCreatedAt());
        return vo;
    }

    @Override
    @Transactional
    public BookmarkCategoryVO update(Long id, Long userId, BookmarkCategoryDTO dto) {
        BookmarkCategory category = bookmarkCategoryMapper.selectById(id);
        if (category == null || !category.getUserId().equals(userId)) {
            throw new NotFoundException("分类不存在");
        }
        category.setName(dto.getName());
        category.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : category.getSortOrder());
        bookmarkCategoryMapper.updateById(category);
        log.info("编辑收藏夹分类: id={}, userId={}", id, userId);

        BookmarkCategoryVO vo = new BookmarkCategoryVO();
        vo.setId(category.getId());
        vo.setName(category.getName());
        vo.setSortOrder(category.getSortOrder());
        vo.setCreatedAt(category.getCreatedAt());
        return vo;
    }

    @Override
    @Transactional
    public void delete(Long id, Long userId) {
        BookmarkCategory category = bookmarkCategoryMapper.selectById(id);
        if (category == null || !category.getUserId().equals(userId)) {
            throw new NotFoundException("分类不存在");
        }
        bookmarkCategoryMapper.deleteById(id);
        log.info("删除收藏夹分类: id={}, userId={}", id, userId);
    }
}
