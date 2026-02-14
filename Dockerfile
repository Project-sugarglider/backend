# ---- build stage ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Gradle 캐시 효율을 위해 먼저 설정/래퍼만 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

# 소스 복사
COPY src src

# 빌드 
RUN chmod +x gradlew \
    && ./gradlew clean bootJar -x test

# ---- run stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app

ENV SPRING_PROFILES_ACTIVE=prod

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
