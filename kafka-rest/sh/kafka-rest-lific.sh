#!/bin/bash

#  docker build --tag 697736665449.dkr.ecr.ap-northeast-2.amazonaws.com/kafka-rest:dev.1 ./kafka-rest

echo "id="$KAFKA_REST_ID >> kafka-rest.properties
echo "schema.registry.url="$KAFKA_REST_SCHEMA_REGISTRY_URL >> kafka-rest.properties
echo "zookeeper.connect="$KAFKA_REST_ZOOKEEPER_CONNECT >> kafka-rest.properties
echo "bootstrap.servers=PLAINTEXT://"$KAFKA_REST_BOOTSTRAP_SERVER >> kafka-rest.properties
echo "port="$KAFKA_REST_PORT >> kafka-rest.properties

# Run jar
java -jar app.jar kafka-rest.properties