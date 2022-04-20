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

package io.confluent.kafkarest.entities.v2;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import javax.validation.constraints.NotEmpty;

@AutoValue
public abstract class LificSearchProduceRequest {

    @NotEmpty
    @JsonProperty("records")
    public abstract ImmutableList<ProduceRecord> getRecords();

    @JsonProperty("topic")
    public abstract Optional<String> getTopic();

    public static LificSearchProduceRequest create(List<ProduceRecord> records) {
        return create(records, null);
    }

    public static LificSearchProduceRequest create(List<ProduceRecord> records, @Nullable String topic) {
        return new AutoValue_LificSearchProduceRequest(ImmutableList.copyOf(records), Optional.ofNullable(topic));
    }

    @JsonCreator
    static LificSearchProduceRequest fromJson(
            @JsonProperty("records") @Nullable List<ProduceRecord> records,
            @JsonProperty("topic") @Nullable String topic) {
        return create(records != null ? records : ImmutableList.of(), topic );
    }

    @AutoValue
    public abstract static class ProduceRecord {

        @JsonProperty("partition")
        public abstract Optional<Integer> getPartition();

        @JsonProperty("key")
        public abstract Optional<JsonNode> getKey();

        @JsonProperty("value")
        public abstract Optional<JsonNode> getValue();

        public static ProduceRecord create(@Nullable JsonNode key, @Nullable JsonNode value) {
            return create(/* partition= */ null, key, value);
        }

        public static LificSearchProduceRequest.ProduceRecord create(
                @Nullable Integer partition, @Nullable JsonNode key, @Nullable JsonNode value) {
            return new AutoValue_LificSearchProduceRequest_ProduceRecord(
                    Optional.ofNullable(partition), Optional.ofNullable(key), Optional.ofNullable(value));
        }

        @JsonCreator
        static ProduceRecord fromJson(
                @JsonProperty("partition") @Nullable Integer partition,
                @JsonProperty("key") @Nullable JsonNode key,
                @JsonProperty("value") @Nullable JsonNode value) {
            return create(partition, key, value);
        }
    }
}
