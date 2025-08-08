# === build ===
FROM gradle:8.8-jdk21 AS build
WORKDIR /workspace
COPY . .
RUN gradle -q --no-daemon clean bootJar

# === run ===
FROM eclipse-temurin:21-jre-alpine
ENV TZ=Asia/Seoul
WORKDIR /app
# 산출물 경로: build/libs/*.jar (프로젝트 설정에 따라 -SNAPSHOT 유무 상관없이 한 개만 있을 거라 가정)
COPY --from=build /workspace/build/libs/*jar app.jar
ENV JAVA_OPTS="-XX:+UseZGC -XX:MaxRAMPercentage=70 -XX:+ExitOnOutOfMemoryError"
EXPOSE 8080
HEALTHCHECK --interval=20s --timeout=3s --retries=5 CMD wget -qO- http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]
