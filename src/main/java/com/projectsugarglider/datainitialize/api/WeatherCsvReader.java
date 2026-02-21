package com.projectsugarglider.datainitialize.api;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.opencsv.CSVReaderHeaderAware;
import com.projectsugarglider.datainitialize.dto.WeatherCommonDto;

/**
 * Weather(기상청) 지역 데이터 조회용 Api 어댑터
 */
@Service
public class WeatherCsvReader {

    /**
     * 기상청 지역 데이터를 호출합니다.
     * 
     * 데이터는 API호출이 아닌
     * main/resources/기상청데이터.csv의 데이터를 참고합니다.
     * 
     * @return     한전 지역 데이터
     */
    public List<WeatherCommonDto> baseDataCall() {
        try (CSVReaderHeaderAware reader = new CSVReaderHeaderAware(
                new InputStreamReader(new ClassPathResource("LocationData.csv").getInputStream(), "UTF-8")
        )) {
                List<WeatherCommonDto> result = new ArrayList<>();
                Map<String, String> line;
                while ((line = reader.readMap()) != null) {
                    WeatherCommonDto dto = new WeatherCommonDto(
                        line.get("행정구역코드"),
                        line.get("1단계"),
                        line.get("2단계"),
                        line.get("격자 X"),
                        line.get("격자 Y")
                    );
                    result.add(dto);
            }
            return result;

        } catch (Exception e) {
            return List.of();
        }
    }
}
