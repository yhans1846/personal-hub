package com.personalhub.resource.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.personalhub.resource.dto.FileQueryDTO;
import com.personalhub.resource.vo.FileVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件资源服务接口
 */
public interface FileResourceService {

    /**
     * 分页查询文件列表
     *
     * @param userId 用户ID
     * @param query  查询条件（关键词/类型/分类）
     * @return 文件分页数据
     */
    IPage<FileVO> list(Long userId, FileQueryDTO query);

    /**
     * 获取文件详情
     *
     * @param id     文件ID
     * @param userId 用户ID
     * @return 文件VO
     */
    FileVO getById(Long id, Long userId);

    /**
     * 上传文件
     *
     * @param userId 用户ID
     * @param file   上传文件
     * @param categoryId 分类ID（可选）
     * @return 文件VO
     */
    FileVO upload(Long userId, MultipartFile file, Long categoryId);

    /**
     * 获取文件存储路径
     *
     * @param id     文件ID
     * @param userId 用户ID
     * @return 文件实体（含路径信息）
     */
    com.personalhub.resource.entity.FileResource getFileResource(Long id, Long userId);

    /**
     * 删除文件
     *
     * @param id     文件ID
     * @param userId 用户ID
     */
    void delete(Long id, Long userId);
}
