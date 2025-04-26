FROM docker.io/nginxinc/nginx-unprivileged:mainline-alpine
WORKDIR /usr/share/nginx/html
COPY dist/ .
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 8080
STOPSIGNAL SIGTERM
ENTRYPOINT ["nginx", "-g", "daemon off;"]
