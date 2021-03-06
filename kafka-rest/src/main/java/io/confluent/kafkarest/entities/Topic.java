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

package io.confluent.kafkarest.entities;

import static java.util.Collections.emptySet;

import com.google.auto.value.AutoValue;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

@AutoValue
public abstract class Topic {

  Topic() {}

  public abstract String getClusterId();

  public abstract String getName();

  public abstract ImmutableList<Partition> getPartitions();

  public abstract short getReplicationFactor();

  public abstract boolean isInternal();

  public abstract Set<Acl.Operation> getAuthorizedOperations();

  public static Topic create(
      String clusterId,
      String name,
      List<Partition> partitions,
      short replicationFactor,
      boolean isInternal) {
    return create(clusterId, name, partitions, replicationFactor, isInternal, null);
  }

  public static Topic create(
      String clusterId,
      String name,
      List<Partition> partitions,
      short replicationFactor,
      boolean isInternal,
      @Nullable Set<Acl.Operation> authorizedOperations) {
    return new AutoValue_Topic(
        clusterId,
        name,
        ImmutableList.copyOf(partitions),
        replicationFactor,
        isInternal,
        authorizedOperations == null ? emptySet() : authorizedOperations);
  }
}
