# ==== runtime only (jar is built by GitHub Actions) ====
FROM eclipse-temurin:21-jre-alpine
ENV TZ=Asia/Seoul
WORKDIR /app

# GitHub Actions에서 생성한 JAR을 컨텍스트에서 복사
# (단일 부트앱 기준. 멀티모듈이면 경로를 해당 모듈로 바꾸세요)
COPY build/libs/*.jar app.jar

ENV JAVA_OPTS="-XX:+UseZGC -XX:MaxRAMPercentage=70 -XX:+ExitOnOutOfMemoryError"
EXPOSE 8080
HEALTHCHECK --interval=20s --timeout=3s --retries=5 CMD wget -qO- http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]
