package com.projectsugarglider.datainitialize.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

/**
 * 기본 지역데이터를 한번에 업데이트하는 서비스
 */
@Service
@RequiredArgsConstructor
public class BaseDataService{
    private final WeatherService weatherLocationSaveService;
    private final KcaService kcaLocationSaveService;
    private final KepcoService kepcoLocationSaveService;

    /**
     * 지역데이터를 통합저장하는 코드
     * 기상청, 소비자원, 한전 데이터를 한번에 업데이트합니다.
     */
    public void saveAllLocations()  {
        weatherLocationSaveService.updateBaseWeatherData();
        kcaLocationSaveService.updateBaseKcaData();
        kepcoLocationSaveService.updateBaseKepcoData();
    }


}
