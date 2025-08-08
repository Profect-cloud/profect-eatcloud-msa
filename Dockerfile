# === build ===
FROM gradle:8.8-jdk21 AS build
WORKDIR /workspace

# 전체 복사 (wrapper 포함)
COPY . .

# gradle wrapper 실행 권한
RUN chmod +x gradlew

# Gradle 캐시를 BuildKit 캐시로 사용 + 상세 로그 + 테스트 스킵
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew --no-daemon clean bootJar -x test --stacktrace --info

# === run ===
FROM eclipse-temurin:21-jre-alpine
ENV TZ=Asia/Seoul
WORKDIR /app

# 산출물 복사 (이름이 무엇이든 *.jar 하나만 있다고 가정)
COPY --from=build /workspace/build/libs/*.jar app.jar

ENV JAVA_OPTS="-XX:+UseZGC -XX:MaxRAMPercentage=70 -XX:+ExitOnOutOfMemoryError"
EXPOSE 8080
HEALTHCHECK --interval=20s --timeout=3s --retries=5 CMD wget -qO- http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]
