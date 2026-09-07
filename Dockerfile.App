FROM gradle:8.6.0-jdk21-alpine AS build
WORKDIR /app
COPY . ./
RUN gradle clean bootJar --info

FROM eclipse-temurin:21-jre

# T-Bank API root CA is missing from the JDK truststore; without it the app fails
# at startup with "PKIX path building failed". Fingerprint pinned.
COPY certs/russian_trusted_root_ca.pem /opt/certs/russian_trusted_root_ca.pem
RUN keytool -printcert -file /opt/certs/russian_trusted_root_ca.pem \
      | grep -q 'D2:6D:2D:02:31:B7:C3:9F:92:CC:73:85:12:BA:54:10:35:19:E4:40:5D:68:B5:BD:70:3E:97:88:CA:8E:CF:31' \
    && keytool -importcert -noprompt -trustcacerts -cacerts -storepass changeit \
      -alias russian_trusted_root_ca -file /opt/certs/russian_trusted_root_ca.pem

COPY --from=build /app/build/libs/tinkoff-invest-robot-*.jar application.jar
ENTRYPOINT ["java", "-jar", "application.jar"]
