package com.projectsugarglider.front.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.projectsugarglider.front.dto.KcaPriceWeeklyResponseDto;
import com.projectsugarglider.front.entity.KcaCallHistory;
import com.projectsugarglider.front.repository.KcaCallHistoryRepository;
import com.projectsugarglider.kca.repository.KcaPriceInfoRepository;
import com.projectsugarglider.kca.service.KcaPriceService;
import com.projectsugarglider.util.service.DateTime;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

/**
 * 업체정보에 맞는 생필품 캐싱 서비스
 */
@Service
@RequiredArgsConstructor
public class KcaCallHistoryCheck {
    
    private final KcaCallHistoryRepository repo;
    private final KcaPriceInfoRepository priceRepo;
    private final DateTime time;
    private final KcaPriceService price;
    private final kcaPriceResponseService response;

    @Transactional
    public KcaPriceWeeklyResponseDto service(String entpId){
        String kcaCallDay = time.kstNowYYYYMMDD();

        Optional<String> latest = priceRepo.findLatestInspectDayByEntpId(entpId);
        if (latest.isPresent()) {
            return response.listByEntpIdAndInspectDay(entpId, latest.get());
        }
        
        List<String> candidates = time.recentFridayCandidates(8);
        for (String goodInspectDay : candidates) {
            String callKey = entpId + ":" + goodInspectDay;
                if (!repo.existsByKcaCallDayAndEntpIdAndGoodInspectDay(kcaCallDay, entpId, goodInspectDay)) {
                    KcaCallHistory DTO = KcaCallHistory.builder()
                        .kcaCallDay(kcaCallDay)
                        .entpId(entpId)
                        .goodInspectDay(goodInspectDay)
                        .build();

                    repo.save(DTO);

                    price.SavePriceInfoData(entpId, goodInspectDay);
                }
                if (priceRepo.existsByEntpIdAndGoodInspectDay(entpId, goodInspectDay)) {
                    return response.listByEntpIdAndInspectDay(entpId, goodInspectDay);
                }
        }

        return new KcaPriceWeeklyResponseDto(null, List.of());

    }
}
