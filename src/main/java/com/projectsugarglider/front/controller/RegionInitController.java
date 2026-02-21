package com.projectsugarglider.front.controller;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.projectsugarglider.front.dto.RegionInitResponseDto;
import com.projectsugarglider.front.service.LocationDataInsert;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class RegionInitController {

    private final LocationDataInsert location;
    @Cacheable(cacheNames = "regioninitCache")
    @GetMapping("/api/region/init")
    public RegionInitResponseDto regionInit(){
        return location.regionInit();
    }
}
