package com.personalhub.resource.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.personalhub.common.exception.NotFoundException;
import com.personalhub.resource.dto.BookmarkCreateDTO;
import com.personalhub.resource.dto.BookmarkQueryDTO;
import com.personalhub.knowledge.service.CategoryService;
import com.personalhub.resource.entity.BookmarkUrl;
import com.personalhub.resource.mapper.BookmarkUrlMapper;
import com.personalhub.resource.service.BookmarkUrlService;
import com.personalhub.resource.vo.BookmarkVO;
import com.personalhub.knowledge.service.TagService;
import com.personalhub.knowledge.vo.TagVO;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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

    /**
     * 启动时自动迁移旧版逗号分隔标签到统一标签系统
     */
    @PostConstruct
    public void migrateOldTags() {
        List<BookmarkUrl> bookmarks = bookmarkUrlMapper.selectList(
                new LambdaQueryWrapper<BookmarkUrl>()
                        .isNotNull(BookmarkUrl::getTags)
                        .ne(BookmarkUrl::getTags, ""));

        int migrated = 0;
        for (BookmarkUrl bm : bookmarks) {
            // 检查是否已迁移
            List<TagVO> existingTags = tagService.getTags("bookmark", bm.getId());
            if (!existingTags.isEmpty()) continue;

            // 解析逗号分隔标签
            String[] tagNames = bm.getTags().split("\\s*,\\s*");
            for (String tagName : tagNames) {
                if (tagName.isBlank()) continue;
                try {
                    // 查找或创建标签
                    List<com.personalhub.knowledge.vo.TagVO> userTags = tagService.listByUser(bm.getUserId());
                    com.personalhub.knowledge.vo.TagVO tag = userTags.stream()
                            .filter(t -> t.getName().equals(tagName.trim()))
                            .findFirst().orElse(null);
                    if (tag == null) {
                        tag = tagService.create(bm.getUserId(), tagName.trim(), null);
                    }
                    tagService.bindTag(tag.getId(), "bookmark", bm.getId());
                } catch (Exception e) {
                    log.warn("迁移标签失败: bookmarkId={}, tagName={}", bm.getId(), tagName);
                }
            }
            migrated++;
        }
        if (migrated > 0) {
            log.info("收藏夹标签迁移完成: {} 条记录", migrated);
        }
    }

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
        // 标签筛选（通过 tag_rel 子查询）
        if (query.getTagId() != null) {
            wrapper.exists("SELECT 1 FROM tag_rel WHERE entity_id = id AND entity_type = 'bookmark' AND tag_id = " + query.getTagId());
        }

        wrapper.orderByDesc(BookmarkUrl::getCreatedAt);

        Page<BookmarkUrl> page = new Page<>(query.getPage(), query.getSize());
        IPage<BookmarkUrl> urlPage = bookmarkUrlMapper.selectPage(page, wrapper);

        // 批量加载分类名称和标签
        Map<Long, String> categoryMap = loadCategoryNames(userId);
        List<Long> bookmarkIds = urlPage.getRecords().stream().map(BookmarkUrl::getId).collect(Collectors.toList());
        Map<Long, List<TagVO>> tagsMap = tagService.getTagsMap("bookmark", bookmarkIds);

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
        BookmarkUrl url = bookmarkUrlMapper.selectById(id);
        if (url == null || !url.getUserId().equals(userId)) {
            log.warn("收藏不存在或无权访问: id={}, userId={}", id, userId);
            throw new NotFoundException("收藏不存在");
        }
        BookmarkVO vo = BookmarkVO.from(url);
        if (url.getCategoryId() != null) {
            com.personalhub.knowledge.vo.CategoryVO cat = categoryService.listByType(url.getUserId(), "bookmark").stream()
                    .filter(c -> c.getId().equals(url.getCategoryId())).findFirst().orElse(null);
            if (cat != null) vo.setCategoryName(cat.getName());
        }
        vo.setTags(tagService.getTags("bookmark", id));
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
        bookmarkUrlMapper.insert(url);
        log.info("新建收藏: id={}, userId={}, title={}", url.getId(), userId, dto.getTitle());

        // 保存标签关联
        if (dto.getTagIds() != null && !dto.getTagIds().isEmpty()) {
            tagService.bindTags(url.getId(), "bookmark", dto.getTagIds());
        }

        BookmarkVO vo = BookmarkVO.from(url);
        if (url.getCategoryId() != null) {
            com.personalhub.knowledge.vo.CategoryVO cat = categoryService.listByType(url.getUserId(), "bookmark").stream()
                    .filter(c -> c.getId().equals(url.getCategoryId())).findFirst().orElse(null);
            if (cat != null) vo.setCategoryName(cat.getName());
        }
        vo.setTags(tagService.getTags("bookmark", url.getId()));
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
        bookmarkUrlMapper.updateById(url);
        log.info("编辑收藏: id={}, userId={}", id, userId);

        // 更新标签关联
        tagService.bindTags(id, "bookmark", dto.getTagIds());

        BookmarkVO vo = BookmarkVO.from(url);
        if (url.getCategoryId() != null) {
            com.personalhub.knowledge.vo.CategoryVO cat = categoryService.listByType(url.getUserId(), "bookmark").stream()
                    .filter(c -> c.getId().equals(url.getCategoryId())).findFirst().orElse(null);
            if (cat != null) vo.setCategoryName(cat.getName());
        }
        vo.setTags(tagService.getTags("bookmark", id));
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
        // 清除标签关联
        tagService.unbindAll("bookmark", id);
        bookmarkUrlMapper.deleteById(id);
        log.info("删除收藏: id={}, userId={}", id, userId);
    }
}
