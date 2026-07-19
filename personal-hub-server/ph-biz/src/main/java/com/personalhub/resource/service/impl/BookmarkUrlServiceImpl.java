package com.personalhub.resource.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.personalhub.common.util.EntityGuard;
import com.personalhub.resource.dto.BookmarkCreateDTO;
import com.personalhub.resource.dto.BookmarkQueryDTO;
import com.personalhub.knowledge.service.CategoryService;
import com.personalhub.resource.entity.BookmarkUrl;
import com.personalhub.resource.mapper.BookmarkUrlMapper;
import com.personalhub.resource.service.BookmarkUrlService;
import com.personalhub.resource.vo.BookmarkVO;
import com.personalhub.knowledge.service.TagService;
import com.personalhub.knowledge.vo.TagVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
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
    private final CategoryService categoryService;
    private final TagService tagService;

    @Override
    public IPage<BookmarkVO> list(Long userId, BookmarkQueryDTO query) {
        Page<BookmarkUrl> page = new Page<>(query.getPage(), query.getSize());
        IPage<BookmarkUrl> urlPage = bookmarkUrlMapper.selectBookmarkPage(page, userId, query);

        // 批量加载分类名称和标签
        Map<Long, String> categoryMap = loadCategoryNames(userId);
        List<Long> bookmarkIds = urlPage.getRecords().stream().map(BookmarkUrl::getId).collect(Collectors.toList());
        Map<Long, List<TagVO>> tagsMap = tagService.getTagsMap(userId, "bookmark", bookmarkIds);

        return urlPage.convert(url -> {
            BookmarkVO vo = BookmarkVO.from(url);
            if (url.getCategoryId() != null) {
                vo.setCategoryName(categoryMap.get(url.getCategoryId()));
            }
            vo.setTags(tagsMap.getOrDefault(url.getId(), List.of()));
            return vo;
        });
    }

    private Map<Long, String> loadCategoryNames(Long userId) {
        return categoryService.listByType(userId, "bookmark").stream()
                .collect(Collectors.toMap(com.personalhub.knowledge.vo.CategoryVO::getId, com.personalhub.knowledge.vo.CategoryVO::getName));
    }

    @Override
    public BookmarkVO getById(Long id, Long userId) {
        BookmarkUrl url = EntityGuard.requireOwned(
                bookmarkUrlMapper.selectById(id), userId, BookmarkUrl::getUserId, "收藏不存在");
        BookmarkVO vo = BookmarkVO.from(url);
        if (url.getCategoryId() != null) {
            com.personalhub.knowledge.vo.CategoryVO cat = categoryService.listByType(url.getUserId(), "bookmark").stream()
                    .filter(c -> c.getId().equals(url.getCategoryId())).findFirst().orElse(null);
            if (cat != null) vo.setCategoryName(cat.getName());
        }
        vo.setTags(tagService.getTags(userId, "bookmark", id));
        return vo;
    }

    @Override
    @Transactional
    public BookmarkVO create(Long userId, BookmarkCreateDTO dto) {
        var url = BookmarkUrl.builder()
                .userId(userId)
                .title(dto.getTitle())
                .url(dto.getUrl())
                .description(dto.getDescription())
                .categoryId(dto.getCategoryId())
                .showOnDashboard(dto.getShowOnDashboard() != null && dto.getShowOnDashboard() == 1 ? 1 : 0)
                .build();
        bookmarkUrlMapper.insert(url);
        log.info("新建收藏: id={}, userId={}, title={}", url.getId(), userId, dto.getTitle());

        // 保存标签关联
        if (dto.getTagIds() != null && !dto.getTagIds().isEmpty()) {
            tagService.bindTags(userId, url.getId(), "bookmark", dto.getTagIds());
        }

        BookmarkVO vo = BookmarkVO.from(url);
        if (url.getCategoryId() != null) {
            com.personalhub.knowledge.vo.CategoryVO cat = categoryService.listByType(url.getUserId(), "bookmark").stream()
                    .filter(c -> c.getId().equals(url.getCategoryId())).findFirst().orElse(null);
            if (cat != null) vo.setCategoryName(cat.getName());
        }
        vo.setTags(tagService.getTags(userId, "bookmark", url.getId()));
        return vo;
    }

    @Override
    @Transactional
    public BookmarkVO update(Long id, Long userId, BookmarkCreateDTO dto) {
        BookmarkUrl url = EntityGuard.requireOwned(
                bookmarkUrlMapper.selectById(id), userId, BookmarkUrl::getUserId, "收藏不存在");
        url.setTitle(dto.getTitle());
        url.setUrl(dto.getUrl());
        url.setDescription(dto.getDescription());
        url.setCategoryId(dto.getCategoryId());
        url.setShowOnDashboard(dto.getShowOnDashboard() != null && dto.getShowOnDashboard() == 1 ? 1 : 0);
        bookmarkUrlMapper.updateById(url);
        log.info("编辑收藏: id={}, userId={}", id, userId);

        // 更新标签关联
        tagService.bindTags(userId, id, "bookmark", dto.getTagIds());

        BookmarkVO vo = BookmarkVO.from(url);
        if (url.getCategoryId() != null) {
            com.personalhub.knowledge.vo.CategoryVO cat = categoryService.listByType(url.getUserId(), "bookmark").stream()
                    .filter(c -> c.getId().equals(url.getCategoryId())).findFirst().orElse(null);
            if (cat != null) vo.setCategoryName(cat.getName());
        }
        vo.setTags(tagService.getTags(userId, "bookmark", id));
        return vo;
    }

    @Override
    @Transactional
    public void delete(Long id, Long userId) {
        EntityGuard.requireOwned(
                bookmarkUrlMapper.selectById(id), userId, BookmarkUrl::getUserId, "收藏不存在");
        // 清除标签关联
        tagService.unbindAll(userId, "bookmark", id);
        bookmarkUrlMapper.deleteById(id);
        log.info("删除收藏: id={}, userId={}", id, userId);
    }

    @Override
    public List<BookmarkVO> listForDashboard(Long userId, int limit) {
        int size = Math.min(Math.max(limit, 1), 20);
        List<BookmarkUrl> urls = bookmarkUrlMapper.selectList(
                new LambdaQueryWrapper<BookmarkUrl>()
                        .eq(BookmarkUrl::getUserId, userId)
                        .eq(BookmarkUrl::getShowOnDashboard, 1)
                        .orderByDesc(BookmarkUrl::getUpdatedAt)
                        .last("LIMIT " + size));
        Map<Long, String> categoryMap = loadCategoryNames(userId);
        List<Long> ids = urls.stream().map(BookmarkUrl::getId).collect(Collectors.toList());
        Map<Long, List<TagVO>> tagsMap = tagService.getTagsMap(userId, "bookmark", ids);
        return urls.stream().map(url -> {
            BookmarkVO vo = BookmarkVO.from(url);
            if (url.getCategoryId() != null) {
                vo.setCategoryName(categoryMap.get(url.getCategoryId()));
            }
            vo.setTags(tagsMap.getOrDefault(url.getId(), List.of()));
            return vo;
        }).collect(Collectors.toList());
    }
}
