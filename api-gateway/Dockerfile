FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# 멀티모듈 구조에서 빌드 결과물 복사
COPY api-gateway/build/libs/api-gateway-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]