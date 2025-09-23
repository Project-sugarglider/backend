package com.projectsugarglider.front.service;

import org.springframework.stereotype.Service;

import com.projectsugarglider.front.dto.TemperatureResponseDto;
import com.projectsugarglider.front.entity.WeatherCallHistory;
import com.projectsugarglider.front.repository.WeatherCallHistoryRepository;
import com.projectsugarglider.util.dto.LocationDto;
import com.projectsugarglider.util.service.CodeToLocationDto;
import com.projectsugarglider.util.service.DateTime;
import com.projectsugarglider.weather.service.ShortTimeForecastService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

/**
 * 일기예보 캐싱 서비스
 */
@Service
@RequiredArgsConstructor
public class WeatherCallHistoryCheck {
    
    private final WeatherCallHistoryRepository repo;
    private final DateTime time;
    private final ShortTimeForecastService forecast;
    private final CodeToLocationDto codeToDto;
    private final TemperatureResponseService response;

    @Transactional
    public TemperatureResponseDto service(String upperCode, String lowerCode){
        String now = time.kstNowYYYYMMDD();

        if (!repo.existsByWeatherCallDayAndUpperCodeAndLowerCode(now, upperCode, lowerCode)){
            LocationDto loc = codeToDto.find(upperCode, lowerCode);
            WeatherCallHistory DTO = WeatherCallHistory.builder()
            .weatherCallDay(now)
            .upperCode(upperCode)
            .lowerCode(lowerCode)
            .build();
            
            repo.save(DTO);
            forecast.saveAllShortTimeForecast(loc);
        }
        return response.service(upperCode,lowerCode);
    };

}
