package com.personalhub.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.personalhub.system.entity.UserLayout;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户布局配置 Mapper
 */
@Mapper
public interface UserLayoutMapper extends BaseMapper<UserLayout> {

    @Select("SELECT * FROM user_layout WHERE user_id = #{userId} AND layout_type = #{layoutType} AND is_deleted = 0")
    UserLayout selectByUserAndType(@Param("userId") Long userId, @Param("layoutType") String layoutType);
}
