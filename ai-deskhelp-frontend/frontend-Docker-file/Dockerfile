# Stage 1: Build
FROM node:22-alpine AS builder

WORKDIR /myapp

COPY ai-deskhelp-frontend/package*.json ./
RUN npm ci

COPY ai-deskhelp-frontend .

RUN npm run build


# Stage 2: Runtime
FROM nginx:alpine

COPY --from=builder /myapp/dist/ai-deskhelp-frontend/browser /usr/share/nginx/html

EXPOSE 80




# Step 5: Define the command to run your Spring Boot application



CMD ["nginx", "-g", "daemon off;"]
