# Frontend Dockerfile: build Angular app and serve with nginx
FROM node:22-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci --silent
COPY . .

# BACKEND_URL can be injected at build time with --build-arg BACKEND_URL=...
# For local builds without the arg it falls back to localhost so the image still builds.
ARG BACKEND_URL=http://localhost:8081
# Substitute __BACKEND_URL__ placeholder in the prod environment file before ng build
RUN sed -i "s|__BACKEND_URL__|${BACKEND_URL}|g" src/environments/environment.prod.ts
RUN npm run build -- --configuration production

# Angular (@angular/build:application) outputs to dist/<project>/browser/
FROM nginx:stable-alpine
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf
# nginx serves the built app on port 8080.
EXPOSE 8080
CMD ["nginx", "-g", "daemon off;"]

