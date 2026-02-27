package com.projectsugarglider.datainitialize.orchestrator;


/**
 * 기본 데이터 초기화 작업의 단계를 나타내는 인터페이스입니다.
 * 각 단계는 고유한 이름과 실행 순서를 가지며, 실행 시 BasicDataStepResult를 반환합니다.
 * order() 메서드는 단계의 실행 순서를 정의하며, 낮은 숫자가 먼저 실행됩니다.
 */
public interface BasicDataStep {

    String name();
    int order();
    BasicDataStepResult run(BasicDataContext context);

}
