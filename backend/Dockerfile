# 1. 빌드 단계
FROM gradle:8.5-jdk21 AS builder
WORKDIR /app
COPY . .

# Windows 환경에서 생성된 gradlew의 줄바꿈 문자(CRLF)를 리눅스(LF)로 변경
RUN sed -i 's/\r$//' gradlew

# 실행 권한 부여 (추가)
RUN chmod +x ./gradlew

# 테스트 제외하고 빌드 (속도 향상)
RUN ./gradlew clean build -x test

# 2. 실행 단계
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]