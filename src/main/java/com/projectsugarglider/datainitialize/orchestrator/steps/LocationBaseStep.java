package com.projectsugarglider.datainitialize.orchestrator.steps;

import org.springframework.stereotype.Component;

import com.projectsugarglider.datainitialize.orchestrator.BasicDataContext;
import com.projectsugarglider.datainitialize.orchestrator.BasicDataStep;
import com.projectsugarglider.datainitialize.orchestrator.BasicDataStepResult;
import com.projectsugarglider.datainitialize.service.BaseDataService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LocationBaseStep implements BasicDataStep {
    private final BaseDataService allLocationDataSaveService;

    @Override
    public String name() {
        return "LOCATION_BASE_UPDATE";
    }

    @Override
    public int order() {
        return 10;
    }

    @Override
    public BasicDataStepResult run(BasicDataContext context) {
        long start = System.currentTimeMillis();
        try {
            allLocationDataSaveService.saveAllLocations();
            long took = System.currentTimeMillis() - start;
            return BasicDataStepResult.ok(name(), took, "지역코드(기상청/소비자원/한전) 통합 업데이트 완료");
        } catch (Exception e) {
            long took = System.currentTimeMillis() - start;
            return BasicDataStepResult.fail(name(), took, "지역코드 통합 업데이트 실패", e);
        }

    }
}
