package com.personalhub.module.note.service;

import com.personalhub.module.note.entity.NoteTag;

import java.util.List;

/**
 * 笔记标签服务接口
 */
public interface NoteTagService {

    /**
     * 获取用户的标签列表
     *
     * @param userId 用户ID
     * @return 标签列表
     */
    List<NoteTag> listByUser(Long userId);

    /**
     * 新建标签
     *
     * @param userId 用户ID
     * @param name   标签名称
     * @return 新建的标签
     */
    NoteTag create(Long userId, String name);

    /**
     * 修改标签
     *
     * @param id     标签ID
     * @param userId 用户ID
     * @param name   标签名称
     * @return 修改后的标签
     */
    NoteTag update(Long id, Long userId, String name);

    /**
     * 删除标签
     *
     * @param id     标签ID
     * @param userId 用户ID
     */
    void delete(Long id, Long userId);
}
