package com.projectsugarglider.apiconnect;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenericExternalApiService {

    //TODO : 로깅 원위치
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final XmlMapper xmlMapper;

    /**
     * 외부 API 호출을 헤더 없이 범용으로 처리합니다. 
     *
     * @param baseUrl       호출할 엔드포인트 URL
     * @param queryParams   쿼리 파라미터 맵 (key → value)
     * @param responseType  결과를 매핑할 DTO 배열 클래스 (예: KcaCommonDto[].class)
     * @param isXml         응답이 XML이면 true, JSON이면 false
     * @param dataNodePath  JsonNode.at() 에 전달할 JSON 경로 (예: "/data" 또는 "/result/iros.openapi.service.vo.stdInfoVO")
     * @param <T>           DTO 타입
     * @return              DTO 리스트
     */
    public <T> List<T> getCall(
            String baseUrl,
            Map<String, String> queryParams,
            Class<T[]> responseType,
            boolean isXml,
            String dataNodePath
    ) {
        String url    = buildUrl(baseUrl, queryParams);
        // log.info("▶▶▶ calling URL = {}", url);

        String raw    = doRequest(url); // 기존 방식 유지
        JsonNode root = parseRaw(raw, isXml);
        JsonNode data = extractDataNode(root, dataNodePath);
        return convertToDto(data, responseType);
    }

    /**
     * 외부 API 호출을 헤더를 포함해서 범용으로 처리합니다. 
     *
     * @param baseUrl       호출할 엔드포인트 URL
     * @param queryParams   쿼리 파라미터 맵 (key → value)
     * @param responseType  결과를 매핑할 DTO 배열 클래스
     * @param isXml         응답이 XML이면 true, JSON이면 false
     * @param dataNodePath  JsonNode.at() 경로
     * @param headers       요청 헤더 맵 (예: Authorization 등)
     * @param <T>           DTO 타입
     * @return              DTO 리스트
     */
    public <T> List<T> getCall(
            String baseUrl,
            Map<String, String> queryParams,
            Class<T[]> responseType,
            boolean isXml,
            String dataNodePath,
            Map<String, String> headers
    ) {
        String url    = buildUrl(baseUrl, queryParams);
        // log.info("▶▶▶ calling URL = {}", url);

        String raw    = doRequest(url, headers); // 헤더 버전
        JsonNode root = parseRaw(raw, isXml);
        JsonNode data = extractDataNode(root, dataNodePath);
        return convertToDto(data, responseType);
    }

    /**
     * Url을 build합니다.
     * serviceKey가 이미 URL-encoded(예: %2B/%3D 포함)된 경우 b.build(true)로 재인코딩 방지합니다.
     * 인코딩 이슈가 의심되면 qp에는 '원문 키'를 넣어주세요.
     * @param baseUrl   Build 하려고 하는 Url
     * @param qp        쿼리 파라미터
     * @return          Build된 Url
     */
    private String buildUrl(String baseUrl, Map<String,String> qp) {
        String sk = qp.getOrDefault("serviceKey", qp.get("ServiceKey")); 
        boolean keyLooksEncoded = sk != null && sk.contains("%"); 
    
        UriComponentsBuilder b = UriComponentsBuilder.fromHttpUrl(baseUrl);
        qp.forEach(b::queryParam);
    
        if (keyLooksEncoded) {
            return b.build(true).toUriString();  
        } else {
            return b.encode(java.nio.charset.StandardCharsets.UTF_8)
                    .build()
                    .toUriString();               
        }
    }
    

    /**
     * 헤더를 포함하지 않는 Url로 Request합니다.
     * 
     * @param url   원본 Url
     * @return      반환된 데이터
     */
    private String doRequest(String url) {
        return doRequest(url, Collections.emptyMap());
    }

    /**
     * 헤더를 포함하는 Url로 Request합니다.
     * 헤더가 없다면 doRequest(String)으로 호출해야 합니다.
     * 
     * RestTemplate에 setConnectTimeout/setReadTimeout(예: 2s/5s) 설정한 Bean 주입이 권장됩니다.
     * 이번 프로젝트에서는 공공기관 데이터를 받아오는데, 타임아웃/호출실패가 가끔 발생합니다.
     * 재시도는 상위 계층에서 백오프 전략으로 처리하며 여기서는 단일 호출 책임을 유지합니다.
     * 
     * @param url       원본 Url
     * @param headers   헤더
     * @return          반환된 데이터
     */
    private String doRequest(String url, Map<String, String> headers) {
        HttpHeaders httpHeaders = new HttpHeaders();
        if (headers != null) {
            headers.forEach(httpHeaders::add);
        }
        HttpEntity<Void> entity = new HttpEntity<>(httpHeaders);
        ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
        return resp.getBody();
    }

    /**
     * Raw  데이터를 파싱합니다.
     * Xml/Json 데이터 모두 처리 가능하며
     * isXml을 통해 구분합니다.
     * 
     * @param raw       원본 데이터
     * @param isXml     Xml여부 (Xml = true, Json = false)
     * @return          파싱된 데이터
     * @throws UncheckedIOException
     */
    private JsonNode parseRaw(String raw, boolean isXml) throws UncheckedIOException {
        try {
            return isXml
                ? xmlMapper.readTree(raw)
                : objectMapper.readTree(raw);
        } catch (IOException e) {
            throw new UncheckedIOException("파싱 실패: " + raw, e);
        }
    }

    /**
     * 데이터에서 필요한 부분을 추출합니다.
     * 데이터와 경로를 입력하면 해당 경로의 데이터를 반환합니다.
     * 
     * @param root  원본 데이터
     * @param path  목표 경로
     * @return      경로에 포함되는 데이터
     */
    private JsonNode extractDataNode(JsonNode root, String path) {
        return root.at(path);
    }

    /**
     * Json데이터를 DTO로 변환합니다.
     * 
     * @param <T>   변환하고자 하는 DTO
     * @param data  원본 데이터
     * @param type  변환하려고 하는 Class(예시/ customDto[].class)
     * @return      DTO로 변환된 데이터
     */ 

    private <T> List<T> convertToDto(JsonNode data, Class<T[]> type) {
        T[] arr = objectMapper.convertValue(data, type);
        return arr != null ? List.of(arr) : Collections.emptyList();
    }
}
