# Project SugarGlider 🐿️

> 공공데이터를 활용하여 **날씨, 생필품, 전기 사용량, 상점 정보** 등을 수집·저장하고,  
> 클라이언트가 조회할 수 있는 **백엔드 서버** 프로젝트입니다.

---

## 📌 프로젝트 개요
- **목표**: 공공데이터 API를 정제해 DB에 저장하고, 이를 조회 API로 제공  
- **역할**: 개인 프로젝트 (서버 아키텍처 설계 및 전체 기능 구현)  
- **특징**: 배포/인프라보다는 **서버 코드 품질, 데이터 처리, API 설계**에 집중  

(배포관점은 ProjectSgarGlider를 참고해 주세요.)
---

## 🛠️ 기술 스택
- **Language / Framework**: Java 21, Spring Boot 3.x  
- **Database**: PostgreSQL (RDS), Redis  
- **API 연계**: 공공데이터포털, KEPCO  
- **Build / Tools**: Gradle, JUnit5, RestAssured, Postman  

---

## 📂 프로젝트 구조

```
src/main/java/com/projectsugarglider
 ├── api              # 외부 API 호출 모듈
 ├── controller       # REST API 컨트롤러
 ├── dto              # 요청/응답 DTO
 ├── entity           # JPA 엔티티
 ├── repository       # DB 접근 계층
 ├── service          # 비즈니스 로직 계층
 └── util             # 공통 유틸/예외 처리
```

---

## 🚀 주요 기능

### 1. 데이터 적재
- 공공데이터 API 호출 → JSON/XML 파싱 → DB 저장  
- 파티셔닝 + 유니크 제약 조건으로 중복 데이터 방지  

### 2. 조회 API
- 지역 기반으로 **오늘의 날씨, 전기 사용량, 생필품 가격, 상점 위치** 제공  
- DTO 계층을 통해 Entity → API 응답 객체 변환  

### 3. 데이터 품질 보정
- 위치 정보 누락/불일치 처리 로직  
  - 상점명 검색 → 도로명/일반 주소 비교  
  - 행정 단위 불일치(읍/면, 남구/미추홀구 등) 보완  
- 잘못된 데이터는 로그 적재 + 예외 처리  

### 4. 캐싱
- Redis In-Memory Cache 적용  
- 반복 조회 API 응답 속도 개선, DB 부하 완화  

### 5. 에러 처리
- 글로벌 예외 핸들러(@ControllerAdvice)  
- 공통 응답 포맷(JSON)으로 클라이언트 일관성 확보  

---

## 📑 API 예시

### 오늘의 데이터 조회
```
GET /api/v1/today?region=서울특별시&city=종로구
```

**응답 예시**
```json
{
  "region": "서울특별시",
  "city": "종로구",
  "weather": {
    "temp": "27.1",
    "condition": "맑음"
  },
  "electricity": {
    "usage": 350,
    "bill": 45000
  },
  "products": [
    { "name": "라면", "price": 950 },
    { "name": "계란", "price": 6580 }
  ]
}
```

---

## ✅ 성과
- **데이터 자동화**: 공공데이터 API → DB 적재 파이프라인 완성  
- **조회 API**: 지역 기반 생활 정보를 단일 API에서 제공  
- **로직 최적화**: 데이터 특성(읽기 중심, 적은 데이터 변경)에 맞춰 불필요한 DB 접근을 제거하고 응답 시간을 단축
- **안정성 확보**: 외부 API 장애에도 재시도/예외 처리 로직으로 서비스 연속성 보장  

---

## 🔮 향후 개선
- Swagger/OpenAPI 문서화  
- PostgreSQL 인덱싱/파티셔닝 고도화  
- 단위/통합 테스트 커버리지 확대  
