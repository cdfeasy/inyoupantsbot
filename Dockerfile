FROM openjdk:8-jdk-alpine
CMD ["sh", "-c", "mkdir /opt/inyoupantsbot"]
CMD ["sh", "-c", "mkdir /opt/inyoupantsbot/logs"]
CMD ["sh", "-c", "mkdir /opt/common"]

COPY target/inyoupantsbot-1.0-SNAPSHOT.jar /opt/common/inyoupantsbot-1.0-SNAPSHOT.jar
COPY target/lib /opt/common/lib
COPY src/main/resources/inyoupanstbot.xml /opt/common/inyoupanstbot.xml
COPY src/main/resources/state.json /opt/common/state.json
VOLUME ["/opt/common"]
ENV JAVA_OPTS=""
EXPOSE 443 80 88 8443
ENTRYPOINT ["sh", "-c", "java -jar /opt/common/inyoupantsbot-1.0-SNAPSHOT.jar"]
