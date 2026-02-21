package com.projectsugarglider.weather.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.projectsugarglider.weather.entity.ShortTimeForecastEntity;
import com.projectsugarglider.weather.entity.id.ShortTimeForecastEntityId;



@Repository
public interface ShortTimeForecastRepository 
    extends JpaRepository<ShortTimeForecastEntity,ShortTimeForecastEntityId>{

    boolean existsByYearAndMonthAndDayAndTimeAndLowerCodeAndUpperCode(
        String year,
        String month,
        String day,
        String time,
        String lowerCode,
        String upperCode
        );

    Optional<ShortTimeForecastEntity> 
    findTopByLowerCodeAndUpperCodeOrderByYearDescMonthDescDayDescTimeDesc(
        String lowerCode, String upperCode);


    List<ShortTimeForecastEntity> findAllByLowerCodeAndUpperCode(String lowerCode, String upperCode);


    @Query("""
      SELECT CONCAT(e.year, e.month, e.day, e.time)
      FROM ShortTimeForecastEntity e
      WHERE e.lowerCode = :lowerCode
        AND e.upperCode = :upperCode
    """)
    List<String> findAllTimeKeysByLocation(
        @Param("lowerCode") String lowerCode,
        @Param("upperCode") String upperCode
    );

    Optional<ShortTimeForecastEntity>
    findFirstByUpperCodeAndLowerCodeAndCodeOrderByYearDescMonthDescDayDescTimeDesc(
            String upper, String lower, String code
    );

    List<ShortTimeForecastEntity>
    findByUpperCodeAndLowerCodeAndCodeAndYearAndMonthAndDayOrderByTimeAsc(
            String upper, String lower, String code, String year, String month, String day
    );
}