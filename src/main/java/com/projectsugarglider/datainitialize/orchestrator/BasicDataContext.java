package com.projectsugarglider.datainitialize.orchestrator;

import java.time.OffsetDateTime;

/**
 * 기본 데이터 초기화 작업의 실행 컨텍스트를 나타내는 클래스입니다.
 * 각 단계에서 필요한 정보를 포함하며, 실행 중에 공유됩니다.
 */
public record BasicDataContext(

    String requestId,
    OffsetDateTime startedAt

) {}
