# Multi-stage build for the Vue SPA.
# Stage 1 builds the static assets with Vite; stage 2 serves them with nginx
# and proxies /api, /v3/api-docs, /swagger-ui and /actuator to the backend.

# ---- Build stage ----
FROM node:22-alpine AS build
WORKDIR /workspace

# Install dependencies first to cache the layer.
COPY package.json package-lock.json* ./
RUN npm ci

COPY . .
# Invoke vue-tsc and vite through node directly. The standard
# 'npm run build' relies on the +x bit of node_modules/.bin entries,
# but on some NAS filesystems (UGOS/Btrfs volumes) npm-installed
# scripts come out without it and 'sh: vue-tsc: Permission denied'
# fails the build. Calling the JS entry point with node bypasses
# the executable-bit requirement entirely.
RUN node ./node_modules/vue-tsc/bin/vue-tsc.js -b && node ./node_modules/vite/bin/vite.js build

# ---- Runtime stage ----
FROM nginx:1.27-alpine

# Replace the default site config with our proxy config.
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Static SPA output.
COPY --from=build /workspace/dist /usr/share/nginx/html

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
    CMD curl -fsS http://127.0.0.1/ >/dev/null || exit 1

CMD ["nginx", "-g", "daemon off;"]
