# ---- build ----
# Maven 依赖走阿里云镜像（deploy/backend/maven-settings.xml），一般无需代理
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src

ENV HTTP_PROXY= \
    HTTPS_PROXY= \
    http_proxy= \
    https_proxy=

COPY personal-hub-server /src/personal-hub-server
COPY deploy/backend/maven-settings.xml /root/.m2/settings.xml
WORKDIR /src/personal-hub-server
RUN mvn -B -DskipTests package -pl ph-boot -am \
    && cp ph-boot/target/ph-boot-*.jar /src/app.jar

# ---- runtime ----
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

ENV HTTP_PROXY= \
    HTTPS_PROXY= \
    http_proxy= \
    https_proxy=

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && mkdir -p /data/personal-hub/uploads /data/personal-hub/logs

COPY --from=build /src/app.jar /app/app.jar

ENV SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-Xms256m -Xmx512m"

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=5 \
    CMD curl -sf --connect-timeout 3 http://127.0.0.1:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
