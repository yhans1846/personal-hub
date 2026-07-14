# ---- build ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src

COPY personal-hub-server /src/personal-hub-server
WORKDIR /src/personal-hub-server
RUN mvn -B -DskipTests package -pl ph-boot -am \
    && cp ph-boot/target/ph-boot-*.jar /src/app.jar

# ---- runtime ----
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && mkdir -p /data/personal-hub/uploads

COPY --from=build /src/app.jar /app/app.jar

ENV SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-Xms256m -Xmx512m"

EXPOSE 8080

# 只要端口能响应 HTTP 即视为就绪（登录接口可能返回 4xx，不用 -f）
HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=5 \
    CMD curl -s -o /dev/null --connect-timeout 3 http://127.0.0.1:8080/api/auth/login || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
