/*
 * Copyright 2021 Confluent Inc.
 *
 * Licensed under the Confluent Community License (the "License"); you may not use
 * this file except in compliance with the License.  You may obtain a copy of the
 * License at
 *
 * http://www.confluent.io/confluent-community-license
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */

package io.confluent.kafkarest.controllers;

import io.confluent.kafkarest.entities.ProduceResult;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.SerializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static java.util.Objects.requireNonNull;

final class ProduceGenericControllerImpl implements ProduceGenericController {

    private static final Logger log = LoggerFactory.getLogger(ProduceGenericController.class);

    private final Producer<String, GenericRecord> genericProducer;

    @Inject
    ProduceGenericControllerImpl(Producer<String, GenericRecord> genericProducer) {
        this.genericProducer = requireNonNull(genericProducer);
    }

    @Override
    public CompletableFuture<ProduceResult> produce(String topic, GenericRecord genericRecord) {
        CompletableFuture<ProduceResult> result = new CompletableFuture<>();
        try {
            log.debug("Received response from kafka");
            result.complete(ProduceResult.fromRecordMetadata(genericProducer.send(new ProducerRecord<>(topic, genericRecord)).get(), Instant.now()));
        }catch (SerializationException | ExecutionException | InterruptedException e) {
            log.error("Received exception from kafka", e);
            result.completeExceptionally(e);
            //e.printStackTrace();
        }

        return result;
    }
}
