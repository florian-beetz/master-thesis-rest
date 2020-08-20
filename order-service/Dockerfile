FROM gradle:jdk13 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon

FROM openjdk:13-slim
EXPOSE 8080
RUN apt-get update && \
    apt-get -y install curl --no-install-recommends && \
    rm -rf /var/lib/apt/lists/*
RUN mkdir /app
HEALTHCHECK --start-period=10s CMD curl http://localhost:8080/order/api/status || exit 1
COPY --from=build /home/gradle/src/build/libs/*.jar /app/order-service.jar
ENTRYPOINT ["java","-jar","/app/order-service.jar"]
