FROM node:lts-alpine as build-stage
WORKDIR /app
COPY ../frontend .
COPY ../config/frontend .
RUN npm install --force
RUN npm run build

FROM nginx:stable-alpine as production-stage
COPY --from=build-stage /app/build /usr/share/nginx/html
COPY ../frontend/nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]