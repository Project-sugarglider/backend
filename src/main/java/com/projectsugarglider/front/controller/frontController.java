package com.projectsugarglider.front.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.projectsugarglider.front.dto.KcaPriceResponseDto;
import com.projectsugarglider.front.dto.KcaStoreInfoResponseDto;
import com.projectsugarglider.front.dto.TemperatureResponseDto;
import com.projectsugarglider.front.service.KcaCallHistoryCheck;
import com.projectsugarglider.front.service.LocationDataInsert;
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

    private final LocationDataInsert location;
    private final KcaStoreInfoResponseService storeInfo;
    private final WeatherCallHistoryCheck check;
    private final KcaCallHistoryCheck kcaCheck;

    public record RegionPickRequest (String upper,  String lower) {}
    public record EntpReq(String entpId) {}

    /**
     * Front Main Page
     * 
     * @param model
     * @return
     */
    @GetMapping("/")
    public String main(Model model) {

        return "main"; 
    }

    /**
     * Front Weather(일기예보) Page
     * 
     * @param model
     * @return
     */
    @GetMapping("/weather")
    public String dashboard(Model model) {

        String upper = "경기도";
        String lower = "가평";
    
        TemperatureResponseDto dto = check.service(upper, lower);
        model.addAttribute("labels", dto.labels());
        model.addAttribute("data", dto.data());
        model.addAttribute("datasetLabel", dto.datasetLabel());
    
        location.dataSave(model);
    
        return "weather"; 
    }

    /**
     * 선택된 지역의 일기예보 데이터 반환
     * 
     * @param req
     * @return
     */
    @ResponseBody
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
    @ResponseBody
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
    @PostMapping("/api/table/map")
    public List<KcaStoreInfoResponseDto> storeLocationInfo(@RequestBody RegionPickRequest req){
        log.info("{}",req);
        return storeInfo.service(req.upper, req.lower);
    }

    /**
     * Front Kca(생필품) Page
     * 
     * @param model
     * @return
     */
    @GetMapping("/kca")
    public String kepco(Model model) {

        String upper = "경기도";
        String lower = "가평";

        TemperatureResponseDto dto = check.service(upper, lower);
        model.addAttribute("labels", dto.labels());
        model.addAttribute("data", dto.data());
        model.addAttribute("datasetLabel", dto.datasetLabel());
    
        location.dataSave(model);

        return "kca"; 
    }




}