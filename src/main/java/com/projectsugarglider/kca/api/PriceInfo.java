package com.projectsugarglider.kca.api;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.projectsugarglider.apiconnect.GenericExternalApiService;
import com.projectsugarglider.kca.dto.KcaPriceInfoDto;
import com.projectsugarglider.util.service.DateTime;

import lombok.RequiredArgsConstructor;

/**
 * KCA(소비자원) 상품 가격정보 조회용 API 어댑터.
 */
@Service
@RequiredArgsConstructor
public class PriceInfo {
    
    @Value("${external.publicapi.apiKey}")
    private String apiKey;

        
    @Value("${external.kca.priceinfo}")
    private String BASE_URL;

    private final GenericExternalApiService apiService;
    private final DateTime dateTime;

    /**
     * 소비자원 상품 가격정보를 업체 아이디만으로 조회합니다.
     * 상품 아이디 없어도 호출 가능.
     *
     * @param entpId 소비자원 업체 아이디
     * @return       해당 업체의 가격정보 목록
     */
    public List<KcaPriceInfoDto> priceEntpCall(String entpId) {
        return priceInfoCall(entpId, null, dateTime.previousTwoWeekFridayYyyyMmDd());
    }

        public List<KcaPriceInfoDto> priceEntpCall(String entpId, String goodInspectDay) {
        return priceInfoCall(entpId, null, goodInspectDay);
    }

    /**
     * 소비자원 상품 가격정보를 상품 아이디만으로 조회합니다.
     * 업체 아이디 없어도 호출 가능.
     *
     * @param goodId 소비자원 상품 아이디
     * @return       해당 상품의 가격정보 목록
     */
    public List<KcaPriceInfoDto> priceGoodCall(String goodId) {
        return priceInfoCall(null, goodId, dateTime.previousTwoWeekFridayYyyyMmDd());
    }


/**
     * 소비자원 상품 가격정보를 조회합니다.
     * entpId, goodId 중 하나 이상을 넘기면 해당 조건으로 조회됩니다.
     * (둘 다 null/blank면 호출 의미 없음)
     *
     * @param entpId 소비자원 업체 아이디 (선택)
     * @param goodId 소비자원 상품 아이디 (선택)
     * @return       가격정보 목록 (엔트리 여러 건 가능)
     */
    public List<KcaPriceInfoDto> priceInfoCall(String entpId, String goodId, String goodInspectDay) {
        Map<String, String> params = getParams(entpId, goodId, goodInspectDay);

        return apiService.getCall(
            BASE_URL,
            params,
            KcaPriceInfoDto[].class,
            true,
            "/result/iros.openapi.service.vo.goodPriceVO"
        );
    }


    /*
     * 파라미터를 만드는 함수입니다.
     * entpId/goodId 둘 중 하나만 들어왔는지도 확인합니다.
     */
    private Map<String, String> getParams(String entpId, String goodId, String goodInspectDay) {
        boolean hasEntp = entpId != null && !entpId.isBlank();
        boolean hasGood = goodId != null && !goodId.isBlank();

        if (!hasEntp && !hasGood) {
            throw new IllegalArgumentException("entpId 또는 goodId 중 하나는 필수입니다.");
        }

        Map<String, String> params = new LinkedHashMap<>();
        params.put("serviceKey", apiKey);

        if (hasEntp) {
            params.put("entpId", entpId);
        } else {
            params.put("goodId", goodId);
        }

        params.put("goodInspectDay", goodInspectDay);
        return params;
    }

}