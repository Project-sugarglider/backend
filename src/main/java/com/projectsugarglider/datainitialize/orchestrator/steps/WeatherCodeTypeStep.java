package com.projectsugarglider.datainitialize.orchestrator.steps;

import org.springframework.stereotype.Component;

import com.projectsugarglider.datainitialize.orchestrator.BasicDataContext;
import com.projectsugarglider.datainitialize.orchestrator.BasicDataStep;
import com.projectsugarglider.datainitialize.orchestrator.BasicDataStepResult;
import com.projectsugarglider.weather.service.WeatherCodeTypeInfo;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class WeatherCodeTypeStep implements BasicDataStep {

    private final WeatherCodeTypeInfo info;

    @Override
    public String name() {
        return "WEATHER_CODE_TYPE_UPDATE";
    }

    @Override
    public int order() {
        return 20;
    }

    @Override
    public BasicDataStepResult run(BasicDataContext context) {
        long start = System.currentTimeMillis();
        try {
            info.insertData();
            long took = System.currentTimeMillis() - start;
            return BasicDataStepResult.ok(name(), took, "Weather 코드타입(TMP 등) 기본데이터 업데이트 완료");
        } catch (Exception e) {
            long took = System.currentTimeMillis() - start;
            return BasicDataStepResult.fail(name(), took, "Weather 코드타입 업데이트 실패", e);
        }
    }
}
