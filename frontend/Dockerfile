# Multi-stage build for the Vue SPA.
# Stage 1 builds the static assets with Vite; stage 2 serves them with nginx
# and proxies /api, /v3/api-docs, /swagger-ui and /actuator to the backend.

# ---- Build stage ----
FROM node:22-alpine AS build
WORKDIR /workspace

# Install dependencies first to cache the layer.
COPY package.json package-lock.json* ./
RUN npm ci
# Some NAS filesystems (notably UGOS/Btrfs volumes) drop the +x bit
# on files when the build context is copied across mount boundaries,
# and chmod on a symlink does not always propagate to its target on
# those filesystems. Force +x on:
#   1. every entry under node_modules/.bin (the npm shims)
#   2. every JS file living in any */bin/* or */.bin/* directory
#      (the real scripts the symlinks point to, e.g. vue-tsc.js)
RUN find node_modules/.bin -mindepth 1 -exec chmod +x {} + 2>/dev/null || true
RUN find node_modules -type f -name '*.js' \( -path '*/bin/*' -o -path '*/.bin/*' \) -exec chmod +x {} + 2>/dev/null || true

# Build the SPA. VITE_API_BASE_URL is empty by default, so calls hit
# relative paths ("/api/...") which nginx will proxy to the backend.
COPY . .
RUN npm run build

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
