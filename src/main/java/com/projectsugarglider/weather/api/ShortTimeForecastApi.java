package com.projectsugarglider.weather.api;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.projectsugarglider.apiconnect.GenericExternalApiService;
import com.projectsugarglider.weather.dto.ShortTimeForecastDto;

import lombok.RequiredArgsConstructor;

/**
 * Werather(기상청) 단기예보 조회용 API 어댑터.
 */
@Service
@RequiredArgsConstructor
public class ShortTimeForecastApi{

   @Value("${external.publicapi.apiKey}")
    private String apiKey;

    @Value("${external.weather.shorttime}")
    private String BASE_URL;

    private final GenericExternalApiService apiService;
 
    public List<ShortTimeForecastDto> forecastCall(
        String nx,
        String ny,
        String now
    ){
        return apiService.getCall(
            BASE_URL, 
            Map.<String,String>of(
            "serviceKey",apiKey,
            "numOfRows", "1000",
            "pageNo", "1",
            "dataType","JSON",
            "base_date",now,
            "base_time","0200",
            "nx",nx,
            "ny",ny
            ),
            ShortTimeForecastDto[].class,
            false,
             "/response/body/items/item"
             );
    }
}
