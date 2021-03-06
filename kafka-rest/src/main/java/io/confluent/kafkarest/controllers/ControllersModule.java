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

import org.glassfish.hk2.utilities.binding.AbstractBinder;

import javax.inject.Singleton;

/** A module to install the various controllers required by the application. */
public final class ControllersModule extends AbstractBinder {

  protected void configure() {
    bind(AclManagerImpl.class).to(AclManager.class);
    bind(BrokerConfigManagerImpl.class).to(BrokerConfigManager.class);
    bind(BrokerManagerImpl.class).to(BrokerManager.class);
    bind(ClusterConfigManagerImpl.class).to(ClusterConfigManager.class);
    bind(ClusterManagerImpl.class).to(ClusterManager.class);
    bind(ConsumerAssignmentManagerImpl.class).to(ConsumerAssignmentManager.class);
    bind(ConsumerGroupLagSummaryManagerImpl.class).to(ConsumerGroupLagSummaryManager.class);
    bind(ConsumerGroupManagerImpl.class).to(ConsumerGroupManager.class);
    bind(ConsumerLagManagerImpl.class).to(ConsumerLagManager.class);
    bind(ConsumerManagerImpl.class).to(ConsumerManager.class);
    bindAsContract(NoSchemaRecordSerializer.class).in(Singleton.class);
    bind(PartitionManagerImpl.class).to(PartitionManager.class);
    bind(ProduceControllerImpl.class).to(ProduceController.class);
    bind(ProduceGenericControllerImpl.class).to(ProduceGenericController.class);
    bind(ReassignmentManagerImpl.class).to(ReassignmentManager.class);
    bind(RecordSerializerFacade.class).to(RecordSerializer.class);
    bind(ReplicaManagerImpl.class).to(ReplicaManager.class);
    bind(SchemaManagerImpl.class).to(SchemaManager.class);
    bindAsContract(SchemaRecordSerializer.class);
    bind(TopicConfigManagerImpl.class).to(TopicConfigManager.class);
    bind(TopicManagerImpl.class).to(TopicManager.class);
  }
}
