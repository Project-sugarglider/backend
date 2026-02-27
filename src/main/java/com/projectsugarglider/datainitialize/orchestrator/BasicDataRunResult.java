package com.projectsugarglider.datainitialize.orchestrator;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * 기본 데이터 초기화 작업의 실행 결과를 나타내는 클래스입니다.
 * 요청 ID, 시작 시간, 종료 시간, 전체 성공 여부 및 각 단계의 결과를 포함합니다.
 */
public record BasicDataRunResult(

    String requestId,
    OffsetDateTime startedAt,
    OffsetDateTime finishedAt,
    boolean allSuccess,
    List<BasicDataStepResult> steps
) {}
