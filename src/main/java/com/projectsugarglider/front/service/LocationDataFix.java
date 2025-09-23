package com.projectsugarglider.front.service;

import java.util.List;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projectsugarglider.front.api.KakaoLocationFix;
import com.projectsugarglider.front.dto.KakaoPlace;
import com.projectsugarglider.kca.entity.KcaStoreInfoEntity;
import com.projectsugarglider.kca.repository.StoreInfoRepository;
import com.projectsugarglider.util.service.ApiNameFix;

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
    private final KakaoLocationFix api;

    private static final Pattern HAS_DIGIT = Pattern.compile(".*\\d.*");

    /** 주소를 '큰 단위'만 남기도록 통일: 앞뒤/다중 공백 정리, 시·도 축약, 숫자 포함 토큰부터 잘라냄
     *  + (예외) 인천 남구 → 미추홀구 치환
     */
    private static String normalizeToArea(String addr) {
        if (addr == null) return "";
        String s = addr.trim().replaceAll("\\s+", " ");
        if (s.isEmpty()) return "";
    
        // 첫 단어만 축약 (서울특별시 → 서울 등)
        String[] parts = s.split("\\s+", 2);
        String first = parts[0];
        String mapped = ApiNameFix.RENAMEUPPOSIMPLY.get(first);
        if (mapped != null) {
            s = (parts.length == 1) ? mapped : mapped + " " + parts[1];
        }
    
        // 예외: 인천/인천광역시 + 남구 → 미추홀구
        String[] tokens = s.split("\\s+");
        if (tokens.length >= 2) {
            if (("인천".equals(tokens[0]) || "인천광역시".equals(tokens[0])) && "남구".equals(tokens[1])) {
                tokens[1] = "미추홀구";
            }
        }
    
        // 숫자 포함 토큰 만나기 전까지만 누적
        StringBuilder sb = new StringBuilder();
        for (String t : tokens) {
            if (HAS_DIGIT.matcher(t).find()) break;
            if (sb.length() > 0) sb.append(' ');
            sb.append(t);
        }
        return sb.toString().trim();
    }

    private static String extractSiGunGu(String area) {
        if (area == null) return "";
        String s = area.trim().replaceAll("\\s+", " ");
        if (s.isEmpty()) return "";
        String[] t = s.split("\\s+");
        StringBuilder sb = new StringBuilder();
        int max = Math.min(3, t.length); // 시/도, 시/군/구, (동/읍/면)
        for (int i = 0; i < max; i++) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(t[i]);
        }
        return sb.toString();
    }
    
    private static String normalizeEupMyeon(String token) {
        if (token == null) return "";
        token = token.trim();
        if (token.isEmpty()) return token;
        char last = token.charAt(token.length() - 1);
        if (last == '읍' || last == '면') {
            return token.substring(0, token.length() - 1) + "*"; // 호명읍/호명면 -> 호명*
        }
        return token;
    }

    private static boolean eqSGGRelaxed(String a, String b) {
        if (a == null || b == null) return false;
        String[] A = extractSiGunGu(a).split("\\s+");
        String[] B = extractSiGunGu(b).split("\\s+");
        int n = Math.min(A.length, B.length);
        if (n < 2) return false; // 최소 시/도 + 시/군/구
        for (int i = 0; i < n; i++) {
            String at = (i == 2) ? normalizeEupMyeon(A[i]) : A[i]; // 3번째 토큰만 읍/면 완화
            String bt = (i == 2) ? normalizeEupMyeon(B[i]) : B[i];
            if (!at.equals(bt)) return false;
        }
        return true;
    }
    

    private static boolean containsAllTokensInOrderForArea(String refArea, String candArea) {
        if (refArea == null || candArea == null) return false;
        refArea = refArea.trim();
        candArea = candArea.trim();
        if (refArea.isEmpty() || candArea.isEmpty()) return false;
    
        String[] A = refArea.split("\\s+");   // DB area 토큰들
        String[] B = candArea.split("\\s+");  // 후보 area 토큰들
        int j = 0;
        for (int i = 0; i < B.length && j < A.length; i++) {
            if (B[i].equals(A[j])) j++;
        }
        return j == A.length; // ref의 모든 토큰이 cand에 순서대로 등장해야 true
    }
    
    

    private static boolean isSamePlace(KcaStoreInfoEntity rec, KakaoPlace cand) {
        String dbPlmkArea = normalizeToArea(rec.getPlmkAddrBasic());
        String dbRoadArea = normalizeToArea(rec.getRoadAddrBasic());
    
        String apiAddrArea = normalizeToArea(cand.address_name());
        String apiRoadArea = normalizeToArea(cand.road_address_name());

        boolean plmkContained = !dbPlmkArea.isBlank() && (
                dbPlmkArea.equals(apiAddrArea) ||
                dbPlmkArea.equals(apiRoadArea) ||
                containsAllTokensInOrderForArea(dbPlmkArea, apiAddrArea) ||
                containsAllTokensInOrderForArea(dbPlmkArea, apiRoadArea)
        );
    
        boolean roadContained = !dbRoadArea.isBlank() && (
                dbRoadArea.equals(apiAddrArea) ||
                dbRoadArea.equals(apiRoadArea) ||
                containsAllTokensInOrderForArea(dbRoadArea, apiAddrArea) ||
                containsAllTokensInOrderForArea(dbRoadArea, apiRoadArea)
        );
    
        if (plmkContained || roadContained) return true;
    
        String dbPlmkSGG = extractSiGunGu(dbPlmkArea);
        String dbRoadSGG = extractSiGunGu(dbRoadArea);
        String apiAddrSGG = extractSiGunGu(apiAddrArea);
        String apiRoadSGG = extractSiGunGu(apiRoadArea);

        boolean sggMatch =
        (!dbPlmkSGG.isBlank() && (eqSGGRelaxed(dbPlmkSGG, apiAddrSGG) || eqSGGRelaxed(dbPlmkSGG, apiRoadSGG))) ||
        (!dbRoadSGG.isBlank() && (eqSGGRelaxed(dbRoadSGG, apiAddrSGG) || eqSGGRelaxed(dbRoadSGG, apiRoadSGG)));
    
        return sggMatch;
    }
    
    

    // ----------------- 상위: 가져오기 순서 유지 -----------------
    @Transactional
    public void service() {
        List<KcaStoreInfoEntity> db = storeRepo.findAll(); 
    
        int target = 0;   // needUpdate 대상 개수
        int success = 0;  // 좌표 업데이트 성공
        int fail = 0;     // 매칭 실패(업데이트 못함)
        int error = 0;    // 처리 중 예외
        int skip  = 0;    // needUpdate 아님(스킵)
    
        for (KcaStoreInfoEntity record : db) {
            if (needUpdate(record)) {
                target++;
                try {
                    boolean updated = processRecord(record); // ← 반환값으로 성공/실패 판정
                    if (updated) success++;
                    else          fail++;
                } catch (Exception e) {
                    error++;
                    log.warn("보정 실패 entp='{}'", record.getEntpName(), e);
                }
            } else {
                skip++;
            }
        }
    
        log.info("보정 집계 | 대상:{} 성공:{} 실패:{} 오류:{} 스킵:{} (총:{})",
                target, success, fail, error, skip, db.size());
    }
    
    private boolean needUpdate(KcaStoreInfoEntity record) {
        return record.getXMapCoord() == null || "1".equals(record.getXMapCoord());
    }
    
    /**
     * true  => 좌표 업데이트 성공
     * false => 후보를 찾았지만 조건 불충족 or 전혀 못찾음(매칭 실패)
     * 예외  => service()에서 error로 집계
     */
    private boolean processRecord(KcaStoreInfoEntity record) {
        // 1) entpName으로
        List<KakaoPlace> byName = safe(api.localDataCall(record.getEntpName()));
        for (KakaoPlace cand : byName) {
            if (isSamePlace(record, cand)) {
                return method(record, cand); // method()가 true 반환
            }
        }
    
        // 2) road로 (있을 때만)
        if (record.getRoadAddrBasic() != null && !record.getRoadAddrBasic().isBlank()) {
            List<KakaoPlace> byRoad = safe(api.localDataCall(record.getRoadAddrBasic()));
            for (KakaoPlace cand : byRoad) {
                if (isSamePlace(record, cand)) {
                    return method(record, cand);
                }
            }
        }
    
        // 3) plmk로
        if (record.getPlmkAddrBasic() != null && !record.getPlmkAddrBasic().isBlank()) {
            List<KakaoPlace> byPlmk = safe(api.localDataCall(record.getPlmkAddrBasic()));
            for (KakaoPlace cand : byPlmk) {
                if (isSamePlace(record, cand)) {
                    return method(record, cand);
                }
            }
        }
    
        log.info("매칭 실패: entp='{}'", record.getEntpName());
        return false;
    }
    
    private List<KakaoPlace> safe(List<KakaoPlace> list) {
        return (list != null) ? list : List.of();
    }
    
    private boolean method(KcaStoreInfoEntity record, KakaoPlace raw) {
        storeRepo.updateMapCoordByEntpNameAndPlmkAddrBasic(
            record.getEntpName(),
            record.getPlmkAddrBasic(),
            raw.x(),
            raw.y()
        );
        log.info("업데이트 성공: entp='{}' -> ({}, {})", record.getEntpName(), raw.x(), raw.y());
        return true;
    }
    
}