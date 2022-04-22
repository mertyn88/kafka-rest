FROM adoptopenjdk/openjdk11:alpine-jre
RUN addgroup -S lific && adduser -S lific -G lific
USER lific:lific
WORKDIR /home/lific
COPY --chown=lific:lific ./target/kafka-rest-7.1.0-standalone.jar app.jar
COPY --chown=lific:lific ./sh/kafka-rest-lific.sh kafka-rest-lific.sh
ENV LC_ALL ko_KR.UTF-8
ENV KAFKA_REST_ID ID
ENV KAFKA_REST_SCHEMA_REGISTRY_URL URL
ENV KAFKA_REST_ZOOKEEPER_CONNECT ZOO
ENV KAFKA_REST_BOOTSTRAP_SERVER SERVER
ENV KAFKA_REST_PORT 8082

CMD ["/bin/sh", "kafka-rest-lific.sh"]