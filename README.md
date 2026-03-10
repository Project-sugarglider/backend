# Project SugarGlider 🐿️

> 공공데이터 API를 수집·정제하여 **생활 정보(날씨, 생필품 가격, 전기 사용량, 상점 위치)**를 제공하는 백엔드 서버 프로젝트

---

## 📌 프로젝트 개요
- **목표**: 공공데이터 API를 정제해 DB에 저장하고, 이를 조회 API로 제공  
- **역할**: 개인 프로젝트 (서버 아키텍처 설계 및 전체 기능 구현)  
- **특징**: 배포/인프라보다 **데이터 정제 로직, API 설계, 서버 코드 품질**에 집중

---

## 🛠️ 기술 스택
- **Language / Framework**: Java 21, Spring Boot 3.x
- **Database**: PostgreSQL
- **Cache**: Caffeine
- **External APIs**: 공공데이터포털, KEPCO, Kakao Local API
- **Build / Tools**: Gradle, JUnit5, RestAssured, Postman

---

## 🏗️ 아키텍처

### 데이터 요청의 경우
Client
  │
  ▼
REST API
  │
  ▼
Service Layer
  │
  ▼
Cache (Caffeine)
  │
  ▼
PostgreSQL

### 데이터 적재의 경우
External APIs
  │
  ▼
Data Initialize Pipeline
  │
  ▼
PostgreSQL

---

## 📂 패키지 구조
> Spring Boot 애플리케이션이 레이어별로 어떻게 구성되어 있는지에 대한 정리

```
src/main/java/com/projectsugarglider/
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
- 중복 데이터 저장을 방지하기 위한 제약 조건 적용

### 2. 조회 API
- 지역 기반으로 **오늘의 날씨, 전기 사용량, 생필품 가격, 상점 위치** 제공  
- DTO 계층을 통해 Entity → API 응답 객체 변환  

### 3. 데이터 품질 보정
- 공공데이터에서 누락되거나 불일치한 위치 정보를 보정
- Kakao Local API를 활용한 상점 위치 검색
- 도로명/지번 주소 비교 및 행정구역 단위 정규화
- 행정구역 변경(남구 → 미추홀구 등) 예외 처리
- 주소 기반 비교 로직을 통해 상점 위치 데이터 정합성 확보

### 4. 캐싱
- 지역 기반 조회 API에 캐시 적용
- 반복 조회 시 DB 접근을 줄여 응답 속도 개선

### 5. 에러 처리
- 공통 응답 포맷(JSON)으로 클라이언트 일관성 확보
- 외부 API 및 데이터 처리 과정에서 발생하는 오류를 공통 예외 처리로 관리


---

## ✅ 성과

- **데이터 적재 자동화**  
  공공데이터 API → DB 적재 파이프라인 구축

- **데이터 품질 개선**  
  주소 기반 위치 보정 로직으로 상점 위치 정합성 확보

- **응답 성능 개선**  
  Caffeine 캐시를 적용하여 반복 조회 시 DB 접근을 줄이고 응답 시간을 단축

- **운영 안정성 확보**  
  공통 예외 처리 구조를 적용해 오류 상황을 일관되게 관리

---

## 🔮 향후 개선
- Swagger/OpenAPI 문서화
- PostgreSQL 인덱싱 및 파티셔닝 고도화
- 테스트 커버리지 확대
- 배치 처리 안정성 개선
