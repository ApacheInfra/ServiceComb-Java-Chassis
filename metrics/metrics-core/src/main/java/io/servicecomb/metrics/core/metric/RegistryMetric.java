/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.servicecomb.metrics.core.metric;

import java.util.Map;

import io.servicecomb.metrics.core.monitor.RegistryMonitor;

public class RegistryMetric {

  private final InstanceMetric instanceMetric;

  private final Map<String, InvocationMetric> invocationMetrics;

  public InstanceMetric getInstanceMetric() {
    return instanceMetric;
  }

  public Map<String, InvocationMetric> getInvocationMetrics() {
    return invocationMetrics;
  }

  public RegistryMetric(RegistryMonitor registryMonitor, int pollerIndex) {
    invocationMetrics = registryMonitor.toInvocationMetrics(pollerIndex);

    //sum instance level metric
    long waitInQueue = 0;
    TimeMetric lifeTimeInQueue = new TimeMetric();
    TimeMetric executionTime = new TimeMetric();
    TimeMetric consumerLatency = new TimeMetric();
    TimeMetric producerLatency = new TimeMetric();
    for (InvocationMetric metric : invocationMetrics.values()) {
      waitInQueue += metric.getWaitInQueue();
      lifeTimeInQueue = lifeTimeInQueue.merge(metric.getLifeTimeInQueue());
      executionTime = executionTime.merge(metric.getExecutionTime());
      consumerLatency = consumerLatency.merge(metric.getConsumerLatency());
      producerLatency = producerLatency.merge(metric.getProducerLatency());
    }
    instanceMetric = new InstanceMetric(waitInQueue, lifeTimeInQueue, executionTime, consumerLatency, producerLatency);
  }
}
