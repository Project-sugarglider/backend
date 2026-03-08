package com.projectsugarglider.kca.service;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.opencsv.CSVReaderHeaderAware;
import com.projectsugarglider.datainitialize.repository.LowerLocationCodeRepository;
import com.projectsugarglider.util.dto.TripleList;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class KcaLocationData {

    private final LowerLocationCodeRepository repo;

    /**
     * 소비자원 지역 데이터중 지역명이 다른 데이터를 보충합니다.
     */
    @Transactional
    public void insertData() {
        List<TripleList> codes = baseDataCall();

        for (TripleList data : codes) {
            repo.updateKcaCodeByKey(data.first(), data.second(), data.third());
        }
    }

    /**
     * 소비자원 지역명 보정 데이터를 호출합니다.
     *
     * 데이터는 API호출이 아닌
     * main/resources/kca-location-code-patch.csv 의 데이터를 참고합니다.
     *
     * @return 지역명 보정 데이터
     */
    public List<TripleList> baseDataCall() {
        try (CSVReaderHeaderAware reader = new CSVReaderHeaderAware(
                new InputStreamReader(new ClassPathResource("kca-location-code-patch.csv").getInputStream(), "UTF-8")
        )) {
            List<TripleList> result = new ArrayList<>();
            Map<String, String> line;

            while ((line = reader.readMap()) != null) {
                String upper = line.get("upper");
                String lower = line.get("lower");
                String code = line.get("code");

                if (upper == null || upper.isBlank() ||
                    lower == null || lower.isBlank() ||
                    code == null || code.isBlank()) {
                    log.warn("KCA 지역 보정 CSV 스킵 - 필수값 누락: {}", line);
                    continue;
                }

                TripleList dto = new TripleList(
                    upper.trim(),
                    lower.trim(),
                    code.trim()
                );
                result.add(dto);
            }

            return result;

        } catch (Exception e) {
            log.warn("KCA 지역 보정 CSV 읽기 실패", e);
            return List.of();
        }
    }
}