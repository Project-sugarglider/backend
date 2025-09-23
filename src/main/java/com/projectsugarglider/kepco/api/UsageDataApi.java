package com.projectsugarglider.kepco.api;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.projectsugarglider.apiconnect.GenericExternalApiService;
import com.projectsugarglider.kepco.dto.KepcoUsageDto;

import lombok.RequiredArgsConstructor;

/**
 * KEPCO(한전) 전력 사용량 조회용 API 어댑터.
 */
@Service
@RequiredArgsConstructor
public class UsageDataApi {
    
    @Value("${external.kepco.kepcokey}")
    private String apiKey;

    @Value("${external.kepco.usagedataurl}")
    private String BASE_URL;

    private final GenericExternalApiService apiService;

    /**
     * 전력 사용량 데이터를 호출합니다.
     * 
     * @return 전력 사용량 데이터
     */
    public List<KepcoUsageDto> usageDataCall(
        String year,
        String month,
        String metroCd
        ){

            return apiService.getCall(
                BASE_URL, 
                Map.of(
                    "year",year,
                    "month",month,
                    "metroCd",metroCd,
                    "apiKey",apiKey
                ), 
                KepcoUsageDto[].class, 
                false, 
                "/data"
            );
    }
}
