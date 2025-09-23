package com.projectsugarglider.kepco.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.projectsugarglider.kepco.dto.KepcoUsageDto;
import com.projectsugarglider.kepco.service.UsageService;

import lombok.RequiredArgsConstructor;

/**
 * KEPCO(한전) 기본 컨트롤러.
 */
@RestController
@RequestMapping("/Kepco")
@RequiredArgsConstructor
public class KepcoController {
    
    private final UsageService usageService;

    /**
     * 한전 전력 사용량 데이터를 업데이트합니다.
     * 
     * @return 데이터 저장 완료 메시지
     */
    @PostMapping("/UsageDataInsert")
    public ResponseEntity<String> usageDataUpdate(){
        usageService.insertUsageKepcoData();
        return ResponseEntity.ok("전력사용량 데이터 업데이트 성공");
    }

    /**
     * 작년 같은달의 데이터를 호출합니다.
     * 
     * @return 작년 같은달 전력 사용량 데이터
     */
    @GetMapping("/last-year-same-month")
    public List<KepcoUsageDto> lastYearSameMonth() {
        return usageService.getLastYearSameMonthUsage();
    }
}
