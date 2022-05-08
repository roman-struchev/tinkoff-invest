FROM gradle:6.9.2-jdk11-alpine AS build
WORKDIR /app
COPY . ./
RUN gradle clean bootJar --info

FROM openjdk:11.0.4-jre-slim
COPY --from=build /app/build/libs/invest-robot-*.jar application.jar
ENTRYPOINT ["java", "-jar", "application.jar"]