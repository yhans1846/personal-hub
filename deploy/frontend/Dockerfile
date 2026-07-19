# ---- build ----
# 默认走 npmmirror，构建期主动清空代理，避免误走宿主机 Clash（未 Allow LAN 会 ECONNREFUSED）
FROM node:22-bookworm-slim AS build
WORKDIR /web

ARG NPM_REGISTRY=https://registry.npmmirror.com

# 忽略 compose 传入的 BUILD_HTTP_PROXY，国内镜像直连即可
ENV HTTP_PROXY= \
    HTTPS_PROXY= \
    http_proxy= \
    https_proxy= \
    npm_config_registry=${NPM_REGISTRY} \
    npm_config_proxy= \
    npm_config_https_proxy=

RUN npm install -g pnpm@9.15.0 \
    && pnpm config set registry "${NPM_REGISTRY}" \
    && pnpm config delete proxy || true \
    && pnpm config delete https-proxy || true

COPY personal-hub-web/package.json personal-hub-web/pnpm-lock.yaml ./
RUN pnpm install --frozen-lockfile

COPY personal-hub-web/ ./
RUN pnpm build

# ---- runtime ----
FROM nginx:1.27-alpine
COPY deploy/nginx/default.conf /etc/nginx/conf.d/default.conf
COPY --from=build /web/dist /usr/share/nginx/html

EXPOSE 80
