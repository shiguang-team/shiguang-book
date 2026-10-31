# 前端构建镜像（构建上下文为仓库根目录：docker build -f frontend/Dockerfile .）
FROM node:20-alpine AS build
WORKDIR /build
COPY frontend/package.json ./
RUN npm install --registry=https://registry.npmmirror.com
COPY frontend/ .
RUN npm run build

FROM nginx:1.25-alpine
COPY --from=build /build/dist /usr/share/nginx/html
COPY deploy/nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
