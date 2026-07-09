package com.personalhub.module.note.service;

import com.personalhub.module.note.entity.NoteCategory;

import java.util.List;

/**
 * 笔记分类服务接口
 */
public interface NoteCategoryService {

    List<NoteCategory> listByUser(Long userId);

    NoteCategory create(Long userId, String name, Integer sortOrder);

    NoteCategory update(Long id, Long userId, String name, Integer sortOrder);

    void delete(Long id, Long userId);
}
