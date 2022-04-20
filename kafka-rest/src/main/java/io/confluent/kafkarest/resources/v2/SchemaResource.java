/*
 * Copyright 2018 Confluent Inc.
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
import io.confluent.kafkarest.controllers.SchemaManager;
import io.confluent.kafkarest.extension.ResourceAccesslistFeature.ResourceName;
import io.confluent.kafkarest.resources.AsyncResponses;
import io.confluent.rest.annotations.PerformanceMetric;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static java.util.Objects.requireNonNull;

@Path("/schema")
@Consumes({Versions.KAFKA_V2_JSON})
@Produces({Versions.KAFKA_V2_JSON})
@ResourceName("api.v2.schema.*")
public final class SchemaResource {

  private final Provider<SchemaManager> schemaManager;

  @Inject
  public SchemaResource(Provider<SchemaManager> schemaManager) {
    this.schemaManager = requireNonNull(schemaManager);
  }

  @GET
  @Path("/refresh")
  @PerformanceMetric("schema.refresh+v2")
  @ResourceName("api.v2.schema.refresh")
  public void refresh(@Suspended AsyncResponse asyncResponse) {
    CompletableFuture<List<String>> response =
    this.schemaManager.get().refreshSchemaSubject()
            .thenApply(subjects -> new ArrayList<>(subjects.keySet()));
    AsyncResponses.asyncResume(asyncResponse, response);
  }
}
