package com.personalhub.module.bookmark.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.personalhub.module.bookmark.entity.BookmarkCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 收藏夹分类 Mapper
 */
@Mapper
public interface BookmarkCategoryMapper extends BaseMapper<BookmarkCategory> {
}
