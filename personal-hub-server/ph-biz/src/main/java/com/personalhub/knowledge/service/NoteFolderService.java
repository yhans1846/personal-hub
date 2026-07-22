package com.personalhub.knowledge.service;

import com.personalhub.knowledge.dto.NoteFolderCreateDTO;
import com.personalhub.knowledge.dto.NoteFolderMoveDTO;
import com.personalhub.knowledge.dto.NoteFolderUpdateDTO;
import com.personalhub.knowledge.vo.NoteFolderTreeVO;
import com.personalhub.knowledge.vo.NoteFolderVO;

import java.util.List;

public interface NoteFolderService {

    int MAX_DEPTH = 5;

    NoteFolderTreeVO tree(Long userId);

    NoteFolderVO create(Long userId, NoteFolderCreateDTO dto);

    NoteFolderVO rename(Long userId, Long id, NoteFolderUpdateDTO dto);

    NoteFolderVO move(Long userId, Long id, NoteFolderMoveDTO dto);

    /** 删夹及子树；其下笔记 folder_id 置空 */
    void delete(Long userId, Long id);

    /** 校验文件夹属于用户（folderId 非空时） */
    void requireOwnedFolder(Long userId, Long folderId);
}
