package com.personalhub.module.note.service;

import com.personalhub.module.note.entity.NoteTag;

import java.util.List;

/**
 * 笔记标签服务接口
 */
public interface NoteTagService {

    List<NoteTag> listByUser(Long userId);

    NoteTag create(Long userId, String name);

    NoteTag update(Long id, Long userId, String name);

    void delete(Long id, Long userId);
}
