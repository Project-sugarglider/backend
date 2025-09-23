# Project SugarGlider 🐿️

> 공공데이터를 활용하여 **날씨, 생필품, 전기 사용량, 상점 정보** 등을 수집·저장하고,  
> 클라이언트가 조회할 수 있는 **백엔드 서버** 프로젝트입니다.  

---

## 📌 프로젝트 개요
- **목표**: 공공데이터 API를 정제해 DB에 저장하고, 이를 조회 API로 제공
- **역할**: 개인 프로젝트 (서버 아키텍처 설계 및 전체 기능 구현)
- **특징**: 배포/인프라보다는 **서버 코드 품질, 데이터 처리, API 설계**에 집중

---

## 🛠️ 기술 스택
- **Language / Framework**: Java 21, Spring Boot 3.x
- **Database**: PostgreSQL (RDS), Redis
- **API 연계**: 공공데이터포털, KEPCO
- **Build / Tools**: Gradle, JUnit5, RestAssured, Postman

---

## 📂 프로젝트 구조

```bash
src/main/java/com/projectsugarglider
 ├── api              # 외부 API 호출 모듈
 ├── controller       # REST API 컨트롤러
 ├── dto              # 요청/응답 DTO
 ├── entity           # JPA 엔티티
 ├── repository       # DB 접근 계층
 ├── service          # 비즈니스 로직 계층
 └── util             # 공통 유틸/예외 처리
