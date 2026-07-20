package com.personalhub.backup;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户备份历史 Mapper
 */
@Mapper
public interface UserBackupMapper extends BaseMapper<UserBackup> {
}
