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
    long start = System.currentTimeMillis();

    int upperCount = 0;
    int lowerCount = 0;
    int upperUpdateCallCount = 0;
    int lowerUpdateCallCount = 0;

    try {
        log.info("[KcaBase] update start");

        List<KcaCommonDto> list = kcaAPI.baseDataCall();
        log.info("[KcaBase] kcaAPI.baseDataCall done listSize={}", list.size());

        String upperName = "default";
        Map<String, String> upperNameByKca = new HashMap<>();

        for (KcaCommonDto dto : list) {
            String code = dto.code();
            String codeName = dto.codeName();
            String highCode = dto.highCode();

            if (code == null || code.length() < 9) {
                log.warn("[KcaBase] skip invalid code dto={}", dto);
                continue;
            }

            // ex)0201/00000 - 서울특별시
            String upperKcaCode = code.substring(0, 4);
            String lowerKcaCode = code.substring(4, 9);

            if ("00000".equals(lowerKcaCode)) {

                codeName = nameFix.fixUpper(codeName);
                upperName = codeName;

                upperRepo.updateKcaCodeByKey(codeName, upperKcaCode);
                upperUpdateCallCount++;
                upperCount++;

                upperNameByKca.put(upperKcaCode, upperName);

            } else {

                codeName = nameFix.fixLower(codeName);

                if (highCode == null || highCode.length() < 4) {
                    log.warn("[KcaBase] skip invalid highCode code={}, codeName={}, highCode={}", code, codeName, highCode);
                    continue;
                }

                String parentUpperKca = highCode.substring(0, 4);
                String resolvedUpperName = upperNameByKca.getOrDefault(parentUpperKca, upperName);

                lowerRepo.updateKcaCodeByKey(resolvedUpperName, codeName, lowerKcaCode);
                lowerUpdateCallCount++;
                lowerCount++;
            }
        }

        long end = System.currentTimeMillis();
        log.info("[KcaBase] update success listSize={}, upperCount={}, lowerCount={}, upperUpdateCallCount={}, lowerUpdateCallCount={}, elapsedMs={}",
            list.size(), upperCount, lowerCount, upperUpdateCallCount, lowerUpdateCallCount, (end - start));

    } catch (Exception e) {
        long end = System.currentTimeMillis();
        log.error("[KcaBase] update failed upperCount={}, lowerCount={}, upperUpdateCallCount={}, lowerUpdateCallCount={}, elapsedMs={}",
            upperCount, lowerCount, upperUpdateCallCount, lowerUpdateCallCount, (end - start), e);
        throw e;
    }
}
}
