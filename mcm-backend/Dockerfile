FROM eclipse-temurin:21-alpine

WORKDIR /usr/local/src
COPY mcm-server/build/libs/mcm-server.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]
