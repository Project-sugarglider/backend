package com.projectsugarglider.weather.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projectsugarglider.weather.entity.WeatherCodeTypeEntity;
import com.projectsugarglider.weather.repository.WeatherCodeTypeRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Weather(기상청) 기본정보 저장용 서비스.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherCodeTypeInfo {

    private final WeatherCodeTypeRepository repository;

    @Transactional
    public void insertData() {
        List<String> codes = List.of(
            "POP","PTY","PCP","REH","SNO","SKY","TMP","TMN","TMX","UUU","VVV","WAV","VEC","WSD"
        );
    
        var existing = repository.findAllById(codes)
                                 .stream().map(WeatherCodeTypeEntity::getCode)
                                 .collect(java.util.stream.Collectors.toSet());
    
        var toInsert = codes.stream()
            .filter(c -> !existing.contains(c))
            .map(c -> WeatherCodeTypeEntity.builder().code(c).build())
            .toList();
    
        if (!toInsert.isEmpty()) {
            repository.saveAll(toInsert);
        }
    }
    

}
