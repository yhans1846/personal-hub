package com.personalhub.knowledge.service;

import com.personalhub.knowledge.dto.CategoryCreateDTO;
import com.personalhub.knowledge.dto.CategoryUpdateDTO;
import com.personalhub.knowledge.dto.SortOrderDTO;
import com.personalhub.knowledge.vo.CategoryVO;

import java.util.List;

/**
 * 统一分类服务接口
 */
public interface CategoryService {

    /**
     * 按类型查询分类列表
     */
    List<CategoryVO> listByType(Long userId, String type);

    /**
     * 创建分类
     */
    CategoryVO create(Long userId, CategoryCreateDTO dto);

    /**
     * 更新分类
     */
    CategoryVO update(Long id, Long userId, CategoryUpdateDTO dto);

    /**
     * 删除分类
     */
    void delete(Long id, Long userId);

    /**
     * 批量更新排序（校验归属）
     *
     * @param userId 当前用户
     * @param list   排序项
     */
    void updateSortOrder(Long userId, List<SortOrderDTO> list);
}
