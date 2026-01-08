FROM gradle:8.6.0-jdk21-alpine AS build
WORKDIR /app
COPY . ./
RUN gradle clean bootJar --info

FROM eclipse-temurin:21-jre
COPY --from=build /app/build/libs/tinkoff-invest-robot-*.jar application.jar
ENTRYPOINT ["java", "-jar", "application.jar"]