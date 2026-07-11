package com.personalhub.knowledge.service;

import com.personalhub.knowledge.entity.NoteCategory;

import java.util.List;

/**
 * 笔记分类服务接口
 */
public interface NoteCategoryService {

    /**
     * 获取用户的分类列表
     *
     * @param userId 用户ID
     * @return 分类列表
     */
    List<NoteCategory> listByUser(Long userId);

    /**
     * 新建分类
     *
     * @param userId    用户ID
     * @param name      分类名称
     * @param sortOrder 排序值
     * @return 新建的分类
     */
    NoteCategory create(Long userId, String name, Integer sortOrder);

    /**
     * 修改分类
     *
     * @param id        分类ID
     * @param userId    用户ID
     * @param name      分类名称
     * @param sortOrder 排序值
     * @return 修改后的分类
     */
    NoteCategory update(Long id, Long userId, String name, Integer sortOrder);

    /**
     * 删除分类
     *
     * @param id     分类ID
     * @param userId 用户ID
     */
    void delete(Long id, Long userId);
}
