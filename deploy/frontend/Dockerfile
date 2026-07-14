# ---- build ----
FROM node:22-bookworm-slim AS build
WORKDIR /web

RUN corepack enable && corepack prepare pnpm@latest --activate

COPY personal-hub-web/package.json personal-hub-web/pnpm-lock.yaml ./
RUN pnpm install --frozen-lockfile

COPY personal-hub-web/ ./
RUN pnpm build

# ---- runtime ----
FROM nginx:1.27-alpine
COPY deploy/nginx/default.conf /etc/nginx/conf.d/default.conf
COPY --from=build /web/dist /usr/share/nginx/html

EXPOSE 80
