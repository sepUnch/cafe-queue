# Stage 1: Build the frontend
FROM node:22-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# Stage 2: Serve the frontend with Nginx
FROM nginx:alpine
# Copy the built assets from the build stage to Nginx web root
COPY --from=build /app/dist /usr/share/nginx/html

# Create a custom nginx config to route traffic to the backend proxy
RUN echo 'server { \
    listen 80; \
    server_name localhost; \
    \
    location / { \
        root /usr/share/nginx/html; \
        index index.html index.htm; \
        try_files $uri $uri/ /index.html; \
    } \
    \
    location /orders { \
        proxy_pass http://cafe_backend:8080; \
    } \
    location /preorders { \
        proxy_pass http://cafe_backend:8080; \
    } \
    location /menu { \
        proxy_pass http://cafe_backend:8080; \
    } \
    location /reconciliation { \
        proxy_pass http://cafe_backend:8080; \
    } \
    location /queue-board/ { \
        proxy_pass http://cafe_backend:8080; \
        proxy_set_header Connection ""; \
        proxy_http_version 1.1; \
        chunked_transfer_encoding off; \
        proxy_buffering off; \
        proxy_cache off; \
    } \
}' > /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
