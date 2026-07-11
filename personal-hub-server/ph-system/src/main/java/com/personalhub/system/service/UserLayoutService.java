package com.personalhub.system.service;

import com.personalhub.system.vo.LayoutVO;

import java.util.List;

/**
 * 用户布局配置 Service
 */
public interface UserLayoutService {

    /**
     * 获取用户指定类型的布局配置
     */
    LayoutVO get(Long userId, String layoutType);

    /**
     * 获取用户所有布局配置
     */
    List<LayoutVO> getAll(Long userId);

    /**
     * 保存或更新布局配置（upsert）
     */
    void save(Long userId, String layoutType, String layoutJson);

    /**
     * 导入布局配置
     */
    void importLayout(Long userId, String layoutJson);

    /**
     * 删除用户布局配置（恢复默认）
     */
    void delete(Long userId, String layoutType);
}
