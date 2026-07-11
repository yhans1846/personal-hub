package com.personalhub.resource.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.personalhub.resource.dto.BookmarkQueryDTO;
import com.personalhub.resource.entity.BookmarkUrl;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 收藏夹 Mapper
 */
@Mapper
public interface BookmarkUrlMapper extends BaseMapper<BookmarkUrl> {

    /** 分页查询收藏（含标签子查询） */
    IPage<BookmarkUrl> selectBookmarkPage(Page<?> page, @Param("userId") Long userId, @Param("query") BookmarkQueryDTO query);
}
