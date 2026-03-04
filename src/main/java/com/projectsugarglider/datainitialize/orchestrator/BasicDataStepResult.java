package com.projectsugarglider.datainitialize.orchestrator;

import com.projectsugarglider.config.error.ExceptionSummary;

/**
 * BasicDataStep의 실행 결과를 나타내는 클래스입니다.
 * 각 단계의 이름, 성공 여부, 실행 시간, 메시지, 에러 타입 및 에러 메시지를 포함합니다.
 */
public record BasicDataStepResult (

    String stepName,
    boolean success,
    long tookMs,
    String message,
    String errorType,
    String errorMessage

){

    public static BasicDataStepResult ok(String stepName, long tookMs, String message) {
        return new BasicDataStepResult(stepName, true, tookMs, message, null, null);
    }

    public static BasicDataStepResult fail(String stepName, long tookMs, String message, Exception e) {
        String summary = ExceptionSummary.summarize(e);
        return new BasicDataStepResult(stepName, false, tookMs, message, e.getClass().getName(), summary);
    }

}
