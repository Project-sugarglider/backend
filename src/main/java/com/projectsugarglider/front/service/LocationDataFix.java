package com.projectsugarglider.front.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projectsugarglider.kca.entity.KcaStoreInfoEntity;
import com.projectsugarglider.kca.repository.StoreInfoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 단일 규칙 버전 (+ 인천 남구 → 미추홀구 예외 매핑)
 * - 상위(가져오기): 1) entpName -> 2) road_addr_basic -> 3) plmk_addr_basic
 * - 하위(검증): plmk/road 모두 같은 규칙 normalizeToArea 적용 후 교차 비교
 *
 * 규칙: 주소는 '첫 숫자(건물번호) 전까지' 큰 단위(시/군/구/동/로 등)만 남겨 비교한다.
 * 예외: 첫 토큰이 '인천'(또는 '인천광역시')이고 두 번째 토큰이 '남구'이면 '미추홀구'로 치환.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LocationDataFix {

    private final StoreInfoRepository storeRepo;
    private final LocationDataFixAsync locationDataFixAsync;

    // ----------------- 상위: 가져오기 순서 유지 -----------------
    @Transactional
    public void service() {
        List<KcaStoreInfoEntity> db = storeRepo.findAll();

        int target = 0;   // needUpdate 대상 개수
        int success = 0;  // 좌표 업데이트 성공
        int fail = 0;     // 매칭 실패(업데이트 못함)
        int error = 0;    // 처리 중 예외
        int skip  = 0;    // needUpdate 아님(스킵)

        List<CompletableFuture<Integer>> futures = new ArrayList<>();

        for (KcaStoreInfoEntity record : db) {
            if (needUpdate(record)) {
                target++;
                futures.add(locationDataFixAsync.processRecordAsync(record));
            } else {
                skip++;
            }
        }

        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();

        for (CompletableFuture<Integer> future : futures) {
            int result = future.join();
            switch (result) {
                case 1 -> success++;
                case 0 -> fail++;
                default -> error++;
            }
        }

        log.info("보정 집계 | 대상:{} 성공:{} 실패:{} 오류:{} 스킵:{} (총:{})",
                target, success, fail, error, skip, db.size());
    }

    private boolean needUpdate(KcaStoreInfoEntity record) {
        String x = record.getXMapCoord();
        String y = record.getYMapCoord();

        if (x == null || "1".equals(x)) return true;
        if (y == null || "1".equals(y)) return true;

        try {
            double xd = Double.parseDouble(x);
            double yd = Double.parseDouble(y);

            boolean normal = looksLikeLng(xd) && looksLikeLat(yd);

            return !normal;
        } catch (NumberFormatException e) {
            return true;
        }
    }

    private static boolean looksLikeLat(double v) {
        return v >= 33.0 && v <= 43.0;   // 한국 위도 대충 범위
    }

    private static boolean looksLikeLng(double v) {
        return v >= 124.0 && v <= 132.0; // 한국 경도 대충 범위
    }
}