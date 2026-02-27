package com.projectsugarglider.datainitialize.orchestrator;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BasicDataOrchestrator {
    
    /**
    * 기본 데이터 초기화 작업을 실행합니다.
    * 각 단계의 성공 여부와 실행 결과를 포함한 BasicDataRunResult를 반환합니다.
    */

    private final List<BasicDataStep> steps;
    
    public BasicDataRunResult runAll(String requestId, boolean stopOnFailure) {

        OffsetDateTime startedAt = OffsetDateTime.now();
        BasicDataContext context = new BasicDataContext(requestId, startedAt);

        List<BasicDataStepResult> results = new ArrayList<>();
        boolean allSuccess = true;

        List<BasicDataStep> ordered = steps.stream()
            .sorted(Comparator.comparingInt(BasicDataStep::order))
            .toList();

        for (BasicDataStep step : ordered) {
            BasicDataStepResult result = step.run(context);
            results.add(result);
            if (!result.success()) {
                allSuccess = false;
                if (stopOnFailure) break;
            }   
        }

        OffsetDateTime finishedAt = OffsetDateTime.now();
        return new BasicDataRunResult(requestId, startedAt, finishedAt, allSuccess, results);


    }
}
