FROM openjdk:11.0.4-jre-slim
COPY ./build/libs/invest-robot-*.jar application.jar
ENTRYPOINT ["java", "-jar", "application.jar"]