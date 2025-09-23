package com.projectsugarglider.weather.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.projectsugarglider.util.dto.LocationDto;
import com.projectsugarglider.util.service.CodeToLocationDto;
import com.projectsugarglider.weather.dto.ShortForecastRequest;
import com.projectsugarglider.weather.service.ShortTimeForecastService;
import com.projectsugarglider.weather.service.WeatherCodeTypeInfo;

import lombok.RequiredArgsConstructor;

/**
 * Weather(기상청) 기본 컨트롤러.
 */
@RestController
@RequestMapping("/Weather")
@RequiredArgsConstructor
public class WeatherController {

    private final ShortTimeForecastService shortTimeForecastService;
    private final WeatherCodeTypeInfo info;
    private final CodeToLocationDto codeToLocationDto;


    /**
     * 기상청 기본데이터를 업데이트합니다.
     * 
     * @return 데이터 저장 완료 메시지
     * @throws JsonProcessingException
     */
    @PostMapping("/BasicDataUpdate")
    public ResponseEntity<String> basicDataUpdate() throws JsonProcessingException{
        info.insertData();
        return ResponseEntity.ok("기본데이터 업데이트 성공");
    }

    /**
     * 기상청 단기예보 데이터를 업데이트합니다.
     * 
     * /Weather/BasicDataUpdate를 선행해야 합니다.
     * 
     * @return 데이터 저장 완료 메시지
     */

    @PostMapping("/ShortForecast")
    public ResponseEntity<String> basicDataUpdate(
        @RequestBody ShortForecastRequest req
    ) {
        LocationDto loc = codeToLocationDto.find(req.upperCode(), req.lowerCode());
        shortTimeForecastService.saveAllShortTimeForecast(loc);
        return ResponseEntity.ok("날씨데이터 저장 성공");
    }

}