FROM openjdk:8-jdk-alpine
CMD ["sh", "-c", "mkdir /opt/inyoupantsbot/bot"]
CMD ["sh", "-c", "mkdir /opt/inyoupantsbot/bot/lib"]
CMD ["sh", "-c", "mkdir /opt/inyoupantsbot/conf"]
CMD ["sh", "-c", "mkdir /opt/inyoupantsbot/logs"]

COPY target/inyoupantsbot-1.0-SNAPSHOT.jar /opt/inyoupantsbot/bot/inyoupantsbot-1.0-SNAPSHOT.jar
COPY target/lib /opt/inyoupantsbot/bot/lib
COPY src/main/resources/inyoupanstbot.xml /opt/inyoupantsbot/conf/inyoupanstbot.xml
COPY src/main/resources/state.json /opt/inyoupantsbot/conf/state.json
VOLUME ["/opt/inyoupantsbot/bot"]
ENV JAVA_OPTS=""
EXPOSE 443 80 88 8443
ENTRYPOINT ["sh", "-c", "java -jar /opt/inyoupantsbot/bot/inyoupantsbot-1.0-SNAPSHOT.jar"]
