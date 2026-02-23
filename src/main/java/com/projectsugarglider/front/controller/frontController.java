package com.projectsugarglider.front.controller;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.projectsugarglider.front.dto.KcaPriceResponseDto;
import com.projectsugarglider.front.dto.KcaStoreInfoResponseDto;
import com.projectsugarglider.front.dto.TemperatureResponseDto;
import com.projectsugarglider.front.service.KcaCallHistoryCheck;
import com.projectsugarglider.front.service.WeatherCallHistoryCheck;
import com.projectsugarglider.kca.service.KcaStoreInfoResponseService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Front Page 반환용 컨트롤러
 */
@Controller
@Slf4j
@RequiredArgsConstructor
public class frontController{

    private final KcaStoreInfoResponseService storeInfo;
    private final WeatherCallHistoryCheck check;
    private final KcaCallHistoryCheck kcaCheck;

    public record RegionPickRequest (String upper,  String lower) {}
    public record EntpReq(String entpId) {}

    /**
     * 선택된 지역의 일기예보 데이터 반환
     * 
     * @param req
     * @return
     */
    @ResponseBody
    @Cacheable(value = "temperature", key = "#req.upper + '-' + #req.lower")
    @PostMapping("/api/chart/tmp")
    public TemperatureResponseDto temperature(@RequestBody RegionPickRequest req){
        log.info("{},{}",req.upper,req.lower);
        return check.service(req.upper,req.lower);
    }

    /**
     * 지도에서 선택된 업체의 판매물품 데이터 반환
     * 
     * @param req
     * @return
     */
    // TODO: 상품의 종류, 검색기능 추가
    @ResponseBody
    @Cacheable(value = "kcaStoreInfoCache", key = "#req.entpId")
    @PostMapping("/api/table/KcaPriceInfoByEntpId")
    public List<KcaPriceResponseDto> priceInfo(@RequestBody EntpReq req){
        log.info("{}",req);
        return kcaCheck.service(req.entpId);
    }

    /**
     * 생필품 가계정보 반환
     * 
     * @param req
     * @return
     */
    @ResponseBody
    @Cacheable(value = "storeLocationCache", key = "#req.upper + '-' + #req.lower")
    @PostMapping("/api/table/map")
    public List<KcaStoreInfoResponseDto> storeLocationInfo(@RequestBody RegionPickRequest req){
        log.info("{}",req);
        return storeInfo.service(req.upper, req.lower);
    }

}