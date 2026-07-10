package com.personalhub.module.file.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.personalhub.module.file.entity.FileCategory;
import org.apache.ibatis.annotations.Mapper;

/** 文件分类 Mapper */
@Mapper
public interface FileCategoryMapper extends BaseMapper<FileCategory> {
}
