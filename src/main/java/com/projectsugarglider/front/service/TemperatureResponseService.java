package com.projectsugarglider.front.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.projectsugarglider.front.dto.TemperatureDataBundle;
import com.projectsugarglider.front.dto.TemperatureResponseDto;
import com.projectsugarglider.util.service.DateTime;
import com.projectsugarglider.weather.entity.ShortTimeForecastEntity;
import com.projectsugarglider.weather.repository.ShortTimeForecastRepository;

import lombok.RequiredArgsConstructor;

/**
 * 일기예보 호출 서비스
 */
@Service
@RequiredArgsConstructor
public class TemperatureResponseService {
    private static final String TEMP_CODE = "TMP";

    private final ShortTimeForecastRepository repo;
    private final DateTime time;

    public TemperatureResponseDto service(String upper, String lower){

        String[] tmdt = time.toYYYYMMDDHHMM(time.kstNow());
        String y = tmdt[0];
        String m = tmdt[1];
        String d = tmdt[2];

        List<ShortTimeForecastEntity> rows = getRows(upper, lower, y, m, d);
        TemperatureDataBundle bundle = buildLabelsAndData(rows);
        List<String> labels = bundle.labels();
        List<String> data = bundle.data();

        String datasetLabel = "기온(℃) - " + lower; 
        return extracted(labels, data, datasetLabel);
    }

    /**
     * 데이터를 받아 차트에 필요한 Dto로 변환하는 함수
     * 
     * @param labels
     * @param data
     * @param datasetLabel
     * @return
     */
    private TemperatureResponseDto extracted(List<String> labels, List<String> data, String datasetLabel) {
        return TemperatureResponseDto.builder()
                .labels(labels)
                .data(data)
                .datasetLabel(datasetLabel)
                .build();
    }

    /**
     * 일기예보 데이터를 가져오는 함수
     * 
     * @param rows
     * @return
     */
    private List<ShortTimeForecastEntity> getRows(String upper, String lower, String y, String m, String d) {
        List<ShortTimeForecastEntity> rows =
                repo.findByUpperCodeAndLowerCodeAndCodeAndYearAndMonthAndDayOrderByTimeAsc(
                        upper, lower, TEMP_CODE, y, m, d
                );
        return rows;
    }

    private static String formatLabel(String hhmm) {
        return hhmm.substring(0, 2) + ":" + hhmm.substring(2);
    }

    /**
     * labels/data를 만드는 함수
     */
    private static TemperatureDataBundle buildLabelsAndData(List<ShortTimeForecastEntity> rows) {
        int size = rows.size();
        List<String> labels = new ArrayList<>(size);
        List<String> data = new ArrayList<>(size);

        for (ShortTimeForecastEntity e : rows) {
            labels.add(formatLabel(e.getTime()));
            String v = e.getValue();
            if (Objects.nonNull(v)) {
                data.add(v);
            }
        }
        return new TemperatureDataBundle(labels, data);
    }
}
