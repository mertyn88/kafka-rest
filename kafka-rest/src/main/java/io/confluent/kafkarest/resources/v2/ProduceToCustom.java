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

package io.confluent.kafkarest.resources.v2;

import io.confluent.kafkarest.Versions;
import io.confluent.kafkarest.controllers.ProduceController;
import io.confluent.kafkarest.controllers.ProduceGenericController;
import io.confluent.kafkarest.controllers.RecordSerializer;
import io.confluent.kafkarest.controllers.SchemaManager;
import io.confluent.kafkarest.entities.EmbeddedFormat;
import io.confluent.kafkarest.entities.v2.ProduceRequest;
import io.confluent.kafkarest.entities.v2.ProduceResponse;
import io.confluent.kafkarest.extension.ResourceAccesslistFeature.ResourceName;
import io.confluent.kafkarest.resources.AsyncResponses.AsyncResponseBuilder;
import io.confluent.rest.annotations.PerformanceMetric;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Response;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Path("/consumer/kafka-rest/topics")
@Consumes({
  Versions.KAFKA_V2_LIFIC_SEARCH_JSON
})
@Produces({Versions.KAFKA_V2_LIFIC_SEARCH_JSON})
@ResourceName("api.v2.produce-to-topic.*")
public final class ProduceToCustom extends AbstractProduceAction {

  @Inject
  public ProduceToCustom(
      Provider<SchemaManager> schemaManager,
      Provider<RecordSerializer> recordSerializer,
      Provider<ProduceController> produceController,
      Provider<ProduceGenericController> produceGenericController
  ) {
    super(schemaManager, recordSerializer, produceController, produceGenericController);
  }

  @POST
  @Path("/{topic}")
  @PerformanceMetric("topic.produce-lific.search+v2")
  @Consumes({Versions.KAFKA_V2_LIFIC_SEARCH_JSON_WEIGHTED_LOW})
  @ResourceName("api.v2.produce-to-topic.lific.search")
  public void produceCustomLific(
          @Suspended AsyncResponse asyncResponse,
          @PathParam("topic") String topic,
          @Valid @NotNull ProduceRequest request) {
    CompletableFuture<ProduceResponse> response = produceGenericSchema(topic, request);
    AsyncResponseBuilder.<ProduceResponse>from(Response.ok())
            .entity(response)
            .status(ProduceResponse::getRequestStatus)
            .asyncResume(asyncResponse);
  }
}
