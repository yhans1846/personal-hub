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

    /** 仅未删除记录（供查询） */
    UserLayout selectByUserAndType(@Param("userId") Long userId, @Param("layoutType") String layoutType);

    /**
     * 含软删记录（供 upsert：恢复默认后再次保存会撞 uk_user_layout）
     */
    UserLayout selectByUserAndTypeAny(@Param("userId") Long userId, @Param("layoutType") String layoutType);

    /** 更新 JSON 并清除软删标记 */
    int restoreAndUpdate(UserLayout entity);
}
