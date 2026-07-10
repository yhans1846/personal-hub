package com.personalhub.module.file.service;

import com.personalhub.module.file.entity.FileCategory;
import com.personalhub.module.file.dto.FileCategoryCreateDTO;
import java.util.List;

/**
 * 文件分类服务接口
 */
public interface FileCategoryService {

    /**
     * 获取用户的所有文件分类
     *
     * @param userId 用户ID
     * @return 分类列表
     */
    List<FileCategory> list(Long userId);

    /**
     * 新建分类
     *
     * @param userId 用户ID
     * @param dto    分类参数
     * @return 新建的分类
     */
    FileCategory create(Long userId, FileCategoryCreateDTO dto);

    /**
     * 更新分类
     *
     * @param id     分类ID
     * @param userId 用户ID
     * @param dto    分类参数
     * @return 更新后的分类
     */
    FileCategory update(Long id, Long userId, FileCategoryCreateDTO dto);

    /**
     * 删除分类
     *
     * @param id     分类ID
     * @param userId 用户ID
     */
    void delete(Long id, Long userId);
}
