package com.personalhub.resource.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.personalhub.resource.dto.BookmarkCreateDTO;
import com.personalhub.resource.dto.BookmarkQueryDTO;
import com.personalhub.resource.vo.BookmarkVO;

/**
 * 收藏夹服务接口
 */
public interface BookmarkUrlService {

    IPage<BookmarkVO> list(Long userId, BookmarkQueryDTO query);

    BookmarkVO getById(Long id, Long userId);

    BookmarkVO create(Long userId, BookmarkCreateDTO dto);

    BookmarkVO update(Long id, Long userId, BookmarkCreateDTO dto);

    void delete(Long id, Long userId);
}
