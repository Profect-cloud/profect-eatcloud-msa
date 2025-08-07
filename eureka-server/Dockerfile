FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# 멀티모듈 구조에서 빌드 결과물 복사
COPY eureka-server/build/libs/eureka-server-*.jar app.jar

EXPOSE 8761

ENTRYPOINT ["java", "-jar", "app.jar"]