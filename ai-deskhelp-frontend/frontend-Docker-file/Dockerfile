# Stage 1: Build
FROM node:22-alpine AS builder

WORKDIR /myapp

COPY ai-deskhelp-frontend/package*.json ./
RUN npm ci

COPY ai-deskhelp-frontend .
RUN npm run build


# Stage 2: Runtime
FROM registry.access.redhat.com/ubi9/nginx-124

COPY --from=builder /myapp/dist/ai-deskhelp-frontend/browser /usr/share/nginx/html

COPY ai-deskhelp-frontend/frontend-Docker-file/nginx.conf /etc/nginx/nginx.conf

EXPOSE 8080

CMD ["nginx", "-g", "daemon off;"]
