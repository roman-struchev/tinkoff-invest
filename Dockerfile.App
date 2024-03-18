FROM gradle:8.6.0-jdk21-alpine AS build
WORKDIR /app
COPY . ./
RUN gradle clean bootJar --info

FROM openjdk:21-slim
COPY --from=build /app/build/libs/invest-robot-*.jar application.jar
ENTRYPOINT ["java", "-jar", "application.jar"]