package com.personalhub.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.personalhub.system.entity.UserLayout;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户布局配置 Mapper
 */
@Mapper
public interface UserLayoutMapper extends BaseMapper<UserLayout> {

    UserLayout selectByUserAndType(@Param("userId") Long userId, @Param("layoutType") String layoutType);
}
