# 1. 빌드 단계
FROM gradle:8.5-jdk21 AS builder
WORKDIR /app
COPY . .
# 테스트 제외하고 빌드 (속도 향상)
RUN ./gradlew clean build -x test

# 2. 실행 단계
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]