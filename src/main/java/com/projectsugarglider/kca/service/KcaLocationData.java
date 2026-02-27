package com.projectsugarglider.kca.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.projectsugarglider.datainitialize.repository.LowerLocationCodeRepository;
import com.projectsugarglider.util.dto.TripleList;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * KCA(소비자원) 지역 데이터 업데이트용 서비스.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class KcaLocationData {

    private final LowerLocationCodeRepository repo;
    
    //TODO : 하드코딩이 아닌 지역데이터 저장할때 변환로직으로 바꾸기
    List<TripleList> codes = List.of(
        new TripleList("경기도",   "고양시일산동", "08000"),
        new TripleList("경기도",   "고양시일산서", "08000"),
        new TripleList("충청북도", "청주시상당",  "07000"),
        new TripleList("충청북도", "청주시흥덕",  "07000"),
        new TripleList("충청북도", "청주시청원",  "07000"),
        new TripleList("충청북도", "청주시서원",  "07000"),
        new TripleList("충청남도", "천안시동남",  "06000"),
        new TripleList("충청남도", "천안시서북",  "06000"),
        new TripleList("경기도",   "용인시처인",  "29000"),
        new TripleList("경기도",   "안산시단원",  "28000"),
        new TripleList("경기도",   "용인시기흥",  "29000"),
        new TripleList("경기도",   "용인시수지",  "29000"),
        new TripleList("경기도",   "안산시상록",  "28000"),
        new TripleList("경상남도", "창원시진해",  "12000"),
        new TripleList("경상남도", "창원시마산합포", "12000"),
        new TripleList("경상남도", "창원시마산회원", "12000"),
        new TripleList("경상남도", "창원시의창",  "12000"),
        new TripleList("경상남도", "창원시성산",  "12000"),
        new TripleList("인천광역시", "미추홀",  "08000")

    );

    /**
     * 소비자원 지역 데이터중 지역명이 다른 데이터를 보충합니다.
     */
    @Transactional
    public void insertData(){
        for(TripleList data : codes){
            repo.updateKcaCodeByKey(data.first(), data.second(), data.third());
        }
    }
}
