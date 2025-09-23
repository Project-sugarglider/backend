package com.projectsugarglider.front.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.projectsugarglider.front.service.LocationDataFix;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 부족한 지역 데이터 보충 컨트롤러
 */
@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/Api")
public class dataFixController {

    private final LocationDataFix fix;

    /**
     * 부족한 데이터 보충용 GET
     * 
     * @return
     */
    @GetMapping("/locationFix")
    public ResponseEntity<String> req(){
        fix.service();
        return ResponseEntity.ok("데이터 픽스 완료");
    }
    
}
