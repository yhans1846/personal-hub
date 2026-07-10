package com.personalhub.module.bookmark.service;

import com.personalhub.module.bookmark.dto.BookmarkCategoryDTO;
import com.personalhub.module.bookmark.vo.BookmarkCategoryVO;

import java.util.List;

/**
 * 收藏夹分类服务接口
 */
public interface BookmarkCategoryService {

    List<BookmarkCategoryVO> list(Long userId);

    BookmarkCategoryVO create(Long userId, BookmarkCategoryDTO dto);

    BookmarkCategoryVO update(Long id, Long userId, BookmarkCategoryDTO dto);

    void delete(Long id, Long userId);
}
