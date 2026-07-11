package com.personalhub.knowledge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.personalhub.knowledge.entity.StudyRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 学习记录 Mapper
 */
@Mapper
public interface StudyRecordMapper extends BaseMapper<StudyRecord> {

    /** 查询指定日期的学习时长 */
    long sumDurationByDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    /** 查询指定日期以来的学习时长 */
    long sumDurationSince(@Param("userId") Long userId, @Param("startDate") LocalDate startDate);

    /** 查询所有学习日期（去重） */
    List<LocalDate> listDistinctDates(@Param("userId") Long userId);
}
