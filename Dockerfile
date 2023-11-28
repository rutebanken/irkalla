FROM eclipse-temurin:17.0.6_10-jdk-alpine

RUN apk update && apk upgrade && apk add --no-cache \
    tini

WORKDIR /deployments
COPY target/irkalla-*-SNAPSHOT.jar /deployments/irkalla.jar



RUN addgroup appuser && adduser --disabled-password appuser --ingroup appuser
RUN chown -R appuser:appuser /deployments
USER appuser

CMD [ "/sbin/tini", "--", "java", "-jar", "/deployments/irkalla.jar" ]
