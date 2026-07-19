# ---- build ----
# 默认走 npmmirror，国内无需代理即可装 pnpm/依赖；仍可通过 BUILD_HTTP_PROXY 覆盖
FROM node:22-bookworm-slim AS build
WORKDIR /web

ARG HTTP_PROXY
ARG HTTPS_PROXY
ARG http_proxy
ARG https_proxy
ARG NO_PROXY=localhost,127.0.0.1
ARG no_proxy=localhost,127.0.0.1
ARG NPM_REGISTRY=https://registry.npmmirror.com

ENV HTTP_PROXY=${HTTP_PROXY} \
    HTTPS_PROXY=${HTTPS_PROXY} \
    http_proxy=${http_proxy} \
    https_proxy=${https_proxy} \
    NO_PROXY=${NO_PROXY} \
    no_proxy=${no_proxy} \
    npm_config_registry=${NPM_REGISTRY}

# 用镜像源装 pnpm，避免 corepack 直连 registry.npmjs.org
RUN npm install -g pnpm@9.15.0 \
    && pnpm config set registry "${NPM_REGISTRY}"

COPY personal-hub-web/package.json personal-hub-web/pnpm-lock.yaml ./
RUN pnpm install --frozen-lockfile

COPY personal-hub-web/ ./
RUN pnpm build

# ---- runtime ----
FROM nginx:1.27-alpine
COPY deploy/nginx/default.conf /etc/nginx/conf.d/default.conf
COPY --from=build /web/dist /usr/share/nginx/html

EXPOSE 80
