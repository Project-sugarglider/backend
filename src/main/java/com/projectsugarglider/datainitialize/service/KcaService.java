package com.projectsugarglider.datainitialize.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.projectsugarglider.datainitialize.api.KcaAPI;
import com.projectsugarglider.datainitialize.dto.KcaCommonDto;
import com.projectsugarglider.datainitialize.repository.LowerLocationCodeRepository;
import com.projectsugarglider.datainitialize.repository.UpperLocationCodeRepository;
import com.projectsugarglider.util.service.ApiNameFix;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * KCA(소비자원) 지역 데이터 저장용 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KcaService {

    private final KcaAPI kcaAPI;
    private final UpperLocationCodeRepository upperRepo;
    private final LowerLocationCodeRepository lowerRepo;
    private final ApiNameFix nameFix;

    /**
     * 소비자원 지역 데이터를 업데이트합니다.
     */
    @Transactional
    public void updateBaseKcaData() {

        List<KcaCommonDto> list = kcaAPI.baseDataCall();
        String upperName = "default";
        Map<String, String> upperNameByKca = new HashMap<>();

        for (KcaCommonDto dto : list) {
            String code  = dto.code(); 
            String codeName = dto.codeName();
            String highCode = dto.highCode();

            // ex)0201/00000 - 서울특별시
            String upperKcaCode = code.substring(0, 4);
            String lowerKcaCode = code.substring(4, 9);
            
            if ("00000".equals(lowerKcaCode)) {

                codeName = nameFix.fixUpper(codeName);
                upperName = codeName;
                upperRepo.updateKcaCodeByKey(codeName, upperKcaCode);
                upperNameByKca.put(upperKcaCode, upperName);
            
            } else {

                codeName = nameFix.fixLower(codeName);
                String parentUpperKca = highCode.substring(0, 4);
                String resolvedUpperName = upperNameByKca.getOrDefault(parentUpperKca, upperName);
                lowerRepo.updateKcaCodeByKey(resolvedUpperName, codeName, lowerKcaCode);

            }
        }
    }
}
