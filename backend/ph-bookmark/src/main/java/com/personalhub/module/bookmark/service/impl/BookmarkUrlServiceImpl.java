package com.personalhub.module.bookmark.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.personalhub.common.exception.NotFoundException;
import com.personalhub.module.bookmark.dto.BookmarkCreateDTO;
import com.personalhub.module.bookmark.dto.BookmarkQueryDTO;
import com.personalhub.module.bookmark.entity.BookmarkCategory;
import com.personalhub.module.bookmark.entity.BookmarkUrl;
import com.personalhub.module.bookmark.mapper.BookmarkCategoryMapper;
import com.personalhub.module.bookmark.mapper.BookmarkUrlMapper;
import com.personalhub.module.bookmark.service.BookmarkUrlService;
import com.personalhub.module.bookmark.vo.BookmarkVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * 收藏夹服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BookmarkUrlServiceImpl implements BookmarkUrlService {

    private final BookmarkUrlMapper bookmarkUrlMapper;
    private final BookmarkCategoryMapper bookmarkCategoryMapper;

    @Override
    public IPage<BookmarkVO> list(Long userId, BookmarkQueryDTO query) {
        LambdaQueryWrapper<BookmarkUrl> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BookmarkUrl::getUserId, userId);

        // 关键词搜索（标题/网址/描述）
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(BookmarkUrl::getTitle, query.getKeyword())
                    .or().like(BookmarkUrl::getUrl, query.getKeyword())
                    .or().like(BookmarkUrl::getDescription, query.getKeyword()));
        }
        // 分类筛选
        if (query.getCategoryId() != null) {
            wrapper.eq(BookmarkUrl::getCategoryId, query.getCategoryId());
        }
        // 标签筛选
        if (StringUtils.hasText(query.getTag())) {
            wrapper.like(BookmarkUrl::getTags, query.getTag());
        }

        wrapper.orderByDesc(BookmarkUrl::getCreatedAt);

        Page<BookmarkUrl> page = new Page<>(query.getPage(), query.getSize());
        IPage<BookmarkUrl> urlPage = bookmarkUrlMapper.selectPage(page, wrapper);

        // 批量加载分类名称
        Map<Long, String> categoryMap = loadCategoryNames(userId);

        return urlPage.convert(url -> {
            BookmarkVO vo = BookmarkVO.from(url);
            if (url.getCategoryId() != null) {
                vo.setCategoryName(categoryMap.get(url.getCategoryId()));
            }
            return vo;
        });
    }

    private Map<Long, String> loadCategoryNames(Long userId) {
        LambdaQueryWrapper<BookmarkCategory> cw = new LambdaQueryWrapper<>();
        cw.eq(BookmarkCategory::getUserId, userId);
        return bookmarkCategoryMapper.selectList(cw).stream()
                .collect(Collectors.toMap(BookmarkCategory::getId, BookmarkCategory::getName));
    }

    @Override
    public BookmarkVO getById(Long id, Long userId) {
        BookmarkUrl url = bookmarkUrlMapper.selectById(id);
        if (url == null || !url.getUserId().equals(userId)) {
            log.warn("收藏不存在或无权访问: id={}, userId={}", id, userId);
            throw new NotFoundException("收藏不存在");
        }
        BookmarkVO vo = BookmarkVO.from(url);
        if (url.getCategoryId() != null) {
            BookmarkCategory cat = bookmarkCategoryMapper.selectById(url.getCategoryId());
            if (cat != null) vo.setCategoryName(cat.getName());
        }
        return vo;
    }

    @Override
    @Transactional
    public BookmarkVO create(Long userId, BookmarkCreateDTO dto) {
        BookmarkUrl url = new BookmarkUrl();
        url.setUserId(userId);
        url.setTitle(dto.getTitle());
        url.setUrl(dto.getUrl());
        url.setDescription(dto.getDescription());
        url.setFavicon(dto.getFavicon());
        url.setCategoryId(dto.getCategoryId());
        url.setTags(dto.getTags());
        bookmarkUrlMapper.insert(url);
        log.info("新建收藏: id={}, userId={}, title={}", url.getId(), userId, dto.getTitle());

        BookmarkVO vo = BookmarkVO.from(url);
        if (url.getCategoryId() != null) {
            BookmarkCategory cat = bookmarkCategoryMapper.selectById(url.getCategoryId());
            if (cat != null) vo.setCategoryName(cat.getName());
        }
        return vo;
    }

    @Override
    @Transactional
    public BookmarkVO update(Long id, Long userId, BookmarkCreateDTO dto) {
        BookmarkUrl url = bookmarkUrlMapper.selectById(id);
        if (url == null || !url.getUserId().equals(userId)) {
            log.warn("编辑收藏不存在或无权访问: id={}, userId={}", id, userId);
            throw new NotFoundException("收藏不存在");
        }
        url.setTitle(dto.getTitle());
        url.setUrl(dto.getUrl());
        url.setDescription(dto.getDescription());
        url.setFavicon(dto.getFavicon());
        url.setCategoryId(dto.getCategoryId());
        url.setTags(dto.getTags());
        bookmarkUrlMapper.updateById(url);
        log.info("编辑收藏: id={}, userId={}", id, userId);

        BookmarkVO vo = BookmarkVO.from(url);
        if (url.getCategoryId() != null) {
            BookmarkCategory cat = bookmarkCategoryMapper.selectById(url.getCategoryId());
            if (cat != null) vo.setCategoryName(cat.getName());
        }
        return vo;
    }

    @Override
    @Transactional
    public void delete(Long id, Long userId) {
        BookmarkUrl url = bookmarkUrlMapper.selectById(id);
        if (url == null || !url.getUserId().equals(userId)) {
            log.warn("删除收藏不存在或无权访问: id={}, userId={}", id, userId);
            throw new NotFoundException("收藏不存在");
        }
        bookmarkUrlMapper.deleteById(id);
        log.info("删除收藏: id={}, userId={}", id, userId);
    }
}
