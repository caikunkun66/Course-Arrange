FROM node:16-bullseye-slim AS build
WORKDIR /src
COPY UI/coursearrange/package.json UI/coursearrange/package-lock.json ./
RUN npm install --registry=https://registry.npmmirror.com
COPY UI/coursearrange/ ./
# 空字符串表示接口走当前站点，由 Nginx 转发到后端
ENV API_BASE_URL=
RUN npm run build

FROM nginx:1.25-alpine
COPY docker/nginx/default.conf /etc/nginx/conf.d/default.conf
COPY --from=build /src/dist /usr/share/nginx/html
EXPOSE 80
