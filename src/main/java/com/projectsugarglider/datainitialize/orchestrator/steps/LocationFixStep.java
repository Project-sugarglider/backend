package com.projectsugarglider.datainitialize.orchestrator.steps;

import org.springframework.stereotype.Component;

import com.projectsugarglider.datainitialize.orchestrator.BasicDataContext;
import com.projectsugarglider.datainitialize.orchestrator.BasicDataStep;
import com.projectsugarglider.datainitialize.orchestrator.BasicDataStepResult;
import com.projectsugarglider.front.service.LocationDataFix;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LocationFixStep implements BasicDataStep {

    private final LocationDataFix locationDataFix;

    @Override
    public String name() {
        return "LOCATION_DATA_FIX";
    }

    @Override
    public int order() {
        return 40;
    }

    @Override
    public BasicDataStepResult run(BasicDataContext context) {
        long start = System.currentTimeMillis();
        try {

            locationDataFix.service();

            long took = System.currentTimeMillis() - start;
            return BasicDataStepResult.ok(name(), took, "KCA 가게 위치데이터 업데이트 완료");
        } catch (Exception e) {
            long took = System.currentTimeMillis() - start;
            return BasicDataStepResult.fail(name(), took, "KCA 가게 위치데이터 업데이트 실패", e);
        }
    }
}