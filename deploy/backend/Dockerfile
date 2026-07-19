# ---- build ----
# Maven 拉依赖时若需代理：HTTP_PROXY=http://host.docker.internal:7897
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src

ARG HTTP_PROXY
ARG HTTPS_PROXY
ARG http_proxy
ARG https_proxy
ARG NO_PROXY=localhost,127.0.0.1
ARG no_proxy=localhost,127.0.0.1

ENV HTTP_PROXY=${HTTP_PROXY} \
    HTTPS_PROXY=${HTTPS_PROXY} \
    http_proxy=${http_proxy} \
    https_proxy=${https_proxy} \
    NO_PROXY=${NO_PROXY} \
    no_proxy=${no_proxy}

COPY personal-hub-server /src/personal-hub-server
WORKDIR /src/personal-hub-server
RUN mvn -B -DskipTests package -pl ph-boot -am \
    && cp ph-boot/target/ph-boot-*.jar /src/app.jar

# ---- runtime ----
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

ARG HTTP_PROXY
ARG HTTPS_PROXY
ARG http_proxy
ARG https_proxy
ARG NO_PROXY=localhost,127.0.0.1
ARG no_proxy=localhost,127.0.0.1

ENV HTTP_PROXY=${HTTP_PROXY} \
    HTTPS_PROXY=${HTTPS_PROXY} \
    http_proxy=${http_proxy} \
    https_proxy=${https_proxy} \
    NO_PROXY=${NO_PROXY} \
    no_proxy=${no_proxy}

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && mkdir -p /data/personal-hub/uploads /data/personal-hub/logs

# 运行期不需要构建代理
ENV HTTP_PROXY= \
    HTTPS_PROXY= \
    http_proxy= \
    https_proxy=

COPY --from=build /src/app.jar /app/app.jar

ENV SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-Xms256m -Xmx512m"

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=5 \
    CMD curl -sf --connect-timeout 3 http://127.0.0.1:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
