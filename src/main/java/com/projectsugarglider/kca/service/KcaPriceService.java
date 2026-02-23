package com.projectsugarglider.kca.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.projectsugarglider.kca.api.PriceInfo;
import com.projectsugarglider.kca.dto.KcaPriceInfoDto;
import com.projectsugarglider.kca.entity.KcaPriceInfoEntity;
import com.projectsugarglider.kca.repository.KcaPriceInfoRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * KCA(소비자원) 상품 가격정보 데이터 저장용 서비스.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KcaPriceService {
    
    private final PriceInfo priceInfo;
    private final KcaPriceInfoRepository priceRepo;

    /**
     * 소비자원 상품 가격정보 데이터를 저장합니다.
     * 
     * @param entp  소비자원 업체 코드(entpId)
     */
    @Transactional
    public void SavePriceInfoData(String entp) {
        List<KcaPriceInfoDto> raw = priceInfo.priceEntpCall(entp);
        saveEntities(raw);
    }

    @Transactional
    public void SavePriceInfoData(String entp, String goodInspectDay) {
        List<KcaPriceInfoDto> raw = priceInfo.priceEntpCall(entp, goodInspectDay);
        log.info("KCA API 호출 결과 | entpId={} goodInspectDay={} raw.size={}", entp, goodInspectDay, raw == null ? null : raw.size());

        saveEntities(raw);
    }

    private void saveEntities(List<KcaPriceInfoDto> raw) {
        List<KcaPriceInfoEntity> entities = getEntities(raw).stream()
            .map(e -> !"Y".equalsIgnoreCase(e.getGoodDcYn())
                ? KcaPriceInfoEntity.builder()
                    .goodInspectDay(e.getGoodInspectDay())
                    .entpId(e.getEntpId())
                    .goodId(e.getGoodId())
                    .goodPrice(e.getGoodPrice())
                    .plusoneYn(e.getPlusoneYn())
                    .goodDcYn(e.getGoodDcYn())
                    .goodDcStartDay(null)
                    .goodDcEndDay(null)
                    .inputDttm(e.getInputDttm())
                    .build()
                : e
            )
            .toList();

        // 중복 저장 방지 (같은 PK 이미 있으면 제외)
        List<KcaPriceInfoEntity> filtered = entities.stream()
            .filter(e -> !priceRepo.existsByEntpIdAndGoodInspectDayAndGoodId(
                e.getEntpId(),
                e.getGoodInspectDay(),
                e.getGoodId()
                
            ))
            .toList();

        if (!filtered.isEmpty()) {
            priceRepo.saveAll(filtered);
        }
    }


    private List<KcaPriceInfoEntity> getEntities(
        List<KcaPriceInfoDto> raw
        ) {
            return raw.stream()
            .map(KcaPriceInfoDto::toEntity)
            .toList();
        }
}
