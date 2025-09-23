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

//TODO : 로직 점검 및 함수 정리
/**
 * 부족한 데이터를 보충하고, fix하는 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LocationDataFix {

    private final StoreInfoRepository storeRepo;
    private final KakaoLocationFix api;

    private static final Pattern HAS_DIGIT  = Pattern.compile(".*\\d.*");
    private static final Pattern HAS_TILDE  = Pattern.compile(".*[~∼〜].*");
    private static final String  NUMBER_TOKEN = "\\d+(?:-\\d+)*";


    private static String cutAfterFirstNumberKeepToken(String s) {
        if (s == null) return null;
        s = s.trim();
        if (s.isEmpty()) return s;
    
        String[] tokens = s.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String t : tokens) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(t);
            if (HAS_DIGIT.matcher(t).find()) break; // 숫자 포함 토큰 '포함'하고 종료
        }
        return sb.toString();
    }

    private static boolean containsAllTokensInOrder(String a, String b) {
        if (a == null || b == null) return false;
    
        // 첫 단어만 축약 (서울특별시→서울 등)
        a = replaceFirstTokenByMap(a);
        b = replaceFirstTokenByMap(b);
    
        String[] A = tokens(a); // 공백 기준 분리 (이미 너가 쓰는 tokens 그대로 사용)
        String[] B = tokens(b);
        if (A.length == 0 || B.length == 0) return false;
    
        int j = 0;
        for (int i = 0; i < B.length && j < A.length; i++) {
            if (B[i].equals(A[j])) j++;
        }
        return j == A.length;
    }

    // 첫 단어만 RENAMEUPPOSIMPLY 기준으로 치환하고 나머지는 그대로 둠.
    // 예) "서울특별시 중구 다동 101-203" -> "서울 중구 다동 101-203"
    private static String replaceFirstTokenByMap(String s) {
        if (s == null) return "";
        s = s.trim();
        if (s.isEmpty()) return "";

        String[] parts = s.split("\\s+", 2);
        String first = parts[0];

        String mapped = ApiNameFix.RENAMEUPPOSIMPLY.get(first);
        if (mapped == null) return s;

        if (parts.length == 1) return mapped;
        return mapped + " " + parts[1];
    }

    // 숫자 포함 토큰부터(그 토큰 포함) 뒤를 잘라냄. 첫 단어 치환 포함.
    private static String simplifyAndCutAfterNumber(String s) {
        if (s == null) return "";
        s = s.trim();
        if (s.isEmpty()) return "";

        s = replaceFirstTokenByMap(s);

        String[] tokens = s.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String t : tokens) {
            if (HAS_DIGIT.matcher(t).find()) break;
            if (sb.length() > 0) sb.append(' ');
            sb.append(t);
        }
        return sb.toString();
    }

    // 디테일 문자열: 맨 앞이 숫자/하이픈 조합이면 그 토큰만 반환. 아니면 숫자 토큰부터 컷.
    private static String simplifyAndCutAfterString(String s) {
        if (s == null) return "";
        s = s.trim();
        if (s.isEmpty()) return "";

        String[] tokens = s.split("\\s+");
        if (tokens.length > 0 && tokens[0].matches(NUMBER_TOKEN)) {
            return tokens[0];
        }

        s = replaceFirstTokenByMap(s);
        tokens = s.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String t : tokens) {
            if (HAS_DIGIT.matcher(t).find()) break;
            if (sb.length() > 0) sb.append(' ');
            sb.append(t);
        }
        return sb.toString();
    }

    // 물결(~, ∼, 〜)이 포함된 토큰부터 컷. 첫 단어 치환 포함.
    private static String deleteWaveString(String s) {
        if (s == null) return "";
        s = s.trim();
        if (s.isEmpty()) return "";

        s = replaceFirstTokenByMap(s);

        String[] tokens = s.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String t : tokens) {
            if (HAS_TILDE.matcher(t).find()) break;
            if (sb.length() > 0) sb.append(' ');
            sb.append(t);
        }
        return sb.toString();
    }

    private static String[] tokens(String s) {
        if (s == null) return new String[0];
        s = s.trim();
        if (s.isEmpty()) return new String[0];
        return s.split("\\s+");
    }

    /** ref(=DB) 토큰 개수 N을 기준으로, cand(=API)의 앞 N토큰이 모두 동일하면 true */
    private static boolean equalsFirstTokensByRef(String ref, String cand) {
        String[] refT  = tokens(ref);
        String[] candT = tokens(cand);
        int N = refT.length;
        if (N == 0) return false;
        if (candT.length < N) return false;

        for (int i = 0; i < N; i++) {
            if (!refT[i].equals(candT[i])) return false;
        }
        return true;
    }

    @Transactional
    public void service(){
        List<KcaStoreInfoEntity> db = storeRepo.findAll();
        for (KcaStoreInfoEntity record : db) {

            if ((record.getXMapCoord() == null) || ("1".equals(record.getXMapCoord()))) {
                String dbAddrRaw   = record.getPlmkAddrBasic();
                String roadAddrRaw = record.getRoadAddrBasic();

                String dbAddr   = simplifyAndCutAfterNumber(dbAddrRaw);
                String roadAddr = simplifyAndCutAfterNumber(roadAddrRaw);

                boolean updated = false;

                // 1) 상호명으로 시도
                try {
                    List<KakaoPlace> data = api.localDataCall(record.getEntpName());
                    if (data != null && !data.isEmpty()) {
                        String apiAddr = null;
                        for (KakaoPlace raw : data) {
                            String apiAddrRaw = raw.address_name();
                            apiAddr = simplifyAndCutAfterNumber(apiAddrRaw);

                            if (equalsFirstTokensByRef(dbAddr, apiAddr)) {
                                log.info("첫번째 로직");
                                updated = method(record, raw);
                                break;
                            }
                            else if (
                                raw.place_name() != null &&
                                record.getEntpName() != null &&
                                raw.place_name().replaceAll("\\s+", "")
                                    .equals(record.getEntpName().replaceAll("\\s+", ""))
                            ) {
                                log.info("두번째 로직");
                                updated = method(record, raw);
                                break;
                            }
                            else if (java.util.Objects.equals(replaceFirstTokenByMap(roadAddrRaw), raw.road_address_name())) {
                                log.info("세번째 로직");
                                updated = method(record, raw);
                                break;
                            }
                        }
                    }
                } catch (Exception e) {
                    log.info("데이터 호출 실패(상호명): {}", record.getEntpName(), e);
                }

                // 2) 아직 업데이트 안 됐으면 도로명으로 재시도
                if (!updated) {
                    try {
                        List<KakaoPlace> data = api.localDataCall(roadAddr);
                        if (data != null && !data.isEmpty()) {
                            for (KakaoPlace raw : data) {
                                String apiAddrRaw = raw.road_address_name();

                                if (equalsFirstTokensByRef(roadAddr, apiAddrRaw)) {
                                    log.info("네번째 로직");
                                    updated = method(record, raw);
                                    break;
                                } else if (java.util.Objects.equals(replaceFirstTokenByMap(roadAddr), simplifyAndCutAfterNumber(apiAddrRaw))) {
                                    log.info("다섯번째 로직");
                                    updated = method(record, raw);
                                    break;
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.info("데이터 호출 실패(도로명): {}", roadAddrRaw, e);
                    }
                }

                // 3) 아직 업데이트 안 됐으면 기본주소+상세로 재시도
                if (!updated) {
                    try {
                        String plmk = cutAfterFirstNumberKeepToken(replaceFirstTokenByMap(deleteWaveString(dbAddrRaw)));
                        String plmkDetail = simplifyAndCutAfterString(record.getPlmkAddrDetail());
                        String Address = ((plmk == null ? "" : plmk) + " " + (plmkDetail == null ? "" : plmkDetail)).trim();

                        log.info("Address = {}", Address);
                        log.info("location = {}", record.getEntpName());

                        List<KakaoPlace> data = api.localDataCall(Address);
                        if (data != null && !data.isEmpty()) {
                            for (KakaoPlace raw : data) {
                                String apiRoadAddrRaw = raw.road_address_name();
                                String apiAddrRaw     = raw.address_name();

                                if (equalsFirstTokensByRef(Address, apiAddrRaw)) {
                                    log.info("여섯번째 로직");
                                    updated = method(record, raw);
                                    break;
                                } else if (java.util.Objects.equals(roadAddr, simplifyAndCutAfterNumber(apiRoadAddrRaw))) {
                                    log.info("일곱번째 로직");
                                    updated = method(record, raw);
                                    break;
                                }
                                else if (containsAllTokensInOrder(Address,apiAddrRaw)){
                                    log.info("여덟번째 로직");
                                    updated = method(record, raw);
                                    break;

                                }
                            }
                        }
                    } catch (Exception e) {
                        log.info("데이터 호출 실패(주소 합성): {}", dbAddrRaw, e);
                    }
                }

                // 최종 실패 로그(선택)
                if (!updated) {
                    // log.info("카카오 지역업데이트 실패 : {}", record.getEntpName());
                    // log.info("dbAddr = {}", dbAddr);
                }
            }
        }
    }

    private boolean method(KcaStoreInfoEntity record, KakaoPlace raw) {
        boolean updated;
        storeRepo.updateMapCoordByEntpNameAndPlmkAddrBasic(
            record.getEntpName(),
            record.getPlmkAddrBasic(),
            raw.x(),
            raw.y()
        );
        updated = true;
        return updated;
    }
}
