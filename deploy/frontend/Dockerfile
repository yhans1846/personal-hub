# ---- build ----
# 构建期内网访问 npm：代理须指向「宿主机」而非 127.0.0.1（容器内回环）
# 例：HTTP_PROXY=http://host.docker.internal:7897
FROM node:22-bookworm-slim AS build
WORKDIR /web

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

RUN corepack enable && corepack prepare pnpm@9.15.0 --activate

COPY personal-hub-web/package.json personal-hub-web/pnpm-lock.yaml ./
RUN pnpm install --frozen-lockfile

COPY personal-hub-web/ ./
RUN pnpm build

# ---- runtime ----
FROM nginx:1.27-alpine
COPY deploy/nginx/default.conf /etc/nginx/conf.d/default.conf
COPY --from=build /web/dist /usr/share/nginx/html

EXPOSE 80
