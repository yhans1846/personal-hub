package com.personalhub.module.study.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.personalhub.module.study.entity.StudyRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 学习记录 Mapper
 */
@Mapper
public interface StudyRecordMapper extends BaseMapper<StudyRecord> {

    @Select("SELECT COALESCE(SUM(duration), 0) FROM study_record WHERE user_id = #{userId} AND date = #{date} AND is_deleted = 0")
    long sumDurationByDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    @Select("SELECT COALESCE(SUM(duration), 0) FROM study_record WHERE user_id = #{userId} AND date >= #{startDate} AND is_deleted = 0")
    long sumDurationSince(@Param("userId") Long userId, @Param("startDate") LocalDate startDate);

    @Select("SELECT DISTINCT date FROM study_record WHERE user_id = #{userId} AND is_deleted = 0 ORDER BY date DESC")
    List<LocalDate> listDistinctDates(@Param("userId") Long userId);
}
