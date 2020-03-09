FROM adoptopenjdk/openjdk11:alpine-jre
WORKDIR /deployments
COPY target/irkalla-*-SNAPSHOT.jar irkalla.jar
CMD java $JAVA_OPTIONS -jar irkalla.jar
