package com.projectsugarglider.util.service;

import org.springframework.stereotype.Service;

import com.projectsugarglider.datainitialize.repository.LowerLocationCodeRepository;
import com.projectsugarglider.util.dto.LocationDto;

import lombok.RequiredArgsConstructor;

/**
 * 지역 x/y좌표 반환 서비스
 */
@Service
@RequiredArgsConstructor
public class CodeToLocationDto {

    private final LowerLocationCodeRepository repo;

    public LocationDto find(String upper, String lower) {
    return repo.findLocationWithCode(upper, lower);
  }
    
}
