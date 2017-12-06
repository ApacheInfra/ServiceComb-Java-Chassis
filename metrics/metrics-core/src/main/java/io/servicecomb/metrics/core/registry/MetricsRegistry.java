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

package io.servicecomb.metrics.core.registry;

import java.util.List;
import java.util.Map;

import io.servicecomb.metrics.core.metric.Metric;

public interface MetricsRegistry {
  void registerMetric(Metric metric);

  Metric getMetric(String name);

  Metric getOrCreateMetric(Metric metric);

  List<Long> getPollingIntervals();

  Map<String, Number> getAllMetricsValue();

  Map<String, Number> getMetricsValues(String group);

  Map<String, Number> getMetricsValues(String group, String level);

  Map<String, Number> getMetricsValues(String group, String level, String catalog);
}
