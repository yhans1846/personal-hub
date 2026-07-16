package com.personalhub.knowledge.service;

import com.personalhub.knowledge.vo.TagVO;

import java.util.List;
import java.util.Map;

/**
 * 标签服务接口
 */
public interface TagService {

    // ========== 标签 CRUD ==========

    /** 获取用户所有标签 */
    List<TagVO> listByUser(Long userId);

    /** 创建标签 */
    TagVO create(Long userId, String name, String color);

    /** 更新标签 */
    void update(Long id, Long userId, String name, String color);

    /** 删除标签（同时清除关联） */
    void delete(Long id, Long userId);

    // ========== 标签绑定 ==========

    /** 绑定标签到实体 */
    void bindTag(Long tagId, String entityType, Long entityId);

    /** 解绑标签 */
    void unbindTag(Long tagId, String entityType, Long entityId);

    /** 解绑实体的所有标签 */
    void unbindAll(String entityType, Long entityId);

    /** 批量绑定标签到实体（先清空再绑定） */
    void bindTags(Long entityId, String entityType, List<Long> tagIds);

    /** 获取实体的标签列表 */
    List<TagVO> getTags(String entityType, Long entityId);

    /** 批量获取实体的标签列表（key=entityId） */
    Map<Long, List<TagVO>> getTagsMap(String entityType, List<Long> entityIds);

}
