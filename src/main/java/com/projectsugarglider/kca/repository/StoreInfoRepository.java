package com.projectsugarglider.kca.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.projectsugarglider.front.dto.KcaStoreInfoResponseDto;
import com.projectsugarglider.kca.entity.KcaStoreInfoEntity;


public interface  StoreInfoRepository extends JpaRepository<KcaStoreInfoEntity, String> {
    
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE KcaStoreInfoEntity s
           SET s.xMapCoord = :xMapCoord,
               s.yMapCoord = :yMapCoord
         WHERE s.entpName = :entpName
           AND s.plmkAddrBasic = :plmkAddrBasic
    """)
    int updateMapCoordByEntpNameAndPlmkAddrBasic(
            @Param("entpName") String entpName,
            @Param("plmkAddrBasic") String plmkAddrBasic,
            @Param("xMapCoord") String xMapCoord,
            @Param("yMapCoord") String yMapCoord
    );


    @Query("""
        select new com.projectsugarglider.front.dto.KcaStoreInfoResponseDto(
            s.entpId,
            s.entpName,
            s.entpTelno,
            s.plmkAddrBasic,
            s.plmkAddrDetail,
            s.xMapCoord,
            s.yMapCoord
        )
        from KcaStoreInfoEntity s
        where s.upperCode = :upperCode
          and s.lowerCode = :lowerCode
    """)
    List<KcaStoreInfoResponseDto> findByUpperCodeAndLowerCode(
        @Param("upperCode") String upperCode,
        @Param("lowerCode") String lowerCode
    );
}
