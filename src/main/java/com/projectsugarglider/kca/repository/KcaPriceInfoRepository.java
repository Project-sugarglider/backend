package com.projectsugarglider.kca.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.projectsugarglider.kca.entity.KcaPriceInfoEntity;

@Repository
public interface KcaPriceInfoRepository extends JpaRepository<KcaPriceInfoEntity, String>{

    List<KcaPriceInfoEntity> findByEntpId(String entpId);

    List<KcaPriceInfoEntity> findByEntpIdAndGoodInspectDay(String entpId, String goodInspectDay);

    @Query("""
        select max(p.goodInspectDay)
        from KcaPriceInfoEntity p
        where p.entpId = :entpId
    """)
    Optional<String> findLatestInspectDayByEntpId(String entpId);

    boolean existsByEntpIdAndGoodInspectDay(String entpId, String goodInspectDay);
    boolean existsByEntpIdAndGoodInspectDayAndGoodId(String entpId, String goodInspectDay, String goodId);

}